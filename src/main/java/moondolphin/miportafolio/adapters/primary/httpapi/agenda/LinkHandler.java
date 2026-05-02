package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.LinkRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import moondolphin.miportafolio.domain.port.in.agenda.LinkServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/links")
public class LinkHandler {

    private final LinkServicePort linkService;

    public LinkHandler(LinkServicePort linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public List<LinkItem> listar(@AuthenticationPrincipal AgendaUserDetails userDetails) {
        return linkService.obtenerLinksDelUsuario(userDetails.getUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinkItem> obtener(@PathVariable Long id,
                                            @AuthenticationPrincipal AgendaUserDetails userDetails) {
        return linkService.obtenerLinkPropioPorId(id, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Link no encontrado"));
    }

    @PostMapping
    public LinkItem crear(@RequestBody LinkRequest request,
                          @AuthenticationPrincipal AgendaUserDetails userDetails) {
        LinkItem link = new LinkItem();
        link.setTitulo(request.getTitulo());
        link.setUrl(request.getUrl());
        link.setDescripcion(request.getDescripcion());
        link.setFechaReferencia(request.getFechaReferencia());
        return linkService.crearLinkParaUsuario(link, request.getEtiquetaIds(), userDetails.getUserId());
    }

    @PutMapping("/{id}")
    public LinkItem actualizar(@PathVariable Long id,
                               @RequestBody LinkRequest request,
                               @AuthenticationPrincipal AgendaUserDetails userDetails) {
        LinkItem link = new LinkItem();
        link.setTitulo(request.getTitulo());
        link.setUrl(request.getUrl());
        link.setDescripcion(request.getDescripcion());
        link.setFechaReferencia(request.getFechaReferencia());
        return linkService.actualizarLinkPropio(id, link, request.getEtiquetaIds(), userDetails.getUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal AgendaUserDetails userDetails) {
        linkService.eliminarLinkPropio(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
