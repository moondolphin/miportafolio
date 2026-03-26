package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.ConflictException;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Role;
import moondolphin.miportafolio.domain.model.agenda.Usuario;
import moondolphin.miportafolio.domain.port.out.agenda.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    // --- registrar ---

    @Test
    void registrar_conDatosNuevos_creaUsuarioPendienteDeAprobacion() {
        // Arrange
        when(usuarioRepository.findByUsername("yesica")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("y@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hash");
        Usuario guardado = new Usuario(1L, "yesica", "y@mail.com", "hash",
                Role.USER, false, true, LocalDateTime.now(), LocalDateTime.now());
        when(usuarioRepository.save(any())).thenReturn(guardado);
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        // Act
        Usuario resultado = service.registrar("yesica", "y@mail.com", "pass");

        // Assert
        verify(usuarioRepository).save(captor.capture());
        Usuario capturado = captor.getValue();
        assertThat(capturado.getUsername()).isEqualTo("yesica");
        assertThat(capturado.getEmail()).isEqualTo("y@mail.com");
        assertThat(capturado.getPasswordHash()).isEqualTo("hash");
        assertThat(capturado.getRole()).isEqualTo(Role.USER);
        assertThat(capturado.isApproved()).isFalse();
        assertThat(capturado.isActive()).isTrue();
        assertThat(capturado.getCreatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void registrar_conUsernameDuplicado_lanzaExcepcionSinGuardar() {
        // Arrange
        when(usuarioRepository.findByUsername("yesica")).thenReturn(Optional.of(new Usuario()));

        // Act + Assert
        assertThatThrownBy(() -> service.registrar("yesica", "y@mail.com", "pass"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El username ya está en uso");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_conEmailDuplicado_lanzaExcepcionSinGuardar() {
        // Arrange
        when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("y@mail.com")).thenReturn(Optional.of(new Usuario()));

        // Act + Assert
        assertThatThrownBy(() -> service.registrar("nuevo", "y@mail.com", "pass"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El email ya está en uso");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_verificaUsernameAntesQueEmail() {
        // Arrange — username duplicado: no debe ni consultar el email
        when(usuarioRepository.findByUsername("yesica")).thenReturn(Optional.of(new Usuario()));

        // Act + Assert
        assertThatThrownBy(() -> service.registrar("yesica", "y@mail.com", "pass"))
                .hasMessage("El username ya está en uso");
        verify(usuarioRepository, never()).findByEmail(any());
    }

    // --- obtenerPendientes ---

    @Test
    void obtenerPendientes_retornaUsuariosSinAprobar() {
        // Arrange
        List<Usuario> pendientes = List.of(new Usuario(), new Usuario());
        when(usuarioRepository.findByApprovedFalse()).thenReturn(pendientes);

        // Act
        List<Usuario> resultado = service.obtenerPendientes();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(usuarioRepository).findByApprovedFalse();
    }

    @Test
    void obtenerPendientes_sinPendientes_retornaListaVacia() {
        // Arrange
        when(usuarioRepository.findByApprovedFalse()).thenReturn(List.of());

        // Act
        List<Usuario> resultado = service.obtenerPendientes();

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- obtenerTodos ---

    @Test
    void obtenerTodos_retornaTodosLosUsuarios() {
        // Arrange
        List<Usuario> todos = List.of(new Usuario(), new Usuario(), new Usuario());
        when(usuarioRepository.findAll()).thenReturn(todos);

        // Act
        List<Usuario> resultado = service.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(3);
    }

    @Test
    void obtenerTodos_sinUsuarios_retornaListaVacia() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodos()).isEmpty();
    }

    // --- aprobar ---

    @Test
    void aprobar_conIdExistente_setApprovedTrueYGuarda() {
        // Arrange
        Usuario usuario = new Usuario(1L, "yesica", "y@mail.com", "hash",
                Role.USER, false, true, LocalDateTime.now(), LocalDateTime.now());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        service.aprobar(1L);

        // Assert
        assertThat(usuario.isApproved()).isTrue();
        assertThat(usuario.getUpdatedAt()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void aprobar_conIdInexistente_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.aprobar(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuario no encontrado");
        verify(usuarioRepository, never()).save(any());
    }

    // --- rechazar ---

    @Test
    void rechazar_conIdExistente_setApprovedYActiveFalseYGuarda() {
        // Arrange
        Usuario usuario = new Usuario(1L, "yesica", "y@mail.com", "hash",
                Role.USER, true, true, LocalDateTime.now(), LocalDateTime.now());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        service.rechazar(1L);

        // Assert
        assertThat(usuario.isApproved()).isFalse();
        assertThat(usuario.isActive()).isFalse();
        assertThat(usuario.getUpdatedAt()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rechazar_conIdInexistente_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.rechazar(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuario no encontrado");
        verify(usuarioRepository, never()).save(any());
    }
}
