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
    public List<JournalEntry> listar(@AuthenticationPrincipal AgendaUserDetails userDetails) {
        return journalService.obtenerJournalDelUsuario(userDetails.getUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> obtener(@PathVariable Long id,
                                                @AuthenticationPrincipal AgendaUserDetails userDetails) {
        return journalService.obtenerEntradaJournalPropiaPorId(id, userDetails.getUserId())
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
        return journalService.crearEntradaJournalParaUsuario(entry, request.getEtiquetaIds(), userDetails.getUserId());
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
        return journalService.actualizarEntradaJournalPropia(id, entry, request.getEtiquetaIds(), userDetails.getUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal AgendaUserDetails userDetails) {
        journalService.eliminarEntradaJournalPropia(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
