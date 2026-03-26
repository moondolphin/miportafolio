package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.NotaRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import moondolphin.miportafolio.domain.port.in.agenda.NotaServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notas")
public class NotaHandler {

    private final NotaServicePort notaService;

    public NotaHandler(NotaServicePort notaService) {
        this.notaService = notaService;
    }

    @GetMapping
    public List<NotaLibre> listar() {
        return notaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaLibre> obtener(@PathVariable Long id) {
        return notaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
    }

    @PostMapping
    public NotaLibre crear(@RequestBody NotaRequest request,
                           @AuthenticationPrincipal AgendaUserDetails userDetails) {
        NotaLibre nota = new NotaLibre();
        nota.setTitulo(request.getTitulo());
        nota.setContenido(request.getContenido());
        nota.setFechaReferencia(request.getFechaReferencia());
        nota.setCreatedBy(userDetails.getUserId());
        return notaService.crear(nota, request.getEtiquetaIds());
    }

    @PutMapping("/{id}")
    public NotaLibre actualizar(@PathVariable Long id,
                                @RequestBody NotaRequest request,
                                @AuthenticationPrincipal AgendaUserDetails userDetails) {
        NotaLibre nota = new NotaLibre();
        nota.setTitulo(request.getTitulo());
        nota.setContenido(request.getContenido());
        nota.setFechaReferencia(request.getFechaReferencia());
        nota.setCreatedBy(userDetails.getUserId());
        return notaService.actualizar(id, nota, request.getEtiquetaIds());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
