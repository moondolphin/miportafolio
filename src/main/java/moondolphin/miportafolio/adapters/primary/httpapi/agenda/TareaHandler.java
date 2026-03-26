package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.TareaRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import moondolphin.miportafolio.domain.port.in.agenda.TareaServicePort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tareas")
public class TareaHandler {

    private final TareaServicePort tareaService;

    public TareaHandler(TareaServicePort tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public List<Tarea> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(required = false) String tag) {
        return tareaService.buscar(fecha, prioridad, estado, tag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtener(@PathVariable Long id) {
        return tareaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
    }

    @PostMapping
    public Tarea crear(@RequestBody TareaRequest request,
                       @AuthenticationPrincipal AgendaUserDetails userDetails) {
        Tarea tarea = new Tarea();
        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setFecha(request.getFecha());
        tarea.setHoraInicio(request.getHoraInicio());
        tarea.setHoraFin(request.getHoraFin());
        tarea.setPrioridad(request.getPrioridad());
        tarea.setEstado(request.getEstado());
        tarea.setCreatedBy(userDetails.getUserId());
        return tareaService.crear(tarea, request.getEtiquetaIds());
    }

    @PutMapping("/{id}")
    public Tarea actualizar(@PathVariable Long id,
                            @RequestBody TareaRequest request,
                            @AuthenticationPrincipal AgendaUserDetails userDetails) {
        Tarea tarea = new Tarea();
        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setFecha(request.getFecha());
        tarea.setHoraInicio(request.getHoraInicio());
        tarea.setHoraFin(request.getHoraFin());
        tarea.setPrioridad(request.getPrioridad());
        tarea.setEstado(request.getEstado());
        tarea.setCreatedBy(userDetails.getUserId());
        return tareaService.actualizar(id, tarea, request.getEtiquetaIds());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
