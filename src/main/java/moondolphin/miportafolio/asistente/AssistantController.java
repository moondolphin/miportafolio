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
        return conversation.reply(http.getSession(), request.message().strip());
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> chatError(ResponseStatusException exception) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("error", exception.getReason());
        if (exception instanceof ToolCycleException cycle) body.put("diagnostico", Map.of("rutas", cycle.routes()));
        return ResponseEntity.status(exception.getStatusCode()).header("Cache-Control", "no-store")
                .body(body);
    }

    record ChatRequest(String message) {}
}
