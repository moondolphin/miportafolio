package moondolphin.miportafolio.asistente;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
class BookingService {
    private static final String SESSION_STATE = BookingService.class.getName();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SUBJECT_TO_CONFIRMATION =
            "Registrar la solicitud queda sujeto a confirmación por recepción.";
    private final RegistrationClient registration;

    BookingService(RegistrationClient registration) {
        this.registration = registration;
    }

    void remember(HttpSession session, List<AvailabilityClient.Slot> slots) {
        synchronized (session) {
            state(session).slots = slots.stream().filter(InstitutionalCalendar.current()::permits).toList();
        }
    }

    View prepare(HttpSession session, Details submitted) {
        synchronized (session) {
            var state = state(session);
            if (state.attempt != null && "indeterminado".equals(state.attempt.stage)) {
                throw new ResponseStatusException(CONFLICT,
                        "Primero verificá el intento anterior para evitar enviar otra solicitud.");
            }
            var details = validated(submitted);
            var slot = state.slots.stream().filter(item -> item.id().equals(details.horarioId()))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Consultá disponibilidad y elegí uno de los horarios devueltos."));
            if (state.attempt == null || !"resumen".equals(state.attempt.stage)
                    || !state.attempt.details.equals(details)) {
                state.attempt = new Attempt(details, slot.label());
                state.attempts.put(state.attempt.id, state.attempt);
            }
            View view = state.attempt.view();
            BookingConversation.sync(session, view);
            return view;
        }
    }

    View confirm(HttpSession session, String attemptId) {
        synchronized (session) {
            var attempt = state(session).attempt;
            if (attempt == null || !attempt.id.equals(attemptId)) {
                throw new ResponseStatusException(CONFLICT,
                        "El resumen venció o pertenece a otra sesión. Volvé a consultar disponibilidad.");
            }

            // Duplicate submissions of a resolved attempt do not execute another effect.
            // Current external state must be read through inspect, not inferred from this local replay.
            if (!"resumen".equals(attempt.stage) && !"indeterminado".equals(attempt.stage)) return attempt.view();
            if ("resumen".equals(attempt.stage) && state(session).slots.stream().noneMatch(slot ->
                    slot.id().equals(attempt.details.horarioId()) && InstitutionalCalendar.current().permits(slot))) {
                throw new ResponseStatusException(CONFLICT, "El horario preparado ya no cumple las políticas institucionales.");
            }
            registration.requireConfiguration();
            if ("indeterminado".equals(attempt.stage)) {
                var lookup = inspect(session, null);
                if ("encontrada".equals(lookup.resultado())) return attempt.view();
                if (!"no_encontrada".equals(lookup.resultado())) {
                    throw new ResponseStatusException(CONFLICT, "No se pudo resolver el intento anterior. No se reenvió.");
                }
            }
            // Mark the result as uncertain before sending. A retry reuses this exact payload.
            attempt.stage = "indeterminado";
            var result = registration.register(attempt.payload());
            attempt.result = result;
            attempt.stage = switch (result.result()) {
                case "registrada", "ya_registrada" -> "registrada";
                case "datos_invalidos", "horario_no_disponible", "conflicto_idempotencia" -> "rechazada";
                default -> "indeterminado";
            };
            if ("registrada".equals(attempt.stage)) state(session).lastRegistered = attempt;
            BookingConversation.sync(session, attempt.view());
            return attempt.view();
        }
    }

    RegistrationClient.LookupResult inspect(HttpSession session, String requestId) {
        synchronized (session) {
            State state = state(session);
            Attempt attempt;
            if (requestId == null || requestId.isBlank()) {
                attempt = state.attempt != null && !"resumen".equals(state.attempt.stage)
                        ? state.attempt : state.lastRegistered;
            } else {
                attempt = state.attempts.values().stream().filter(item -> item.result != null
                        && requestId.equals(item.result.id())).findFirst().orElse(null);
            }
            if (attempt == null) {
                throw new ResponseStatusException(CONFLICT,
                        "No hay una solicitud de esta sesión que pueda verificarse con ese identificador.");
            }
            var result = registration.inspect(Map.of("operacion", "consultar",
                    "clave_idempotencia", attempt.id, "correo", attempt.details.correo(),
                    "comprobante_hash", attempt.receiptHash));
            if (result.solicitud() != null) {
                if (attempt.result != null && attempt.result.id() != null
                        && !attempt.result.id().equals(result.solicitud().id())) {
                    throw new ResponseStatusException(CONFLICT, "La respuesta no corresponde a la solicitud consultada.");
                }
                attempt.result = new RegistrationClient.Result("ya_registrada",
                        result.solicitud().id(), result.solicitud().estado());
                attempt.stage = "registrada";
                if (state.attempt == attempt) {
                    state.lastRegistered = attempt;
                    BookingConversation.sync(session, attempt.view());
                }
            }
            return result;
        }
    }

    List<Map<String, String>> knownRequests(HttpSession session) {
        synchronized (session) {
            return state(session).attempts.values().stream()
                    .filter(attempt -> attempt.result != null && attempt.result.id() != null)
                    .map(attempt -> Map.of("solicitud_id", attempt.result.id(),
                            "estado_observado_anteriormente", attempt.result.state()))
                    .toList();
        }
    }

    View current(HttpSession session) {
        if (session == null) {
            return null;
        }
        synchronized (session) {
            var state = (State) session.getAttribute(SESSION_STATE);
            return state == null || state.attempt == null ? null : state.attempt.view();
        }
    }

    void clearCompleted(HttpSession session) {
        synchronized (session) {
            State state = state(session);
            if (state.attempt != null && "registrada".equals(state.attempt.stage)) {
                state.attempt = null;
            }
        }
    }

    void discardDraft(HttpSession session) {
        synchronized (session) {
            State state = state(session);
            if (state.attempt != null && ("resumen".equals(state.attempt.stage)
                    || "rechazada".equals(state.attempt.stage))) {
                state.attempt = null;
            }
        }
    }

    private static State state(HttpSession session) {
        session.setMaxInactiveInterval(30 * 60);
        var state = (State) session.getAttribute(SESSION_STATE);
        if (state == null) {
            state = new State();
            session.setAttribute(SESSION_STATE, state);
        }
        return state;
    }

    private static Details validated(Details submitted) {
        if (submitted == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Completá los datos de la solicitud.");
        }
        String id = clean(submitted.horarioId());
        String name = clean(submitted.nombre());
        String email = clean(submitted.correo());
        String reason = clean(submitted.motivo());
        try {
            if (Long.parseLong(id) <= 0 || !Long.toString(Long.parseLong(id)).equals(id)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(BAD_REQUEST, "El horario elegido es inválido.");
        }
        if (name.isBlank() || name.length() > 120 || reason.isBlank() || reason.length() > 500
                || email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+[.][^\\s@]+$")) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Revisá nombre (hasta 120 caracteres), correo y motivo (hasta 500 caracteres).");
        }
        return new Details(id, name, email, reason);
    }

    private static String clean(String value) {
        return value == null ? "" : value.strip();
    }

    private static String hash(String receipt) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(receipt.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }

    record Details(String horarioId, String nombre, String correo, String motivo) {}
    record View(String attemptId, String stage, Details details, String horario,
                String message, String solicitudId, String estado, String comprobante) {}

    private static class State {
        private List<AvailabilityClient.Slot> slots = List.of();
        private Attempt attempt;
        private Attempt lastRegistered;
        private final Map<String, Attempt> attempts = new java.util.LinkedHashMap<>();
    }

    private static class Attempt {
        private final String id = UUID.randomUUID().toString();
        private final Details details;
        private final String label;
        private final String receipt;
        private final String receiptHash;
        private String stage = "resumen";
        private RegistrationClient.Result result;

        Attempt(Details details, String label) {
            this.details = details;
            this.label = label;
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            receipt = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            receiptHash = hash(receipt);
        }

        Map<String, String> payload() {
            return Map.of("operacion", "registrar", "horario_id", details.horarioId(), "nombre", details.nombre(),
                    "correo", details.correo(), "motivo", details.motivo(),
                    "clave_idempotencia", id, "comprobante_hash", receiptHash);
        }

        View view() {
            String message = switch (stage) {
                case "resumen" -> "Revisá los datos antes de confirmar el envío. " + SUBJECT_TO_CONFIRMATION;
                case "registrada" -> switch (result.state()) {
                    case "confirmada" -> "El sistema informa que el turno está confirmado por recepción.";
                    case "cancelada" -> "El sistema informa que la solicitud está cancelada.";
                    default -> "Solicitud registrada. El turno queda pendiente de confirmación por recepción.";
                };
                case "rechazada" -> switch (result.result()) {
                    case "horario_no_disponible" -> "El horario dejó de estar disponible. Consultá las opciones nuevamente.";
                    case "conflicto_idempotencia" -> "El intento tiene datos distintos de los registrados. Contactá a recepción.";
                    default -> "La solicitud fue rechazada por datos inválidos. Revisá el formulario.";
                };
                default -> "Todavía no pude verificar si se registró la solicitud. Usá Verificar o reintentar para resolver este mismo intento.";
            };
            boolean saved = "registrada".equals(stage);
            return new View(id, stage, details, label, message,
                    saved ? result.id() : null, saved ? result.state() : null, saved ? receipt : null);
        }
    }
}
