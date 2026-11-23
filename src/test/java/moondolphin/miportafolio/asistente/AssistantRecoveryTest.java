package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssistantRecoveryTest {
    private final RegistrationClient registration = mock(RegistrationClient.class);
    private final BookingService booking = new BookingService(registration);
    private final MockHttpSession session = new MockHttpSession();
    private static final String ROW = "681a1f4d-680a-4d1d-96ed-fb418e313329";

    private BookingService.View uncertain() {
        booking.remember(session, List.of(new AvailabilityClient.Slot("2", "16/11/2026 10:00 a 16/11/2026 10:30")));
        var prepared = booking.prepare(session, new BookingService.Details("2", "Demo", "demo@example.com", "Control"));
        when(registration.register(anyMap())).thenReturn(RegistrationClient.Result.unknown());
        assertThat(booking.confirm(session, prepared.attemptId()).stage()).isEqualTo("indeterminado");
        return prepared;
    }

    @Test
    void openAiThenN8nFailuresResumeWithoutDuplicatingSuccessfulOperationsOrHistory() throws Exception {
        var ai = mock(OpenAiClient.class);
        var rag = mock(InstitutionalAssistant.class);
        var availability = mock(AvailabilityClient.class);
        var json = new ObjectMapper();
        when(rag.prompt()).thenReturn("V4");
        when(ai.planDialogue(anyString(), anyString())).thenThrow(OpenAiClient.unavailable()).thenReturn(json.readTree(
                "{\"herramientas\":[{\"nombre\":\"disponibilidad\"}]}"));
        when(ai.writeDialogue(anyString(), anyString())).thenReturn(json.readTree(
                "{\"respuesta\":\"Respuesta de prueba\",\"herramientas\":[],\"acciones_anunciadas\":[]}"));
        when(availability.availableSlots()).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY))
                .thenReturn(List.of());
        var dialogue = new BookingConversation(booking, availability, ai, rag);
        String id = UUID.randomUUID().toString();
        assertThatThrownBy(() -> dialogue.reply(session, "Consultá disponibilidad", id, false))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(dialogue.reply(session, "Consultá disponibilidad", id, true).herramientas().get(0).exito()).isFalse();
        var recovered = dialogue.reply(session, "Consultá disponibilidad", id, true);
        assertThat(recovered.herramientas()).allMatch(BookingConversation.Evidence::exito);
        assertThat(recovered.conversation().history()).hasSize(2);
        assertThat(dialogue.reply(session, "Consultá disponibilidad", id, true)).isSameAs(recovered);
        verify(availability, times(2)).availableSlots();
        verify(ai, times(2)).planDialogue(anyString(), anyString());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void indeterminateRegistrationVerifiesThenResendsExactlyTheSamePayload() {
        var attempt = uncertain();
        when(registration.inspect(anyMap())).thenReturn(new RegistrationClient.LookupResult("no_encontrada", null));
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada", ROW, "pendiente"));
        var recovered = booking.confirm(session, attempt.attemptId());
        var order = inOrder(registration);
        order.verify(registration).requireConfiguration();
        org.mockito.ArgumentCaptor<Map<String, String>> payload = org.mockito.ArgumentCaptor.forClass((Class) Map.class);
        order.verify(registration).register(payload.capture());
        order.verify(registration).requireConfiguration();
        order.verify(registration).inspect(anyMap());
        order.verify(registration).register(payload.capture());
        assertThat(payload.getAllValues().get(0)).isEqualTo(payload.getAllValues().get(1));
        assertThat(recovered.stage()).isEqualTo("registrada");
        assertThat(recovered.comprobante()).isNotBlank();
    }

    @Test
    void alreadyPersistedRegistrationRecoversReceiptAndNeverWritesAgain() {
        var attempt = uncertain();
        var stored = new RegistrationClient.StoredRequest(ROW, "2", "Demo", "demo@example.com", "Control",
                "pendiente", "2026-11-16T10:00:00-03:00", "2026-11-16T10:30:00-03:00");
        when(registration.inspect(anyMap())).thenReturn(new RegistrationClient.LookupResult("encontrada", stored));
        var recovered = booking.confirm(session, attempt.attemptId());
        assertThat(recovered.solicitudId()).isEqualTo(ROW);
        assertThat(recovered.comprobante()).isNotBlank();
        assertThat(booking.confirm(session, attempt.attemptId()).comprobante()).isEqualTo(recovered.comprobante());
        var dialogue = new BookingConversation(booking, mock(AvailabilityClient.class), mock(OpenAiClient.class), mock(InstitutionalAssistant.class));
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setSession(session);
        var error = new AssistantController(dialogue).chatError(OpenAiClient.unavailable(), request);
        assertThat(((BookingService.View) error.getBody().get("booking")).comprobante()).isEqualTo(recovered.comprobante());
        verify(registration).register(anyMap());
        verify(registration).inspect(anyMap());
    }

    @Test
    void failedVerificationKeepsAttemptIndeterminateAndDoesNotResend() {
        var attempt = uncertain();
        when(registration.inspect(anyMap())).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY));
        assertThatThrownBy(() -> booking.confirm(session, attempt.attemptId())).isInstanceOf(ResponseStatusException.class);
        assertThat(booking.current(session).stage()).isEqualTo("indeterminado");
        verify(registration).register(anyMap());
    }
}
