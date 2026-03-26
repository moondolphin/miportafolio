package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaServicePort {
    List<Tarea> obtenerTodas();
    List<Tarea> buscar(LocalDate fecha, Prioridad prioridad, EstadoTarea estado, String tag);
    Optional<Tarea> obtenerPorId(Long id);
    Tarea crear(Tarea tarea, List<Long> etiquetaIds);
    Tarea actualizar(Long id, Tarea tarea, List<Long> etiquetaIds);
    void eliminar(Long id);
}
