package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaRepositoryPort {
    List<Tarea> findAllByCreatedBy(Long userId);
    List<Tarea> findByFechaAndCreatedBy(LocalDate fecha, Long userId);
    List<Tarea> findByPrioridadAndCreatedBy(Prioridad prioridad, Long userId);
    List<Tarea> findByEstadoAndCreatedBy(EstadoTarea estado, Long userId);
    List<Tarea> findByEtiquetaNombreAndCreatedBy(String nombre, Long userId);
    Optional<Tarea> findByIdAndCreatedBy(Long id, Long userId);
    Tarea save(Tarea tarea, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<Tarea> searchByTextoAndCreatedBy(String texto, Long userId);
}
