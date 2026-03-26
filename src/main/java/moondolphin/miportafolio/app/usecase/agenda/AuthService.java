package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.bootstrap.security.JwtTokenProvider;
import moondolphin.miportafolio.domain.exception.ForbiddenException;
import moondolphin.miportafolio.domain.exception.InvalidCredentialsException;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Usuario;
import moondolphin.miportafolio.domain.port.in.agenda.AuthServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthServicePort {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UsuarioRepositoryPort usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String login(String usernameOrEmail, String password) {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
        if (!usuario.isApproved()) {
            throw new ForbiddenException("Usuario pendiente de aprobación");
        }
        if (!usuario.isActive()) {
            throw new ForbiddenException("Usuario inactivo");
        }

        return tokenProvider.generarToken(usuario.getUsername(), usuario.getRole().name(), usuario.getId());
    }

    @Override
    public void cambiarPassword(Long userId, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Contraseña actual incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }
}
