package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.CollageEntry;
import moondolphin.miportafolio.domain.port.in.agenda.CollageServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.CollageRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CollageService implements CollageServicePort {

    private final CollageRepositoryPort collageRepository;

    public CollageService(CollageRepositoryPort collageRepository) {
        this.collageRepository = collageRepository;
    }

    @Override
    public List<CollageEntry> obtenerTodos() {
        return collageRepository.findAll();
    }

    @Override
    public Optional<CollageEntry> obtenerPorId(Long id) {
        return collageRepository.findById(id);
    }

    @Override
    public CollageEntry crear(CollageEntry entry) {
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        return collageRepository.save(entry);
    }

    @Override
    public CollageEntry actualizar(Long id, CollageEntry entry) {
        collageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Collage no encontrado"));
        entry.setId(id);
        entry.setUpdatedAt(LocalDateTime.now());
        return collageRepository.save(entry);
    }

    @Override
    public void eliminar(Long id) {
        collageRepository.deleteById(id);
    }
}
