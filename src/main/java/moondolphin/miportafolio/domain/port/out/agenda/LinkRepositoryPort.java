package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import java.util.List;
import java.util.Optional;

public interface LinkRepositoryPort {
    List<LinkItem> findAll();
    Optional<LinkItem> findById(Long id);
    LinkItem save(LinkItem link, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<LinkItem> searchByTexto(String texto);
}
