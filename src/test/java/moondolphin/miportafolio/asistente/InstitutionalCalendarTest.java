package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpSession;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstitutionalCalendarTest {
    private AvailabilityClient.Slot slot(String start, String end) {
        return new AvailabilityClient.Slot("2", "", start, end);
    }
    @Test void rejectsWeekendOutOfHoursAndCrossMidnightButAcceptsTheClosingBoundary() {
        var c = InstitutionalCalendar.current();
        assertThat(c.permits(slot("2026-11-14T10:00:00-03:00", "2026-11-14T10:30:00-03:00"))).isFalse();
        assertThat(c.permits(slot("2026-11-15T10:00:00-03:00", "2026-11-15T10:30:00-03:00"))).isFalse();
        assertThat(c.permits(slot("2026-11-13T08:30:00-03:00", "2026-11-13T09:00:00-03:00"))).isFalse();
        assertThat(c.permits(slot("2026-11-13T18:00:00-03:00", "2026-11-13T18:30:00-03:00"))).isFalse();
        assertThat(c.permits(slot("2026-11-13T17:30:00-03:00", "2026-11-14T10:00:00-03:00"))).isFalse();
        assertThat(c.permits(slot("2026-11-13T20:30:00Z", "2026-11-13T21:00:00Z"))).isTrue();
    }
    @Test void aReviewedClosedDateIsExcludedAndChangingTheNormativeRuleNeedsReview() throws Exception {
        var rule = InstitutionalCalendar.current().context();
        ((com.fasterxml.jackson.databind.node.ObjectNode) rule).withArray("closedDates").add("2026-11-13");
        var corpus = new ClassPathResource("asistente/base_rag_consultorio_v1.md").getContentAsString(StandardCharsets.UTF_8);
        var c = new InstitutionalCalendar(rule, corpus);
        assertThat(c.permits(slot("2026-11-13T10:00:00-03:00", "2026-11-13T10:30:00-03:00"))).isFalse();
        assertThatThrownBy(() -> new InstitutionalCalendar(rule, corpus.replace("de lunes a viernes de 9:00 a 18:00", "de lunes a sábado de 8:00 a 20:00")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("INST-01");
    }
    @Test void anInvalidSlotCannotBePreparedEvenIfPassedToSessionMemory() {
        var client = mock(RegistrationClient.class);
        var booking = new BookingService(client);
        var session = new MockHttpSession();
        booking.remember(session, List.of(slot("2026-11-14T10:00:00-03:00", "2026-11-14T10:30:00-03:00")));
        assertThatThrownBy(() -> booking.prepare(session, new BookingService.Details("2", "Demo", "demo@example.com", "Control")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verifyNoInteractions(client);
    }
}
