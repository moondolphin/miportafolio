package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
class AvailabilityClient {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityClient.class);
    private static final ZoneId BUENOS_AIRES = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final String UNAVAILABLE = "No pude consultar la disponibilidad en este momento. Volvé a intentar más tarde.";

    private final RestClient client;
    private final String url;
    private final String key;

    @Autowired
    AvailabilityClient(@Value("${asistente.n8n.disponibilidad-url}") String url,
                       @Value("${asistente.n8n.disponibilidad-key}") String key) {
        this(url, key, createClient());
    }

    AvailabilityClient(String url, String key, RestClient client) {
        this.url = url;
        this.key = key;
        this.client = client;
    }

    List<Slot> availableSlots() {
        if (url.isBlank() || key.isBlank()) {
            LOG.warn("n8n operacion=disponibilidad evento=no_enviada causa=configuracion_ausente");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, UNAVAILABLE);
        }
        String trace = java.util.UUID.randomUUID().toString();
        long started = System.nanoTime();
        int[] http = {0};
        String outcome = "error";
        LOG.info("n8n id={} operacion=disponibilidad evento=inicio metodo=GET filtro_fecha=sin_parametro", trace);
        try {
            URI webhook = URI.create(url.strip());
            if (!"https".equalsIgnoreCase(webhook.getScheme()) || webhook.getHost() == null
                    || webhook.getUserInfo() != null || webhook.getFragment() != null) {
                throw badGateway();
            }
            JsonNode response = client.get().uri(webhook)
                    .header("X-Los-Tilos-Key", key.strip())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> true, (request, received) -> {
                        http[0] = received.getStatusCode().value();
                        if (!received.getStatusCode().is2xxSuccessful()) throw badGateway();
                    })
                    .body(JsonNode.class);
            var slots = parseSlots(response);
            LOG.info("n8n id={} operacion=disponibilidad recibidos={} aceptados={} recibidos_por_fecha={} aceptados_por_fecha={} zona=America/Argentina/Buenos_Aires",
                    trace, response.path("horarios").size(), slots.size(), dates(response.path("horarios")), dates(slots));
            outcome = "ok";
            return slots;
        } catch (RestClientException | IllegalArgumentException | DateTimeParseException exception) {
            LOG.warn("n8n id={} operacion=disponibilidad tipo_error={}", trace, exception.getClass().getSimpleName());
            throw badGateway();
        } finally {
            LOG.info("n8n id={} operacion=disponibilidad evento=fin http={} resultado={} duracion_ms={}",
                    trace, http[0] == 0 ? "sin_respuesta" : Integer.toString(http[0]), outcome,
                    java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
        }
    }

    // Log only counts and public slot dates, never response bodies, headers or credentials.
    private static Object dates(JsonNode rows) {
        try {
            var counts = new java.util.TreeMap<String, Integer>();
            for (var row : rows) counts.merge(parseTime(row.path("inicio")).toLocalDate().toString(), 1, Integer::sum);
            return counts;
        } catch (RuntimeException exception) { return "no_disponible"; }
    }

    private static Object dates(List<Slot> slots) {
        try {
            var counts = new java.util.TreeMap<String, Integer>();
            for (var slot : slots) counts.merge(OffsetDateTime.parse(slot.inicio()).toLocalDate().toString(), 1, Integer::sum);
            return counts;
        } catch (RuntimeException exception) { return "no_disponible"; }
    }

    private static List<Slot> parseSlots(JsonNode response) {
        JsonNode times = response == null ? null : response.path("horarios");
        if (times == null || !times.isArray()) {
            throw badGateway();
        }
        if (times.isEmpty()) {
            return List.of();
        }
        var slots = new ArrayList<Slot>();
        var ids = new HashSet<String>();
        for (var time : times) {
            if (!time.path("id").isIntegralNumber() && !time.path("id").isTextual()) {
                throw badGateway();
            }
            String id = Long.toString(Long.parseLong(time.path("id").asText()));
            if (Long.parseLong(id) <= 0 || !ids.add(id)) {
                throw badGateway();
            }
            var start = parseTime(time.path("inicio"));
            var end = parseTime(time.path("fin"));
            if (!end.isAfter(start)) {
                throw badGateway();
            }
            slots.add(new Slot(id, DISPLAY.format(start) + " a " + DISPLAY.format(end),
                    start.atZone(BUENOS_AIRES).toOffsetDateTime().toString(),
                    end.atZone(BUENOS_AIRES).toOffsetDateTime().toString()));
        }
        return slots.stream().filter(InstitutionalCalendar.current()::permits).toList();
    }

    record Slot(String id, String label, String inicio, String fin) {
        Slot(String id, String label) {
            this(id, label, null, null);
        }
    }

    private static LocalDateTime parseTime(JsonNode value) {
        if (!value.isTextual()) {
            throw badGateway();
        }
        var time = DateTimeFormatter.ISO_DATE_TIME.parseBest(value.textValue().strip().replace(' ', 'T'),
                OffsetDateTime::from, LocalDateTime::from);
        if (time instanceof OffsetDateTime offsetTime) {
            return offsetTime.atZoneSameInstant(BUENOS_AIRES).toLocalDateTime();
        }
        return LocalDateTime.from(time);
    }

    static RestClient createClient() {
        var factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String method) throws IOException {
                super.prepareConnection(connection, method);
                connection.setInstanceFollowRedirects(false);
            }
        };
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(8));
        return RestClient.builder().requestFactory(factory).build();
    }

    private static ResponseStatusException badGateway() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, UNAVAILABLE);
    }
}
