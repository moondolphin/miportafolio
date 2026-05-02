package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import java.util.List;
import java.util.Optional;

public interface JournalRepositoryPort {
    List<JournalEntry> findAllByCreatedBy(Long userId);
    Optional<JournalEntry> findByIdAndCreatedBy(Long id, Long userId);
    JournalEntry save(JournalEntry entry, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<JournalEntry> searchByTextoAndCreatedBy(String texto, Long userId);
}
