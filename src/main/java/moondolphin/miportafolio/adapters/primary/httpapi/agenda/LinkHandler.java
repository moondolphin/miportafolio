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
    public List<LinkItem> listar() {
        return linkService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinkItem> obtener(@PathVariable Long id) {
        return linkService.obtenerPorId(id)
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
        link.setCreatedBy(userDetails.getUserId());
        return linkService.crear(link, request.getEtiquetaIds());
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
        link.setCreatedBy(userDetails.getUserId());
        return linkService.actualizar(id, link, request.getEtiquetaIds());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        linkService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
