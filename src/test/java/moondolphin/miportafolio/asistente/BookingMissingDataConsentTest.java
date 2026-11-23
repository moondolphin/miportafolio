package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingMissingDataConsentTest {
    private final RegistrationClient registration = mock(RegistrationClient.class);
    private final BookingService booking = new BookingService(registration);
    private final OpenAiClient ai = mock(OpenAiClient.class);
    private final InstitutionalAssistant rag = mock(InstitutionalAssistant.class);
    private final AvailabilityClient availability = mock(AvailabilityClient.class);
    private final MockHttpSession session = new MockHttpSession();
    private final BookingConversation dialogue = new BookingConversation(booking, availability, ai, rag);
    private final ObjectMapper json = new ObjectMapper();

    private void plan(String value) throws Exception {
        when(rag.prompt()).thenReturn("V4");
        when(ai.planDialogue(anyString(), anyString())).thenReturn(json.readTree(value));
        // Deliberately incorrect draft: the guard must prevent unproved success.
        when(ai.writeDialogue(anyString(), anyString())).thenReturn(json.readTree(
                "{\"respuesta\":\"He registrado tu solicitud\",\"herramientas\":[],\"acciones_anunciadas\":[],\"usa_datos_dinamicos\":false}"));
    }

    private void offer() throws Exception {
        when(availability.availableSlots()).thenReturn(List.of(new AvailabilityClient.Slot("2", "23/11/2026 17:00 a 23/11/2026 17:30")));
        plan("{\"herramientas\":[{\"nombre\":\"disponibilidad\"}]}");
        when(ai.writeDialogue(anyString(), anyString())).thenReturn(json.readTree("{\"respuesta\":\"Opciones disponibles\",\"herramientas\":[],\"acciones_anunciadas\":[]}"));
        dialogue.reply(session, "Consultá disponibilidad");
    }

    @Test
    void acceptsPatientReasonAndShowsSummaryWithoutRegisteringBeforeConsent() throws Exception {
        offer();
        plan("""
            {"intencion_turno":true,"confirmacion_explicita":false,
             "datos":{"nombre":"Serena Demo","correo":"demo@example.com","motivo":"por una carie"},
             "seleccion":{"id":"2"},"herramientas":[{"nombre":"registrar"}]}
            """);
        var summary = dialogue.reply(session, "Soy Serena Demo, demo@example.com, por una carie, el de las 17");
        assertThat(summary.booking().stage()).isEqualTo("resumen");
        assertThat(summary.booking().details().motivo()).isEqualTo("por una carie");
        assertThat(summary.answer()).contains("Serena Demo", "demo@example.com", "por una carie", "17:00", "¿Confirmás")
                .doesNotContain("He registrado");
        assertThat(summary.booking().comprobante()).isNull();
        verifyNoInteractions(registration);
        plan("{\"confirmacion_explicita\":true,\"herramientas\":[{\"nombre\":\"registrar\"}]}");
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada", UUID.randomUUID().toString(), "pendiente"));
        var saved = dialogue.reply(session, "Sí, enviá la solicitud");
        assertThat(saved.booking().comprobante()).isNotBlank();
        verify(registration).register(argThat(payload -> "por una carie".equals(payload.get("motivo"))));
    }

    @Test
    void missingDataDoesNotConsumeConsentAndCompletingItAllowsOneRegistrationWithoutAnotherYes() throws Exception {
        offer();
        plan("""
            {"intencion_turno":true,"confirmacion_explicita":true,
             "datos":{"nombre":"Serena Demo","motivo":"por una carie e infección que menciono"},
             "seleccion":{"id":"2"},"herramientas":[{"nombre":"registrar"}]}
            """);
        var incomplete = dialogue.reply(session, "Sí, enviá la solicitud. Soy Serena Demo, es por una carie e infección, el de las 17");
        assertThat(incomplete.answer()).contains("Falta un correo electrónico válido").doesNotContain("He registrado");
        assertThat(incomplete.booking()).isNull();
        verifyNoInteractions(registration);
        plan("{\"confirmacion_explicita\":false,\"datos\":{\"correo\":\"demo@example.com\"},\"herramientas\":[{\"nombre\":\"registrar\"}]}");
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada", UUID.randomUUID().toString(), "pendiente"));
        String id = UUID.randomUUID().toString();
        var saved = dialogue.reply(session, "Mi correo es demo@example.com", id, false);
        assertThat(saved.booking().stage()).isEqualTo("registrada");
        assertThat(saved.booking().comprobante()).isNotBlank();
        assertThat(saved.booking().details().motivo()).contains("carie", "infección");
        assertThat(dialogue.reply(session, "Mi correo es demo@example.com", id, false)).isSameAs(saved);
        verify(registration).register(anyMap());
        verify(ai, atLeastOnce()).planDialogue(anyString(), contains("\"autorizacion_registro_conservada\":true"));
    }
}
