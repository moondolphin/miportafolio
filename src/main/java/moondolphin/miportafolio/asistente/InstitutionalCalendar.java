package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.ClassPathResource;

/** Executable projection of INST-01; changes to its opening rule require explicit review. */
final class InstitutionalCalendar {
    private static final InstitutionalCalendar CURRENT = load();
    private final JsonNode rule;
    private final ZoneId zone;
    InstitutionalCalendar(JsonNode rule, String corpus) {
        this.rule = rule;
        zone = ZoneId.of(rule.path("zone").asText());
        String section = java.util.Arrays.stream(corpus.split("(?m)^##\\s+"))
                .filter(s -> s.startsWith(rule.path("sourceId").asText() + " ")).findFirst().orElse("");
        if (!section.replace("\r\n", "\n").contains(rule.path("sourceRule").asText())
                || rule.path("sourceRule").asText().isBlank() || rule.path("days").isEmpty()
                || !LocalTime.parse(rule.path("opens").asText()).isBefore(LocalTime.parse(rule.path("closes").asText()))) {
            throw new IllegalStateException("La proyección de calendario no coincide con INST-01. Revisar RAG, calendario y pruebas juntos.");
        }
    }
    static InstitutionalCalendar current() { return CURRENT; }
    JsonNode context() { return rule.deepCopy(); }
    boolean permits(AvailabilityClient.Slot slot) {
        try {
            LocalDateTime start;
            LocalDateTime end;
            if (slot.inicio() != null && slot.fin() != null) {
                start = OffsetDateTime.parse(slot.inicio()).atZoneSameInstant(zone).toLocalDateTime();
                end = OffsetDateTime.parse(slot.fin()).atZoneSameInstant(zone).toLocalDateTime();
            } else {
                var parts = slot.label().split(" a ", 2);
                var format = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
                start = LocalDateTime.parse(parts[0], format);
                end = LocalDateTime.parse(parts[1], format);
            }
            if (!end.isAfter(start) || !start.toLocalDate().equals(end.toLocalDate())) return false;
            int weekday = start.getDayOfWeek().getValue();
            boolean day = false;
            for (var allowed : rule.path("days")) if (allowed.asInt() == weekday) day = true;
            for (var closed : rule.path("closedDates")) if (closed.asText().equals(start.toLocalDate().toString())) return false;
            return day && !start.toLocalTime().isBefore(LocalTime.parse(rule.path("opens").asText()))
                    && !end.toLocalTime().isAfter(LocalTime.parse(rule.path("closes").asText()));
        } catch (DateTimeException | IllegalArgumentException | ArrayIndexOutOfBoundsException exception) {
            return false;
        }
    }
    private static InstitutionalCalendar load() {
        try {
            var json = new ObjectMapper().readTree(resource("institutional-calendar.json"));
            return new InstitutionalCalendar(json, resource("base_rag_consultorio_v1.md"));
        } catch (IOException exception) { throw new IllegalStateException("No se pudo cargar el calendario institucional.", exception); }
    }
    private static String resource(String name) throws IOException {
        return new ClassPathResource("asistente/" + name).getContentAsString(StandardCharsets.UTF_8);
    }
}
