package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.CollageRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.CollageEntry;
import moondolphin.miportafolio.domain.port.in.agenda.CollageServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/collage")
public class CollageHandler {

    private final CollageServicePort collageService;

    public CollageHandler(CollageServicePort collageService) {
        this.collageService = collageService;
    }

    @GetMapping
    public List<CollageEntry> listar(@AuthenticationPrincipal AgendaUserDetails userDetails) {
        return collageService.obtenerCollageDelUsuario(userDetails.getUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollageEntry> obtener(@PathVariable Long id,
                                                @AuthenticationPrincipal AgendaUserDetails userDetails) {
        return collageService.obtenerCollagePropioPorId(id, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Collage no encontrado"));
    }

    @PostMapping
    public CollageEntry crear(@RequestBody CollageRequest request,
                              @AuthenticationPrincipal AgendaUserDetails userDetails) {
        CollageEntry entry = new CollageEntry();
        entry.setTitulo(request.getTitulo());
        entry.setImageUrl(request.getImageUrl());
        entry.setGifUrl(request.getGifUrl());
        entry.setQuote(request.getQuote());
        entry.setFechaReferencia(request.getFechaReferencia());
        return collageService.crearCollageParaUsuario(entry, userDetails.getUserId());
    }

    @PutMapping("/{id}")
    public CollageEntry actualizar(@PathVariable Long id,
                                   @RequestBody CollageRequest request,
                                   @AuthenticationPrincipal AgendaUserDetails userDetails) {
        CollageEntry entry = new CollageEntry();
        entry.setTitulo(request.getTitulo());
        entry.setImageUrl(request.getImageUrl());
        entry.setGifUrl(request.getGifUrl());
        entry.setQuote(request.getQuote());
        entry.setFechaReferencia(request.getFechaReferencia());
        return collageService.actualizarCollagePropio(id, entry, userDetails.getUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal AgendaUserDetails userDetails) {
        collageService.eliminarCollagePropio(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
