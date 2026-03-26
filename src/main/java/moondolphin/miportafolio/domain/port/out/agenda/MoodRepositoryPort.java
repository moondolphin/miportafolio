package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;
import java.util.List;
import java.util.Map;

public interface MoodRepositoryPort {
    List<MoodEntry> findAll();
    MoodEntry save(MoodEntry entry);
    Map<EstadoAnimo, Long> countByEstadoAnimo();
}
