package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import java.util.List;
import java.util.Optional;

public interface LinkServicePort {
    List<LinkItem> obtenerLinksDelUsuario(Long userId);
    Optional<LinkItem> obtenerLinkPropioPorId(Long id, Long userId);
    LinkItem crearLinkParaUsuario(LinkItem link, List<Long> etiquetaIds, Long userId);
    LinkItem actualizarLinkPropio(Long id, LinkItem link, List<Long> etiquetaIds, Long userId);
    void eliminarLinkPropio(Long id, Long userId);
}
