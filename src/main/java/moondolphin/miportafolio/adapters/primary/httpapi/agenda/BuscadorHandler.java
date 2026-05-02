package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import moondolphin.miportafolio.bootstrap.security.AgendaUserDetails;
import moondolphin.miportafolio.domain.port.in.agenda.BuscadorServicePort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/historico")
public class BuscadorHandler {

    private final BuscadorServicePort buscadorService;

    public BuscadorHandler(BuscadorServicePort buscadorService) {
        this.buscadorService = buscadorService;
    }

    @GetMapping("/search")
    public BuscadorServicePort.Resultado buscar(@RequestParam String q,
                                                @AuthenticationPrincipal AgendaUserDetails userDetails) {
        return buscadorService.buscarEnAgendaDelUsuario(q, userDetails.getUserId());
    }
}
