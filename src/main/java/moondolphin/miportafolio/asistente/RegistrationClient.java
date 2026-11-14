package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
class RegistrationClient {
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationClient.class);
    private final String url;
    private final String key;
    private final RestClient client;

    @Autowired
    RegistrationClient(@Value("${asistente.n8n.registro-url}") String url,
                       @Value("${asistente.n8n.registro-key}") String key) {
        this(url, key, AvailabilityClient.createClient());
    }

    RegistrationClient(String url, String key, RestClient client) {
        this.url = url;
        this.key = key;
        this.client = client;
    }

    void requireConfiguration() {
        if (url.isBlank() || key.isBlank()) {
            LOG.warn("n8n operacion=solicitudes evento=no_enviada causa=configuracion_ausente");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El registro no está configurado en el servidor. Podés conservar el resumen e intentar más tarde.");
        }
        try {
            var uri = URI.create(url.strip());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La URL de registro del servidor es inválida.");
        }
    }

    Result register(Map<String, String> request) {
        String trace = UUID.randomUUID().toString();
        long started = System.nanoTime();
        int[] http = {0};
        String receivedResult = "sin_resultado";
        LOG.info("n8n id={} operacion=registrar evento=inicio metodo=POST", trace);
        try {
            var response = client.post().uri(URI.create(url.strip()))
                    .header("X-Los-Tilos-Key", key.strip())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request).retrieve()
                    .onStatus(status -> true, (sent, received) -> { http[0] = received.getStatusCode().value(); })
                    .toEntity(JsonNode.class);
            JsonNode body = response.getBody();
            receivedResult = loggedResult(body);
            if (body == null || !body.isObject()) {
                return Result.unknown();
            }
            String result = body.path("resultado").asText();
            int status = response.getStatusCode().value();
            if ((status == 201 && "registrada".equals(result))
                    || (status == 200 && "ya_registrada".equals(result))) {
                UUID id = UUID.fromString(body.path("solicitud_id").asText());
                String state = body.path("estado").asText();
                if (!java.util.Set.of("pendiente", "confirmada", "cancelada").contains(state)
                        || ("registrada".equals(result) && !"pendiente".equals(state))) {
                    return Result.unknown();
                }
                return new Result(result, id.toString(), state);
            }
            if ((status == 400 && "datos_invalidos".equals(result))
                    || (status == 409 && java.util.Set.of("horario_no_disponible", "conflicto_idempotencia").contains(result))) {
                return new Result(result, null, null);
            }
        } catch (RestClientException | IllegalArgumentException exception) {
            LOG.warn("n8n id={} operacion=registrar tipo_error={}", trace, exception.getClass().getSimpleName());
            return Result.unknown();
        } finally {
            LOG.info("n8n id={} operacion=registrar evento=fin http={} resultado_n8n={} duracion_ms={}",
                    trace, http[0] == 0 ? "sin_respuesta" : Integer.toString(http[0]), receivedResult,
                    java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
        }
        return Result.unknown();
    }

    LookupResult inspect(Map<String, String> request) {
        requireConfiguration();
        String trace = UUID.randomUUID().toString();
        long started = System.nanoTime();
        int[] http = {0};
        String receivedResult = "sin_resultado";
        LOG.info("n8n id={} operacion=consultar_solicitud evento=inicio metodo=POST", trace);
        try {
            var response = client.post().uri(URI.create(url.strip()))
                    .header("X-Los-Tilos-Key", key.strip())
                    .contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
                    .onStatus(status -> true, (sent, received) -> { http[0] = received.getStatusCode().value(); }).toEntity(JsonNode.class);
            JsonNode body = response.getBody();
            receivedResult = loggedResult(body);
            if (body != null && response.getStatusCode().value() == 404
                    && "no_encontrada".equals(body.path("resultado").asText())) {
                return new LookupResult("no_encontrada", null);
            }
            if (body == null || !response.getStatusCode().is2xxSuccessful()
                    || !"encontrada".equals(body.path("resultado").asText())) {
                throw lookupUnavailable();
            }
            var row = body.path("solicitud");
            String id = UUID.fromString(row.path("id").asText()).toString();
            String state = row.path("estado").asText();
            String slot = row.path("horario_id").asText();
            if (!java.util.Set.of("pendiente", "confirmada", "cancelada").contains(state)
                    || Long.parseLong(slot) <= 0 || !row.path("nombre").isTextual()
                    || !row.path("correo").isTextual() || !row.path("motivo").isTextual()) {
                throw lookupUnavailable();
            }
            var start = java.time.OffsetDateTime.parse(row.path("inicio").asText())
                    .atZoneSameInstant(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).toOffsetDateTime();
            var end = java.time.OffsetDateTime.parse(row.path("fin").asText())
                    .atZoneSameInstant(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).toOffsetDateTime();
            return new LookupResult("encontrada", new StoredRequest(id, slot,
                    row.path("nombre").asText(), row.path("correo").asText(), row.path("motivo").asText(),
                    state, start.toString(), end.toString()));
        } catch (RestClientException | IllegalArgumentException | java.time.format.DateTimeParseException exception) {
            LOG.warn("n8n id={} operacion=consultar_solicitud tipo_error={}", trace, exception.getClass().getSimpleName());
            throw lookupUnavailable();
        } finally {
            LOG.info("n8n id={} operacion=consultar_solicitud evento=fin http={} resultado_n8n={} duracion_ms={}",
                    trace, http[0] == 0 ? "sin_respuesta" : Integer.toString(http[0]), receivedResult,
                    java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
        }
    }

    private static String loggedResult(JsonNode body) {
        if (body == null) return "cuerpo_ausente";
        String result = body.path("resultado").asText();
        return java.util.Set.of("registrada", "ya_registrada", "datos_invalidos", "horario_no_disponible",
                "conflicto_idempotencia", "encontrada", "no_encontrada", "error_interno").contains(result)
                ? result : "no_reconocido";
    }

    private static ResponseStatusException lookupUnavailable() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "No pude consultar los datos actuales de la solicitud en el sistema.");
    }

    record LookupResult(String resultado, StoredRequest solicitud) {}
    record StoredRequest(String id, String horarioId, String nombre, String correo,
                         String motivo, String estado, String inicio, String fin) {}

    record Result(String result, String id, String state) {
        static Result unknown() {
            return new Result("indeterminado", null, null);
        }
    }
}
