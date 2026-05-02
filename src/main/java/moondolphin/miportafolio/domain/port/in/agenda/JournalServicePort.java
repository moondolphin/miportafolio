package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import java.util.List;
import java.util.Optional;

public interface JournalServicePort {
    List<JournalEntry> obtenerJournalDelUsuario(Long userId);
    Optional<JournalEntry> obtenerEntradaJournalPropiaPorId(Long id, Long userId);
    JournalEntry crearEntradaJournalParaUsuario(JournalEntry entry, List<Long> etiquetaIds, Long userId);
    JournalEntry actualizarEntradaJournalPropia(Long id, JournalEntry entry, List<Long> etiquetaIds, Long userId);
    void eliminarEntradaJournalPropia(Long id, Long userId);
}
