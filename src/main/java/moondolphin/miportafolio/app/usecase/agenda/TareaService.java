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
    public List<Tarea> obtenerTodas() {
        return tareaRepository.findAll();
    }

    @Override
    public List<Tarea> buscar(LocalDate fecha, Prioridad prioridad, EstadoTarea estado, String tag) {
        if (fecha != null) return tareaRepository.findByFecha(fecha);
        if (prioridad != null) return tareaRepository.findByPrioridad(prioridad);
        if (estado != null) return tareaRepository.findByEstado(estado);
        if (tag != null) return tareaRepository.findByEtiquetaNombre(tag);
        return tareaRepository.findAll();
    }

    @Override
    public Optional<Tarea> obtenerPorId(Long id) {
        return tareaRepository.findById(id);
    }

    @Override
    public Tarea crear(Tarea tarea, List<Long> etiquetaIds) {
        tarea.setCreatedAt(LocalDateTime.now());
        tarea.setUpdatedAt(LocalDateTime.now());
        return tareaRepository.save(tarea, etiquetaIds);
    }

    @Override
    public Tarea actualizar(Long id, Tarea tarea, List<Long> etiquetaIds) {
        tareaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        tarea.setId(id);
        tarea.setUpdatedAt(LocalDateTime.now());
        return tareaRepository.save(tarea, etiquetaIds);
    }

    @Override
    public void eliminar(Long id) {
        tareaRepository.deleteById(id);
    }
}
