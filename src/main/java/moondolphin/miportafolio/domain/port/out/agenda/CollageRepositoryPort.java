package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.CollageEntry;
import java.util.List;
import java.util.Optional;

public interface CollageRepositoryPort {
    List<CollageEntry> findAllByCreatedBy(Long userId);
    Optional<CollageEntry> findByIdAndCreatedBy(Long id, Long userId);
    CollageEntry save(CollageEntry entry);
    void deleteById(Long id);
}
