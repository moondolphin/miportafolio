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
    public List<NotaLibre> listar(@AuthenticationPrincipal AgendaUserDetails userDetails) {
        return notaService.obtenerNotasDelUsuario(userDetails.getUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaLibre> obtener(@PathVariable Long id,
                                             @AuthenticationPrincipal AgendaUserDetails userDetails) {
        return notaService.obtenerNotaPropiaPorId(id, userDetails.getUserId())
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
        return notaService.crearNotaParaUsuario(nota, request.getEtiquetaIds(), userDetails.getUserId());
    }

    @PutMapping("/{id}")
    public NotaLibre actualizar(@PathVariable Long id,
                                @RequestBody NotaRequest request,
                                @AuthenticationPrincipal AgendaUserDetails userDetails) {
        NotaLibre nota = new NotaLibre();
        nota.setTitulo(request.getTitulo());
        nota.setContenido(request.getContenido());
        nota.setFechaReferencia(request.getFechaReferencia());
        return notaService.actualizarNotaPropia(id, nota, request.getEtiquetaIds(), userDetails.getUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal AgendaUserDetails userDetails) {
        notaService.eliminarNotaPropia(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
