package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import moondolphin.miportafolio.domain.port.in.agenda.NotaServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.NotaRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotaService implements NotaServicePort {

    private final NotaRepositoryPort notaRepository;

    public NotaService(NotaRepositoryPort notaRepository) {
        this.notaRepository = notaRepository;
    }

    @Override
    public List<NotaLibre> obtenerNotasDelUsuario(Long userId) {
        return notaRepository.findAllByCreatedBy(userId);
    }

    @Override
    public Optional<NotaLibre> obtenerNotaPropiaPorId(Long id, Long userId) {
        return notaRepository.findByIdAndCreatedBy(id, userId);
    }

    @Override
    public NotaLibre crearNotaParaUsuario(NotaLibre nota, List<Long> etiquetaIds, Long userId) {
        nota.setCreatedBy(userId);
        nota.setCreatedAt(LocalDateTime.now());
        nota.setUpdatedAt(LocalDateTime.now());
        return notaRepository.save(nota, etiquetaIds);
    }

    @Override
    public NotaLibre actualizarNotaPropia(Long id, NotaLibre nota, List<Long> etiquetaIds, Long userId) {
        notaRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
        nota.setId(id);
        nota.setCreatedBy(userId);
        nota.setUpdatedAt(LocalDateTime.now());
        return notaRepository.save(nota, etiquetaIds);
    }

    @Override
    public void eliminarNotaPropia(Long id, Long userId) {
        notaRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
        notaRepository.deleteById(id);
    }
}
