package moondolphin.miportafolio.adapters.primary.httpapi.handler;

import moondolphin.miportafolio.domain.model.Proyecto;
import moondolphin.miportafolio.domain.port.in.ProyectoServicePort;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProyectoHandler {

    private final ProyectoServicePort proyectoService;

    public ProyectoHandler(ProyectoServicePort proyectoService) {
        this.proyectoService = proyectoService;
    }

    @GetMapping("/saludo")
    public String saludar(@RequestParam(defaultValue = "Mundo") String nombre) {
        return String.format("¡Hola, %s! Este saludo viene desde Java.", nombre);
    }

    @GetMapping("/proyectos")
    public List<Proyecto> obtenerTodos() {
        return proyectoService.obtenerTodos();
    }

    @GetMapping("/proyectos/buscar")
    public List<Proyecto> buscarPorNombre(@RequestParam String nombre) {
        return proyectoService.buscarPorNombre(nombre);
    }

    @GetMapping("/proyectos/buscar-termino")
    public List<Proyecto> buscarPorTerminoEnDescripcion(@RequestParam String descripcion) {
        return proyectoService.buscarPorTerminoEnDescripcion(descripcion);
    }
}
