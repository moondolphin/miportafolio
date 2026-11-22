package moondolphin.miportafolio.asistente;

import java.net.SocketTimeoutException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RequestLookupTest {
    private static final String URL = "https://example.test/webhook/los-tilos/solicitudes-turno";
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final RegistrationClient client = new RegistrationClient(URL, "test-header", builder.build());
    private final Map<String, String> payload = Map.of("operacion", "consultar",
            "clave_idempotencia", "681a1f4d-680a-4d1d-96ed-fb418e313329",
            "correo", "demo@example.com", "comprobante_hash", "a".repeat(64));

    @Test
    void readsTheSameEndpointAgainAndValidatesTheStoredFields() {
        String body = """
                {"resultado":"encontrada","solicitud":{"id":"681a1f4d-680a-4d1d-96ed-fb418e313329",
                "horario_id":"2","nombre":"Demo","correo":"demo@example.com","motivo":"Control",
                "estado":"pendiente","inicio":"2026-11-13T20:00:00Z","fin":"2026-11-13T20:30:00Z"}}
                """;
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Los-Tilos-Key", "test-header"))
                .andExpect(jsonPath("$.operacion").value("consultar"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL)).andRespond(withSuccess(body.replace("pendiente", "confirmada"), MediaType.APPLICATION_JSON));
        var read = client.inspect(payload).solicitud();
        assertThat(read.estado()).isEqualTo("pendiente");
        assertThat(read.inicio()).isEqualTo("2026-11-13T17:00-03:00");
        assertThat(read.fin()).isEqualTo("2026-11-13T17:30-03:00");
        assertThat(client.inspect(payload).solicitud().estado()).isEqualTo("confirmada");
        server.verify();
    }

    @Test
    void anUnsupportedWorkflowOrTimeoutNeverBecomesACachedSuccess() {
        server.expect(requestTo(URL)).andRespond(withBadRequest().contentType(MediaType.APPLICATION_JSON)
                .body("{\"resultado\":\"datos_invalidos\"}"));
        server.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("test")));
        assertThatThrownBy(() -> client.inspect(payload)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> client.inspect(payload)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        server.verify();
    }

    @Test
    void returnsUniformNotFoundWithoutAnyStoredFields() {
        server.expect(requestTo(URL)).andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON).body("{\"resultado\":\"no_encontrada\"}"));
        assertThat(client.inspect(payload).solicitud()).isNull();
        server.verify();
    }
}
