package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

final class SlotSelection {
    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    private SlotSelection() {}

    static Result resolve(JsonNode selection, String message, List<AvailabilityClient.Slot> slots, LocalDate today) {
        String id = selection.path("id").asText("");
        String date = selection.path("fecha").asText("");
        String time = selection.path("hora").asText("");
        var relative = selection.path("desplazamiento_dias");
        if (relative.isIntegralNumber()) {
            if (relative.asLong() < -366 || relative.asLong() > 366) return new Result(true, "", "La referencia temporal requiere aclaración.");
            date = today.plusDays(relative.asInt()).toString();
        }
        if (id.isEmpty() && date.isEmpty() && time.isEmpty()) {
            return selection.path("ambigua").asBoolean()
                    ? new Result(true, "", "La selección de horario es ambigua. Pedir aclaración usando las opciones reales.")
                    : new Result(false, "", "");
        }
        try {
            LocalDate requestedDate = date.isEmpty() ? null : LocalDate.parse(date);
            LocalTime requestedTime = time.isEmpty() ? null : parseHour(time);
            List<AvailabilityClient.Slot> matches = slots;
            if (requestedDate != null || requestedTime != null) {
                matches = slots.stream().filter(slot -> {
                    LocalDateTime start = start(slot);
                    return start != null && (requestedDate == null || start.toLocalDate().equals(requestedDate))
                            && (requestedTime == null || start.toLocalTime().equals(requestedTime));
                }).toList();
            } else {
                matches = slots.stream().filter(slot -> slot.id().equals(id)).toList();
            }
            if (matches.size() == 1) {
                return new Result(true, matches.get(0).id(), "");
            }
            return new Result(true, "", matches.isEmpty()
                    ? "La fecha/hora elegida no coincide con los horarios ofrecidos. Pedir otra opción."
                    : "Hay varias opciones compatibles. Pedir la fecha y el horario exactos.");
        } catch (java.time.DateTimeException exception) {
            return new Result(true, "", "No se pudo validar la fecha u hora elegida. Pedir aclaración.");
        }
    }

    private static LocalTime parseHour(String value) {
        // Accept the numeric time extracted by the LLM, without matching user phrases.
        if (value.matches("[0-9]{1,2}")) return LocalTime.of(Integer.parseInt(value), 0);
        if (value.matches("[0-9]{1,2}:[0-9]{2}")) {
            var parts = value.split(":");
            return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        return LocalTime.parse(value);
    }

    private static LocalDateTime start(AvailabilityClient.Slot slot) {
        try {
            if (slot.inicio() != null) {
                return OffsetDateTime.parse(slot.inicio()).atZoneSameInstant(ZONE).toLocalDateTime();
            }
            return LocalDateTime.parse(slot.label().split(" a ", 2)[0],
                    DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm"));
        } catch (java.time.DateTimeException exception) {
            return null;
        }
    }

    record Result(boolean requested, String id, String issue) {}
}
