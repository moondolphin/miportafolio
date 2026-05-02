package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto.MoodRequest;
import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;
import moondolphin.miportafolio.domain.port.in.agenda.MoodServicePort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/moods")
public class MoodHandler {

    private final MoodServicePort moodService;

    public MoodHandler(MoodServicePort moodService) {
        this.moodService = moodService;
    }

    @GetMapping
    public List<MoodEntry> listar(@AuthenticationPrincipal AgendaUserDetails userDetails) {
        return moodService.obtenerMoodsDelUsuario(userDetails.getUserId());
    }

    @PostMapping
    public MoodEntry registrar(@RequestBody MoodRequest request,
                               @AuthenticationPrincipal AgendaUserDetails userDetails) {
        MoodEntry entry = new MoodEntry();
        entry.setFecha(request.getFecha());
        entry.setEstadoAnimo(request.getEstadoAnimo());
        entry.setNota(request.getNota());
        return moodService.registrarMoodParaUsuario(entry, userDetails.getUserId());
    }

    @GetMapping("/stats")
    public Map<EstadoAnimo, Long> estadisticas(@AuthenticationPrincipal AgendaUserDetails userDetails) {
        return moodService.obtenerEstadisticasMoodDelUsuario(userDetails.getUserId());
    }
}
