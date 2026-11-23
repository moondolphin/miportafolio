package moondolphin.miportafolio.asistente;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/public/assistant/booking")
class BookingController {
    private final BookingService booking;
    private final BookingConversation conversation;

    BookingController(BookingService booking, BookingConversation conversation) {
        this.booking = booking;
        this.conversation = conversation;
    }

    @PostMapping("/prepare")
    ResponseEntity<BookingService.View> prepare(@RequestBody BookingService.Details details,
                                               HttpServletRequest request) {
        var view = booking.prepare(request.getSession(), details);
        conversation.reviewShown(request.getSession(), view);
        return response(view);
    }

    @PostMapping("/confirm")
    ResponseEntity<BookingService.View> confirm(@RequestBody Confirmation confirmation,
                                               HttpServletRequest request) {
        return response(booking.confirm(request.getSession(), confirmation.attemptId()));
    }

    @GetMapping("/current")
    ResponseEntity<BookingService.View> current(HttpServletRequest request) {
        return response(booking.current(request.getSession(false)));
    }

    @GetMapping("/conversation")
    ResponseEntity<BookingConversation.Context> conversation(HttpServletRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(conversation.current(request.getSession(false)));
    }

    @PostMapping("/close")
    ResponseEntity<Void> close(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                session.invalidate();
            }
        }
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> error(ResponseStatusException exception) {
        org.slf4j.LoggerFactory.getLogger(BookingController.class).warn(
                "asistente evento=error_sesion http={} tipo={}", exception.getStatusCode().value(), exception.getClass().getSimpleName());
        return ResponseEntity.status(exception.getStatusCode()).cacheControl(CacheControl.noStore())
                .body(Map.of("error", exception.getReason()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> unexpected(Exception exception) {
        String id = java.util.UUID.randomUUID().toString();
        org.slf4j.LoggerFactory.getLogger(BookingController.class).error(
                "asistente error_id={} evento=error_sesion tipo={}", id, exception.getClass().getSimpleName());
        return ResponseEntity.internalServerError().cacheControl(CacheControl.noStore())
                .body(Map.of("error", "No pude completar la operación. Podés reintentar.", "errorId", id));
    }

    private static ResponseEntity<BookingService.View> response(BookingService.View view) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(view);
    }

    record Confirmation(String attemptId) {}
}
