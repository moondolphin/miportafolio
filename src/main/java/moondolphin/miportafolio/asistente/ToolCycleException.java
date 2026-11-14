package moondolphin.miportafolio.asistente;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Safe operational diagnostics: route codes only, never text, data or provider payloads. */
final class ToolCycleException extends ResponseStatusException {
    private final List<String> routes;
    ToolCycleException(List<String> routes) {
        super(HttpStatus.BAD_GATEWAY, "No pude completar la verificación de herramientas en este turno. Volvé a intentarlo.");
        this.routes = List.copyOf(routes);
    }
    List<String> routes() { return routes; }
}
