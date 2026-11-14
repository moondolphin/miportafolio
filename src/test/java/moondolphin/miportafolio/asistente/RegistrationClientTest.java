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

class RegistrationClientTest {
    private static final String URL = "https://example.test/webhook/los-tilos/solicitudes-turno";
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final RegistrationClient client = new RegistrationClient(URL, "fake-key", builder.build());
    private final Map<String, String> payload = Map.of(
            "horario_id", "2", "nombre", "Ana Demo", "correo", "ana@example.com", "motivo", "Control",
            "clave_idempotencia", "681a1f4d-680a-4d1d-96ed-fb418e313329", "comprobante_hash", "a".repeat(64));

    @Test
    void postsTheHashAndAcceptsOnlyTheExpectedSuccessfulContract() {
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Los-Tilos-Key", "fake-key"))
                .andExpect(jsonPath("$.comprobante_hash").value("a".repeat(64)))
                .andExpect(jsonPath("$.horario_id").value("2"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"resultado\":\"registrada\",\"solicitud_id\":\"681a1f4d-680a-4d1d-96ed-fb418e313329\",\"estado\":\"pendiente\"}"));
        assertThat(client.register(payload).state()).isEqualTo("pendiente");
        server.verify();
    }

    @Test
    void aTimeoutIsIndeterminateAndNeverReportedAsARejectedWrite() {
        server.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("test")));
        assertThat(client.register(payload).result()).isEqualTo("indeterminado");
        server.verify();
    }

    @Test
    void malformedSuccessfulResponseIsAlsoIndeterminate() {
        server.expect(requestTo(URL)).andRespond(withSuccess("{\"respuesta\":{}}", MediaType.APPLICATION_JSON));
        assertThat(client.register(payload).result()).isEqualTo("indeterminado");
        server.verify();
    }

    @Test
    void missingConfigurationFailsBeforeSendingAnyRequest() {
        var missing = new RegistrationClient("", "", builder.build());
        assertThatThrownBy(missing::requireConfiguration).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        server.verify();
    }
}
