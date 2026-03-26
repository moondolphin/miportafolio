package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.RegistroRequest;
import moondolphin.miportafolio.domain.model.agenda.Usuario;
import moondolphin.miportafolio.domain.port.in.agenda.UsuarioServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UsuarioHandler {

    private final UsuarioServicePort usuarioService;

    public UsuarioHandler(UsuarioServicePort usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/api/v1/users/register")
    public ResponseEntity<Map<String, String>> registrar(@RequestBody RegistroRequest request) {
        usuarioService.registrar(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("mensaje", "Registro exitoso. Pendiente de aprobación."));
    }

    @GetMapping("/api/v1/admin/users/pending")
    public List<Usuario> obtenerPendientes() {
        return usuarioService.obtenerPendientes();
    }

    @GetMapping("/api/v1/admin/users")
    public List<Usuario> obtenerTodos() {
        return usuarioService.obtenerTodos();
    }

    @PutMapping("/api/v1/admin/users/{id}/approve")
    public ResponseEntity<Map<String, String>> aprobar(@PathVariable Long id) {
        usuarioService.aprobar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario aprobado"));
    }

    @PutMapping("/api/v1/admin/users/{id}/reject")
    public ResponseEntity<Map<String, String>> rechazar(@PathVariable Long id) {
        usuarioService.rechazar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario rechazado"));
    }
}
