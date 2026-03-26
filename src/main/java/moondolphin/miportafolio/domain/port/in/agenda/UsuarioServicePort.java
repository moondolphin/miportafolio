package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.Usuario;
import java.util.List;

public interface UsuarioServicePort {
    Usuario registrar(String username, String email, String password);
    List<Usuario> obtenerPendientes();
    List<Usuario> obtenerTodos();
    void aprobar(Long id);
    void rechazar(Long id);
}
