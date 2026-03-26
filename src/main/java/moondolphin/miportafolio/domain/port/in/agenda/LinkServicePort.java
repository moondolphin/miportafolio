package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import java.util.List;
import java.util.Optional;

public interface LinkServicePort {
    List<LinkItem> obtenerTodos();
    Optional<LinkItem> obtenerPorId(Long id);
    LinkItem crear(LinkItem link, List<Long> etiquetaIds);
    LinkItem actualizar(Long id, LinkItem link, List<Long> etiquetaIds);
    void eliminar(Long id);
}
