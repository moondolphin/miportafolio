package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaRepositoryPort {
    List<Tarea> findAll();
    List<Tarea> findByFecha(LocalDate fecha);
    List<Tarea> findByPrioridad(Prioridad prioridad);
    List<Tarea> findByEstado(EstadoTarea estado);
    List<Tarea> findByEtiquetaNombre(String nombre);
    Optional<Tarea> findById(Long id);
    Tarea save(Tarea tarea, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<Tarea> searchByTexto(String texto);
}
