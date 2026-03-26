package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import java.util.List;
import java.util.Optional;

public interface JournalRepositoryPort {
    List<JournalEntry> findAll();
    Optional<JournalEntry> findById(Long id);
    JournalEntry save(JournalEntry entry, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<JournalEntry> searchByTexto(String texto);
}
