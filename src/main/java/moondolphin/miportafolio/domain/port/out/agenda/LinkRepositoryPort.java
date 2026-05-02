package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import java.util.List;
import java.util.Optional;

public interface LinkRepositoryPort {
    List<LinkItem> findAllByCreatedBy(Long userId);
    Optional<LinkItem> findByIdAndCreatedBy(Long id, Long userId);
    LinkItem save(LinkItem link, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<LinkItem> searchByTextoAndCreatedBy(String texto, Long userId);
}
