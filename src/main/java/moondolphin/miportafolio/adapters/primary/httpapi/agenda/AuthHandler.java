package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.CambiarPasswordRequest;
import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.LoginRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.port.in.agenda.AuthServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthHandler {

    private final AuthServicePort authService;

    public AuthHandler(AuthServicePort authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getUsernameOrEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada. Eliminá el token del cliente."));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @AuthenticationPrincipal AgendaUserDetails userDetails,
            @RequestBody CambiarPasswordRequest request) {
        authService.cambiarPassword(userDetails.getUserId(),
                request.getPasswordActual(), request.getPasswordNueva());
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada"));
    }
}
