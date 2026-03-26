package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.JournalRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import moondolphin.miportafolio.domain.port.in.agenda.JournalServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/journal")
public class JournalHandler {

    private final JournalServicePort journalService;

    public JournalHandler(JournalServicePort journalService) {
        this.journalService = journalService;
    }

    @GetMapping
    public List<JournalEntry> listar() {
        return journalService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> obtener(@PathVariable Long id) {
        return journalService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Entrada de journal no encontrada"));
    }

    @PostMapping
    public JournalEntry crear(@RequestBody JournalRequest request,
                              @AuthenticationPrincipal AgendaUserDetails userDetails) {
        JournalEntry entry = new JournalEntry();
        entry.setTitulo(request.getTitulo());
        entry.setContenido(request.getContenido());
        entry.setMoodId(request.getMoodId());
        entry.setFechaReferencia(request.getFechaReferencia());
        entry.setCreatedBy(userDetails.getUserId());
        return journalService.crear(entry, request.getEtiquetaIds());
    }

    @PutMapping("/{id}")
    public JournalEntry actualizar(@PathVariable Long id,
                                   @RequestBody JournalRequest request,
                                   @AuthenticationPrincipal AgendaUserDetails userDetails) {
        JournalEntry entry = new JournalEntry();
        entry.setTitulo(request.getTitulo());
        entry.setContenido(request.getContenido());
        entry.setMoodId(request.getMoodId());
        entry.setFechaReferencia(request.getFechaReferencia());
        entry.setCreatedBy(userDetails.getUserId());
        return journalService.actualizar(id, entry, request.getEtiquetaIds());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        journalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
