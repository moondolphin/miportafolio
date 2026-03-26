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
    public List<JournalEntry> obtenerTodas() {
        return journalRepository.findAll();
    }

    @Override
    public Optional<JournalEntry> obtenerPorId(Long id) {
        return journalRepository.findById(id);
    }

    @Override
    public JournalEntry crear(JournalEntry entry, List<Long> etiquetaIds) {
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        return journalRepository.save(entry, etiquetaIds);
    }

    @Override
    public JournalEntry actualizar(Long id, JournalEntry entry, List<Long> etiquetaIds) {
        journalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entrada de journal no encontrada"));
        entry.setId(id);
        entry.setUpdatedAt(LocalDateTime.now());
        return journalRepository.save(entry, etiquetaIds);
    }

    @Override
    public void eliminar(Long id) {
        journalRepository.deleteById(id);
    }
}
