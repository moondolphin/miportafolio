package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.bootstrap.security.JwtTokenProvider;
import moondolphin.miportafolio.domain.exception.ForbiddenException;
import moondolphin.miportafolio.domain.exception.InvalidCredentialsException;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Role;
import moondolphin.miportafolio.domain.model.agenda.Usuario;
import moondolphin.miportafolio.domain.port.out.agenda.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarioRepository, passwordEncoder, tokenProvider);
    }

    private Usuario usuarioAprobadoActivo() {
        return new Usuario(1L, "yesica", "yesica@mail.com", "hash123",
                Role.USER, true, true, LocalDateTime.now(), LocalDateTime.now());
    }

    // --- login ---

    @Test
    void login_conCredencialesValidas_retornaToken() {
        // Arrange
        Usuario usuario = usuarioAprobadoActivo();
        when(usuarioRepository.findByUsernameOrEmail("yesica", "yesica")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hash123")).thenReturn(true);
        when(tokenProvider.generarToken("yesica", "USER", 1L)).thenReturn("jwt-token");

        // Act
        String token = service.login("yesica", "password");

        // Assert
        assertThat(token).isEqualTo("jwt-token");
        verify(tokenProvider).generarToken("yesica", "USER", 1L);
    }

    @Test
    void login_conUsuarioInexistente_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByUsernameOrEmail("nadie", "nadie")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.login("nadie", "pass"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales inválidas");
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void login_conPasswordIncorrecta_lanzaExcepcion() {
        // Arrange
        Usuario usuario = usuarioAprobadoActivo();
        when(usuarioRepository.findByUsernameOrEmail("yesica", "yesica")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "hash123")).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> service.login("yesica", "mala"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales inválidas");
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void login_conUsuarioPendienteDeAprobacion_lanzaExcepcion() {
        // Arrange
        Usuario usuario = new Usuario(2L, "pendiente", "p@mail.com", "hash",
                Role.USER, false, true, LocalDateTime.now(), LocalDateTime.now());
        when(usuarioRepository.findByUsernameOrEmail("pendiente", "pendiente")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.login("pendiente", "pass"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Usuario pendiente de aprobación");
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void login_conUsuarioInactivo_lanzaExcepcion() {
        // Arrange
        Usuario usuario = new Usuario(3L, "inactivo", "i@mail.com", "hash",
                Role.USER, true, false, LocalDateTime.now(), LocalDateTime.now());
        when(usuarioRepository.findByUsernameOrEmail("inactivo", "inactivo")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.login("inactivo", "pass"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Usuario inactivo");
        verifyNoInteractions(tokenProvider);
    }

    // --- cambiarPassword ---

    @Test
    void cambiarPassword_conDatosCorrectos_actualizaPasswordHash() {
        // Arrange
        Usuario usuario = usuarioAprobadoActivo();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actual", "hash123")).thenReturn(true);
        when(passwordEncoder.encode("nueva")).thenReturn("nuevo-hash");

        // Act
        service.cambiarPassword(1L, "actual", "nueva");

        // Assert
        assertThat(usuario.getPasswordHash()).isEqualTo("nuevo-hash");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPassword_conUsuarioInexistente_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.cambiarPassword(99L, "pass", "nueva"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuario no encontrado");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPassword_conPasswordActualIncorrecta_lanzaExcepcion() {
        // Arrange
        Usuario usuario = usuarioAprobadoActivo();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash123")).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> service.cambiarPassword(1L, "incorrecta", "nueva"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Contraseña actual incorrecta");
        verify(usuarioRepository, never()).save(any());
    }
}
