package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AvailabilityIntegrationTest {
    private static final String URL = "https://example.test/webhook/los-tilos/disponibilidad";
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer webhook = MockRestServiceServer.bindTo(builder).build();
    private final OpenAiClient openAi = mock(OpenAiClient.class);
    private final InstitutionalAssistant institutional = mock(InstitutionalAssistant.class);

    private MockMvc chat(AvailabilityClient client) throws Exception {
        var json = new ObjectMapper();
        when(institutional.prompt()).thenReturn("V4");
        when(openAi.planDialogue(eq("V4"), anyString())).thenReturn(
                json.readTree("{\"herramientas\":[{\"nombre\":\"disponibilidad\",\"solicitud_id\":null}],\"intencion_turno\":true}"));
        when(openAi.writeDialogue(eq("V4"), anyString())).thenAnswer(invocation -> {
            var context = json.readTree(invocation.getArgument(1, String.class));
            var call = context.path("herramientas_turno").get(0);
            var slots = call.path("resultado").path("horarios");
            var result = json.createObjectNode();
            result.put("respuesta", !call.path("exito").asBoolean() ? call.path("resultado").path("error").asText()
                    : slots.isEmpty() ? "En este momento no hay horarios disponibles." : slots.get(0).path("label").asText());
            result.putArray("herramientas");
            result.putArray("acciones_anunciadas");
            result.put("usa_datos_dinamicos", call.path("exito").asBoolean());
            result.putArray("fuentes_dinamicas").add(call.path("id").asText());
            return result;
        });
        var booking = new BookingService(mock(RegistrationClient.class));
        return MockMvcBuilders.standaloneSetup(new AssistantController(
                new BookingConversation(booking, client, openAi, institutional))).build();
    }

    @Test
    void displaysOnlyWebhookTimesInBuenosAires() throws Exception {
        webhook.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Los-Tilos-Key", "test-key"))
                .andRespond(withSuccess("""
                        {"horarios":[{"id":2,"inicio":"2026-11-13T13:00:00Z","fin":"2026-11-13T13:30:00Z"}]}
                        """, MediaType.APPLICATION_JSON));
        chat(new AvailabilityClient(URL, "test-key", builder.build()))
                .perform(question()).andExpect(status().isOk())
                .andExpect(jsonPath("$.answer", containsString("13/11/2026 10:00 a 13/11/2026 10:30")))
                .andExpect(jsonPath("$.horarios[0].id").value("2"))
                .andExpect(jsonPath("$.retrieved").doesNotExist());
        webhook.verify();
        verify(institutional, never()).retrieve(anyString());
    }

    @Test
    void reportsAnEmptySchedule() throws Exception {
        webhook.expect(requestTo(URL)).andRespond(withSuccess("{\"horarios\":[]}", MediaType.APPLICATION_JSON));
        chat(new AvailabilityClient(URL, "test-key", builder.build())).perform(question())
                .andExpect(status().isOk()).andExpect(jsonPath("$.answer").value("En este momento no hay horarios disponibles."));
        webhook.verify();
    }

    @Test
    void reportsWebhookFailure() throws Exception {
        webhook.expect(requestTo(URL)).andRespond(withServerError());
        chat(new AvailabilityClient(URL, "test-key", builder.build())).perform(question())
                .andExpect(status().isOk()).andExpect(jsonPath("$.herramientas[0].exito").value(false));
        webhook.verify();
    }

    @Test
    void reportsWebhookTimeout() throws Exception {
        webhook.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("test")));
        chat(new AvailabilityClient(URL, "test-key", builder.build())).perform(question())
                .andExpect(status().isOk()).andExpect(jsonPath("$.herramientas[0].exito").value(false));
        webhook.verify();
    }

    @Test
    void distinguishesMissingConfigurationFromAnEmptySchedule() throws Exception {
        chat(new AvailabilityClient("", "", builder.build())).perform(question())
                .andExpect(status().isOk()).andExpect(jsonPath("$.herramientas[0].exito").value(false));
        webhook.verify();
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder question() {
        return post("/api/v1/public/assistant/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"¿Qué horarios tienen disponibles?\"}");
    }

    @Test
    void aWeekendRowFromN8nIsNeverOfferedAsValidAvailability() throws Exception {
        webhook.expect(requestTo(URL)).andRespond(withSuccess("""
                {"horarios":[{"id":2,"inicio":"2026-11-14T10:00:00-03:00","fin":"2026-11-14T10:30:00-03:00"}]}
                """, MediaType.APPLICATION_JSON));
        chat(new AvailabilityClient(URL, "test-key", builder.build())).perform(question())
                .andExpect(status().isOk()).andExpect(jsonPath("$.horarios").isEmpty());
        webhook.verify();
    }
}
