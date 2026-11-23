package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrationReceiptGuardTest {
    private final RegistrationClient registration = mock(RegistrationClient.class);
    private final BookingService booking = new BookingService(registration);
    private final OpenAiClient ai = mock(OpenAiClient.class);
    private final InstitutionalAssistant rag = mock(InstitutionalAssistant.class);
    private final MockHttpSession session = new MockHttpSession();
    private final AvailabilityClient availability = mock(AvailabilityClient.class);
    private final BookingConversation dialogue = new BookingConversation(booking, availability, ai, rag);
    private final ObjectMapper json = new ObjectMapper();

    private void model(String plan) throws Exception {
        when(rag.prompt()).thenReturn("V4");
        when(ai.planDialogue(anyString(), anyString())).thenReturn(json.readTree(plan));
        when(ai.writeDialogue(anyString(), anyString())).thenReturn(json.readTree(
                "{\"respuesta\":\"He registrado tu solicitud\",\"usa_datos_dinamicos\":false,\"herramientas\":[],\"acciones_anunciadas\":[]}"));
    }

    @Test
    void blockedRegistrationCannotBeAnnouncedAsSuccessfulEvenIfModelClaimsIt() throws Exception {
        model("{\"confirmacion_explicita\":true,\"datos\":{\"nombre\":\"Demo\",\"correo\":\"demo@example.com\",\"motivo\":\"caries\"},\"herramientas\":[{\"nombre\":\"registrar\"}]}");
        var reply = dialogue.reply(session, "Sí, confirmo");
        assertThat(reply.answer()).startsWith("No se registró").doesNotContain("He registrado");
        assertThat(reply.booking()).isNull();
        verifyNoInteractions(registration);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void authorizedUnknownSendIsVerifiedAndResentWithSameKeyBeforeAnnouncingSuccess() throws Exception {
        when(availability.availableSlots()).thenReturn(List.of(new AvailabilityClient.Slot("2", "23/11/2026 17:00 a 23/11/2026 17:30")));
        model("{\"herramientas\":[{\"nombre\":\"disponibilidad\"}]}");
        when(ai.writeDialogue(anyString(), anyString())).thenReturn(json.readTree("{\"respuesta\":\"Opciones disponibles\",\"herramientas\":[],\"acciones_anunciadas\":[]}"));
        dialogue.reply(session, "Consultá disponibilidad");
        var prepared = booking.prepare(session, new BookingService.Details("2", "Demo", "demo@example.com", "Control"));
        dialogue.reviewShown(session, prepared);
        model("{\"confirmacion_explicita\":true,\"herramientas\":[{\"nombre\":\"registrar\"}]}");
        when(registration.register(anyMap())).thenReturn(RegistrationClient.Result.unknown(),
                new RegistrationClient.Result("registrada", UUID.randomUUID().toString(), "pendiente"));
        when(registration.inspect(anyMap())).thenReturn(new RegistrationClient.LookupResult("no_encontrada", null));
        var reply = dialogue.reply(session, "Sí, confirmo");
        assertThat(reply.booking().stage()).isEqualTo("registrada");
        assertThat(reply.booking().comprobante()).isNotBlank();
        assertThat(reply.answer()).isEqualTo("He registrado tu solicitud");
        var order = inOrder(registration);
        org.mockito.ArgumentCaptor<Map<String, String>> payload = org.mockito.ArgumentCaptor.forClass((Class) Map.class);
        order.verify(registration).requireConfiguration();
        order.verify(registration).register(payload.capture());
        order.verify(registration).requireConfiguration();
        order.verify(registration).inspect(anyMap());
        order.verify(registration).register(payload.capture());
        assertThat(payload.getAllValues().get(0)).isEqualTo(payload.getAllValues().get(1));
        verify(registration, times(2)).register(anyMap());
    }
}
