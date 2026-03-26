package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.RecordatorioRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import moondolphin.miportafolio.domain.port.in.agenda.RecordatorioServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/recordatorios")
public class RecordatorioHandler {

    private final RecordatorioServicePort recordatorioService;

    public RecordatorioHandler(RecordatorioServicePort recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    @GetMapping
    public List<Recordatorio> listar() {
        return recordatorioService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recordatorio> obtener(@PathVariable Long id) {
        return recordatorioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Recordatorio no encontrado"));
    }

    @PostMapping
    public Recordatorio crear(@RequestBody RecordatorioRequest request,
                              @AuthenticationPrincipal AgendaUserDetails userDetails) {
        Recordatorio r = new Recordatorio();
        r.setTitulo(request.getTitulo());
        r.setDescripcion(request.getDescripcion());
        r.setFecha(request.getFecha());
        r.setHora(request.getHora());
        r.setEstado(request.getEstado());
        r.setCreatedBy(userDetails.getUserId());
        return recordatorioService.crear(r, request.getEtiquetaIds());
    }

    @PutMapping("/{id}")
    public Recordatorio actualizar(@PathVariable Long id,
                                   @RequestBody RecordatorioRequest request,
                                   @AuthenticationPrincipal AgendaUserDetails userDetails) {
        Recordatorio r = new Recordatorio();
        r.setTitulo(request.getTitulo());
        r.setDescripcion(request.getDescripcion());
        r.setFecha(request.getFecha());
        r.setHora(request.getHora());
        r.setEstado(request.getEstado());
        r.setCreatedBy(userDetails.getUserId());
        return recordatorioService.actualizar(id, r, request.getEtiquetaIds());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        recordatorioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
