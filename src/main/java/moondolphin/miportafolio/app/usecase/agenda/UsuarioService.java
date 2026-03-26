package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.ConflictException;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Role;
import moondolphin.miportafolio.domain.model.agenda.Usuario;
import moondolphin.miportafolio.domain.port.in.agenda.UsuarioServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService implements UsuarioServicePort {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrar(String username, String email, String password) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new ConflictException("El username ya está en uso");
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("El email ya está en uso");
        }
        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setEmail(email);
        nuevo.setPasswordHash(passwordEncoder.encode(password));
        nuevo.setRole(Role.USER);
        nuevo.setApproved(false);
        nuevo.setActive(true);
        nuevo.setCreatedAt(LocalDateTime.now());
        nuevo.setUpdatedAt(LocalDateTime.now());
        return usuarioRepository.save(nuevo);
    }

    @Override
    public List<Usuario> obtenerPendientes() {
        return usuarioRepository.findByApprovedFalse();
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public void aprobar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuario.setApproved(true);
        usuario.setUpdatedAt(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Override
    public void rechazar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuario.setApproved(false);
        usuario.setActive(false);
        usuario.setUpdatedAt(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
}
