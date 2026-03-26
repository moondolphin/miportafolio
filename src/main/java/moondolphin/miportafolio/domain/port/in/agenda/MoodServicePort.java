package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;
import java.util.List;
import java.util.Map;

public interface MoodServicePort {
    List<MoodEntry> obtenerTodos();
    MoodEntry registrar(MoodEntry entry);
    Map<EstadoAnimo, Long> obtenerEstadisticas();
}
