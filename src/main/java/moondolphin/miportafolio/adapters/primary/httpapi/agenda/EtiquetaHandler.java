package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.EtiquetaRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import moondolphin.miportafolio.domain.port.in.agenda.EtiquetaServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/etiquetas")
public class EtiquetaHandler {

    private final EtiquetaServicePort etiquetaService;

    public EtiquetaHandler(EtiquetaServicePort etiquetaService) {
        this.etiquetaService = etiquetaService;
    }

    @GetMapping
    public List<Etiqueta> listar(@AuthenticationPrincipal AgendaUserDetails userDetails) {
        return etiquetaService.obtenerEtiquetasDelUsuario(userDetails.getUserId());
    }

    @PostMapping
    public Etiqueta crear(@RequestBody EtiquetaRequest request,
                          @AuthenticationPrincipal AgendaUserDetails userDetails) {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre(request.getNombre());
        etiqueta.setColor(request.getColor());
        return etiquetaService.crearEtiquetaParaUsuario(etiqueta, userDetails.getUserId());
    }

    @PutMapping("/{id}")
    public Etiqueta actualizar(@PathVariable Long id,
                               @RequestBody EtiquetaRequest request,
                               @AuthenticationPrincipal AgendaUserDetails userDetails) {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre(request.getNombre());
        etiqueta.setColor(request.getColor());
        return etiquetaService.actualizarEtiquetaPropia(id, etiqueta, userDetails.getUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal AgendaUserDetails userDetails) {
        etiquetaService.eliminarEtiquetaPropia(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
