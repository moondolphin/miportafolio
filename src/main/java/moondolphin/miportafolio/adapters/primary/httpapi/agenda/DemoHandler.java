package moondolphin.miportafolio.adapters.primary.httpapi.agenda;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/agenda-mislun")
public class DemoHandler {

    @GetMapping("/demo")
    public Map<String, Object> demo() {
        return Map.of(
            "nombre", "Agenda Mislun",
            "descripcion", "Módulo privado de agenda personal. El acceso completo requiere autenticación y aprobación.",
            "funciones", new String[]{"tareas", "notas", "journal", "links", "recordatorios", "mood", "collage", "buscador"},
            "acceso", "Contacta a la administradora para solicitar acceso."
        );
    }
}
