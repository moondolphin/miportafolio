package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaServicePort {
    List<Tarea> obtenerTareasDelUsuario(Long userId);
    List<Tarea> buscarTareasDelUsuario(Long userId, LocalDate fecha, Prioridad prioridad, EstadoTarea estado, String tag);
    Optional<Tarea> obtenerTareaPropiaPorId(Long id, Long userId);
    Tarea crearTareaParaUsuario(Tarea tarea, List<Long> etiquetaIds, Long userId);
    Tarea actualizarTareaPropia(Long id, Tarea tarea, List<Long> etiquetaIds, Long userId);
    void eliminarTareaPropia(Long id, Long userId);
}
