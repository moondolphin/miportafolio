package moondolphin.miportafolio.asistente;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
class AssistantController {
    private final BookingConversation conversation;

    AssistantController(BookingConversation conversation) {
        this.conversation = conversation;
    }

    @PostMapping("/api/v1/public/assistant/chat")
    InstitutionalAssistant.ChatReply chat(@RequestBody ChatRequest request, HttpServletRequest http,
                                         jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        if (request.message() == null || request.message().isBlank() || request.message().length() > 1000) {
            throw new ResponseStatusException(BAD_REQUEST, "Ingresá una consulta de hasta 1000 caracteres.");
        }
        if (request.requestId() == null) return conversation.reply(http.getSession(), request.message().strip());
        try { java.util.UUID.fromString(request.requestId()); }
        catch (IllegalArgumentException invalid) { throw new ResponseStatusException(BAD_REQUEST, "Identificador de consulta inválido."); }
        return conversation.reply(http.getSession(), request.message().strip(), request.requestId(), request.retry());
    }

    @PostMapping("/api/v1/public/assistant/client-error")
    ResponseEntity<Void> clientError(@RequestBody Map<String, String> event) {
        String operation = event.getOrDefault("operation", "unknown");
        if (!java.util.Set.of("chat", "restore", "close").contains(operation)) operation = "unknown";
        org.slf4j.LoggerFactory.getLogger(AssistantController.class)
                .warn("asistente evento=error_cliente operacion={}", operation);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> chatError(ResponseStatusException exception, HttpServletRequest request) {
        String errorId = java.util.UUID.randomUUID().toString();
        org.slf4j.LoggerFactory.getLogger(AssistantController.class).warn(
                "asistente error_id={} evento=error http={} tipo={}", errorId,
                exception.getStatusCode().value(), exception.getClass().getSimpleName());
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("errorId", errorId);
        body.put("booking", conversation.currentAttempt(request.getSession(false)));
        body.put("error", exception.getReason());
        if (exception instanceof ToolCycleException cycle) body.put("diagnostico", Map.of("rutas", cycle.routes()));
        return ResponseEntity.status(exception.getStatusCode()).header("Cache-Control", "no-store")
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(Exception exception, HttpServletRequest request) {
        String id = java.util.UUID.randomUUID().toString();
        org.slf4j.LoggerFactory.getLogger(AssistantController.class).error(
                "asistente error_id={} evento=error_inesperado tipo={}", id, exception.getClass().getSimpleName());
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("error", "No pude completar la operación. Podés reintentar.");
        body.put("errorId", id);
        body.put("booking", conversation.currentAttempt(request.getSession(false)));
        return ResponseEntity.internalServerError().header("Cache-Control", "no-store").body(body);
    }

    record ChatRequest(String message, String requestId, boolean retry) {}
}
