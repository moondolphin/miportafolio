package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.CollageEntry;
import java.util.List;
import java.util.Optional;

public interface CollageServicePort {
    List<CollageEntry> obtenerTodos();
    Optional<CollageEntry> obtenerPorId(Long id);
    CollageEntry crear(CollageEntry entry);
    CollageEntry actualizar(Long id, CollageEntry entry);
    void eliminar(Long id);
}
