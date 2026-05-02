package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.CollageEntry;
import java.util.List;
import java.util.Optional;

public interface CollageServicePort {
    List<CollageEntry> obtenerCollageDelUsuario(Long userId);
    Optional<CollageEntry> obtenerCollagePropioPorId(Long id, Long userId);
    CollageEntry crearCollageParaUsuario(CollageEntry entry, Long userId);
    CollageEntry actualizarCollagePropio(Long id, CollageEntry entry, Long userId);
    void eliminarCollagePropio(Long id, Long userId);
}
