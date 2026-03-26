package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.EtiquetaRequest;
import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import moondolphin.miportafolio.domain.port.in.agenda.EtiquetaServicePort;
import org.springframework.http.ResponseEntity;
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
    public List<Etiqueta> listar() {
        return etiquetaService.obtenerTodas();
    }

    @PostMapping
    public Etiqueta crear(@RequestBody EtiquetaRequest request) {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre(request.getNombre());
        etiqueta.setColor(request.getColor());
        return etiquetaService.crear(etiqueta);
    }

    @PutMapping("/{id}")
    public Etiqueta actualizar(@PathVariable Long id, @RequestBody EtiquetaRequest request) {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre(request.getNombre());
        etiqueta.setColor(request.getColor());
        return etiquetaService.actualizar(id, etiqueta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        etiquetaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
