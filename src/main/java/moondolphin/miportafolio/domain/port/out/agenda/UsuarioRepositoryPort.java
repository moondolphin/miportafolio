package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsernameOrEmail(String username, String email);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAll();
    List<Usuario> findByApprovedFalse();
    Usuario save(Usuario usuario);
}
