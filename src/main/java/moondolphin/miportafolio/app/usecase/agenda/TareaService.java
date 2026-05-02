package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import moondolphin.miportafolio.domain.port.in.agenda.TareaServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.TareaRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TareaService implements TareaServicePort {

    private final TareaRepositoryPort tareaRepository;

    public TareaService(TareaRepositoryPort tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    @Override
    public List<Tarea> obtenerTareasDelUsuario(Long userId) {
        return tareaRepository.findAllByCreatedBy(userId);
    }

    @Override
    public List<Tarea> buscarTareasDelUsuario(Long userId, LocalDate fecha, Prioridad prioridad, EstadoTarea estado, String tag) {
        if (fecha != null) return tareaRepository.findByFechaAndCreatedBy(fecha, userId);
        if (prioridad != null) return tareaRepository.findByPrioridadAndCreatedBy(prioridad, userId);
        if (estado != null) return tareaRepository.findByEstadoAndCreatedBy(estado, userId);
        if (tag != null) return tareaRepository.findByEtiquetaNombreAndCreatedBy(tag, userId);
        return tareaRepository.findAllByCreatedBy(userId);
    }

    @Override
    public Optional<Tarea> obtenerTareaPropiaPorId(Long id, Long userId) {
        return tareaRepository.findByIdAndCreatedBy(id, userId);
    }

    @Override
    public Tarea crearTareaParaUsuario(Tarea tarea, List<Long> etiquetaIds, Long userId) {
        tarea.setCreatedBy(userId);
        tarea.setCreatedAt(LocalDateTime.now());
        tarea.setUpdatedAt(LocalDateTime.now());
        return tareaRepository.save(tarea, etiquetaIds);
    }

    @Override
    public Tarea actualizarTareaPropia(Long id, Tarea tarea, List<Long> etiquetaIds, Long userId) {
        tareaRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        tarea.setId(id);
        tarea.setCreatedBy(userId);
        tarea.setUpdatedAt(LocalDateTime.now());
        return tareaRepository.save(tarea, etiquetaIds);
    }

    @Override
    public void eliminarTareaPropia(Long id, Long userId) {
        tareaRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        tareaRepository.deleteById(id);
    }
}
