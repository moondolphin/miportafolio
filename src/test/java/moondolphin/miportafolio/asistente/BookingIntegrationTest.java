package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingIntegrationTest {
    private static final String BASE = "/api/v1/public/assistant/";
    private static final String ROW = "681a1f4d-680a-4d1d-96ed-fb418e313329";
    private final RegistrationClient registration = mock(RegistrationClient.class);
    private final AvailabilityClient availability = mock(AvailabilityClient.class);
    private final InstitutionalAssistant institutional = mock(InstitutionalAssistant.class);
    private final BookingService booking = new BookingService(registration);
    private final MockHttpSession session = new MockHttpSession();
    private final ObjectMapper json = new ObjectMapper();
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new AssistantController(mock(BookingConversation.class)),
            new BookingController(booking, mock(BookingConversation.class))).build();

    private void offer() {
        booking.remember(session, List.of(new AvailabilityClient.Slot("2", "12/11/2026 10:00 a 12/11/2026 10:30")));
    }

    private JsonNode prepare() throws Exception {
        var result = mvc.perform(post(BASE + "booking/prepare").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"horarioId":"2","nombre":"Ana Demo","correo":"ana@example.com","motivo":"Control"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stage").value("resumen"))
                .andExpect(jsonPath("$.comprobante").doesNotExist())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode confirm(String attempt) throws Exception {
        var result = mvc.perform(post(BASE + "booking/confirm").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("attemptId", attempt))))
                .andExpect(status().isOk()).andReturn();
        return json.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void requiresReviewAndSendsOnlyAfterExplicitConfirmation() throws Exception {
        offer();
        JsonNode summary = prepare();
        verifyNoInteractions(registration, institutional);

        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada", ROW, "pendiente"));
        JsonNode saved = confirm(summary.path("attemptId").asText());

        assertThat(saved.path("stage").asText()).isEqualTo("registrada");
        assertThat(saved.path("message").asText()).contains("pendiente de confirmación por recepción");
        assertThat(saved.path("comprobante").asText()).hasSize(43);
        assertThat(saved.path("solicitudId").asText()).isEqualTo(ROW);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void retriesTheSameAttemptAndRecoversTheOriginalReceipt() throws Exception {
        offer();
        String attempt = prepare().path("attemptId").asText();
        when(registration.register(anyMap())).thenReturn(RegistrationClient.Result.unknown(),
                new RegistrationClient.Result("ya_registrada", ROW, "pendiente"));

        assertThat(confirm(attempt).path("stage").asText()).isEqualTo("indeterminado");
        mvc.perform(post(BASE + "booking/prepare").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horarioId\":\"2\",\"nombre\":\"Otra\",\"correo\":\"otra@example.com\",\"motivo\":\"Otro\"}"))
                .andExpect(status().isConflict());

        JsonNode saved = confirm(attempt);
        ArgumentCaptor<Map<String, String>> payload = ArgumentCaptor.forClass((Class) Map.class);
        verify(registration, times(2)).register(payload.capture());
        List<Map<String, String>> requests = payload.getAllValues();
        assertThat(requests.get(0)).isEqualTo(requests.get(1));
        assertThat(requests.get(0).get("clave_idempotencia")).isEqualTo(attempt);
        String expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(saved.path("comprobante").asText().getBytes(StandardCharsets.UTF_8)));
        assertThat(requests.get(0).get("comprobante_hash")).isEqualTo(expectedHash);
        assertThat(requests.get(0)).doesNotContainKey("comprobante");
        assertThat(confirm(attempt).path("comprobante")).isEqualTo(saved.path("comprobante"));
        verify(registration, times(2)).register(anyMap());
    }

    @Test
    void rejectsAnUnseenSlotAndAnotherSessionsAttempt() throws Exception {
        mvc.perform(post(BASE + "booking/prepare").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horarioId\":\"2\",\"nombre\":\"Ana\",\"correo\":\"ana@example.com\",\"motivo\":\"Control\"}"))
                .andExpect(status().isBadRequest());
        offer();
        String attempt = prepare().path("attemptId").asText();
        mvc.perform(post(BASE + "booking/confirm").session(new MockHttpSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("attemptId", attempt))))
                .andExpect(status().isConflict());
        verifyNoInteractions(registration);
    }

    @Test
    void reportsALostSlotAndPreservesTheActualStateOnReplay() throws Exception {
        offer();
        String attempt = prepare().path("attemptId").asText();
        when(registration.register(anyMap()))
                .thenReturn(new RegistrationClient.Result("horario_no_disponible", null, null));
        assertThat(confirm(attempt).path("message").asText()).contains("dejó de estar disponible");

        attempt = prepare().path("attemptId").asText();
        when(registration.register(anyMap()))
                .thenReturn(new RegistrationClient.Result("ya_registrada", ROW, "confirmada"));
        assertThat(confirm(attempt).path("estado").asText()).isEqualTo("confirmada");
    }

    @Test
    void reloadRecoversTheAttemptAndClosingTheSessionRemovesIt() throws Exception {
        offer();
        String attempt = prepare().path("attemptId").asText();
        mvc.perform(get(BASE + "booking/current").session(session))
                .andExpect(jsonPath("$.attemptId").value(attempt))
                .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(post(BASE + "booking/close").session(session)).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get(BASE + "booking/current").session(new MockHttpSession()))
                .andExpect(content().string(""));
    }

    @Test
    void concurrentConfirmationsDoNotRepeatAResolvedEffect() throws Exception {
        offer();
        String attempt = prepare().path("attemptId").asText();
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada", ROW, "pendiente"));
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> booking.confirm(session, attempt));
            var second = executor.submit(() -> booking.confirm(session, attempt));
            assertThat(first.get().solicitudId()).isEqualTo(ROW);
            assertThat(second.get().solicitudId()).isEqualTo(ROW);
            verify(registration, times(1)).register(anyMap());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void refusesAConfirmationWithoutAPreparedSummary() throws Exception {
        mvc.perform(post(BASE + "booking/confirm").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("attemptId", UUID.randomUUID().toString()))))
                .andExpect(status().isConflict());
        verifyNoInteractions(registration);
    }
}
