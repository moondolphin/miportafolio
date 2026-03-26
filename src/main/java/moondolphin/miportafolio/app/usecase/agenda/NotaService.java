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
    public List<NotaLibre> obtenerTodas() {
        return notaRepository.findAll();
    }

    @Override
    public Optional<NotaLibre> obtenerPorId(Long id) {
        return notaRepository.findById(id);
    }

    @Override
    public NotaLibre crear(NotaLibre nota, List<Long> etiquetaIds) {
        nota.setCreatedAt(LocalDateTime.now());
        nota.setUpdatedAt(LocalDateTime.now());
        return notaRepository.save(nota, etiquetaIds);
    }

    @Override
    public NotaLibre actualizar(Long id, NotaLibre nota, List<Long> etiquetaIds) {
        notaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
        nota.setId(id);
        nota.setUpdatedAt(LocalDateTime.now());
        return notaRepository.save(nota, etiquetaIds);
    }

    @Override
    public void eliminar(Long id) {
        notaRepository.deleteById(id);
    }
}
