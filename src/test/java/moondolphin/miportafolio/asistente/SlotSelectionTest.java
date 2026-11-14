package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlotSelectionTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void aTimeWithoutADateCanBeAmbiguousAndTomorrowUsesBuenosAiresDate() throws Exception {
        var slots = List.of(
                new AvailabilityClient.Slot("2", "", "2026-11-13T20:00:00Z", "2026-11-13T20:30:00Z"),
                new AvailabilityClient.Slot("3", "", "2026-11-14T17:00:00-03:00", "2026-11-14T17:30:00-03:00"));
        var timeOnly = json.readTree("{\"hora\":\"17:00\"}");
        assertThat(SlotSelection.resolve(timeOnly, "a las 17", slots, LocalDate.of(2026, 11, 12)).id()).isEmpty();
        var tomorrow = json.readTree("{\"fecha\":\"2026-11-13\",\"hora\":\"17:00\"}");
        assertThat(SlotSelection.resolve(tomorrow, "mañana a las 17", slots, LocalDate.of(2026, 11, 12)).id()).isEqualTo("2");
    }

    @Test
    void anHourMeansTheExactStartOfASlotAndOverridesAStaleId() throws Exception {
        var slots = List.of(new AvailabilityClient.Slot("7", "", "2026-11-13T13:00:00Z", "2026-11-13T13:30:00Z"));
        for (String hour : List.of("10", "10:00", "10:00:00")) {
            var selection = json.createObjectNode().put("hora", hour).put("id", "stale").put("ambigua", true);
            var resolved = SlotSelection.resolve(selection, "texto libre interpretado por LLM", slots, LocalDate.of(2026,11,13));
            assertThat(resolved.id()).isEqualTo("7");
            assertThat(resolved.issue()).isEmpty();
        }
    }
    @Test
    void relativeDaysUseLocalDateAndMultipleExactStartsRequireClarification() throws Exception {
        var slots = List.of(
            new AvailabilityClient.Slot("7", "", "2026-11-13T10:00:00-03:00", "2026-11-13T10:30:00-03:00"),
            new AvailabilityClient.Slot("8", "", "2026-11-16T10:00:00-03:00", "2026-11-16T10:30:00-03:00"));
        var selection = json.createObjectNode().put("hora", "10");
        assertThat(SlotSelection.resolve(selection, "", slots, LocalDate.of(2026,11,12)).issue()).contains("varias");
        selection.put("desplazamiento_dias", 1);
        assertThat(SlotSelection.resolve(selection, "", slots, LocalDate.of(2026,11,12)).id()).isEqualTo("7");
        selection.put("hora", "11");
        assertThat(SlotSelection.resolve(selection, "", slots, LocalDate.of(2026,11,12)).id()).isEmpty();
    }
}
