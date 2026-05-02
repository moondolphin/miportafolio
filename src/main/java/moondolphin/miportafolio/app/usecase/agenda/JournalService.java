package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import moondolphin.miportafolio.domain.port.in.agenda.JournalServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.JournalRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalService implements JournalServicePort {

    private final JournalRepositoryPort journalRepository;

    public JournalService(JournalRepositoryPort journalRepository) {
        this.journalRepository = journalRepository;
    }

    @Override
    public List<JournalEntry> obtenerJournalDelUsuario(Long userId) {
        return journalRepository.findAllByCreatedBy(userId);
    }

    @Override
    public Optional<JournalEntry> obtenerEntradaJournalPropiaPorId(Long id, Long userId) {
        return journalRepository.findByIdAndCreatedBy(id, userId);
    }

    @Override
    public JournalEntry crearEntradaJournalParaUsuario(JournalEntry entry, List<Long> etiquetaIds, Long userId) {
        entry.setCreatedBy(userId);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        return journalRepository.save(entry, etiquetaIds);
    }

    @Override
    public JournalEntry actualizarEntradaJournalPropia(Long id, JournalEntry entry, List<Long> etiquetaIds, Long userId) {
        journalRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Entrada de journal no encontrada"));
        entry.setId(id);
        entry.setCreatedBy(userId);
        entry.setUpdatedAt(LocalDateTime.now());
        return journalRepository.save(entry, etiquetaIds);
    }

    @Override
    public void eliminarEntradaJournalPropia(Long id, Long userId) {
        journalRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Entrada de journal no encontrada"));
        journalRepository.deleteById(id);
    }
}
