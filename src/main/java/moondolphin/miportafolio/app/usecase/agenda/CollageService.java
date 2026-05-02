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
    public List<CollageEntry> obtenerCollageDelUsuario(Long userId) {
        return collageRepository.findAllByCreatedBy(userId);
    }

    @Override
    public Optional<CollageEntry> obtenerCollagePropioPorId(Long id, Long userId) {
        return collageRepository.findByIdAndCreatedBy(id, userId);
    }

    @Override
    public CollageEntry crearCollageParaUsuario(CollageEntry entry, Long userId) {
        entry.setCreatedBy(userId);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        return collageRepository.save(entry);
    }

    @Override
    public CollageEntry actualizarCollagePropio(Long id, CollageEntry entry, Long userId) {
        collageRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Collage no encontrado"));
        entry.setId(id);
        entry.setCreatedBy(userId);
        entry.setUpdatedAt(LocalDateTime.now());
        return collageRepository.save(entry);
    }

    @Override
    public void eliminarCollagePropio(Long id, Long userId) {
        collageRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Collage no encontrado"));
        collageRepository.deleteById(id);
    }
}
