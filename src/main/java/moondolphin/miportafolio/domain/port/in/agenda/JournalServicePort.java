package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import java.util.List;
import java.util.Optional;

public interface JournalServicePort {
    List<JournalEntry> obtenerTodas();
    Optional<JournalEntry> obtenerPorId(Long id);
    JournalEntry crear(JournalEntry entry, List<Long> etiquetaIds);
    JournalEntry actualizar(Long id, JournalEntry entry, List<Long> etiquetaIds);
    void eliminar(Long id);
}
