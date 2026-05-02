package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;
import moondolphin.miportafolio.domain.port.in.agenda.MoodServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.MoodRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MoodService implements MoodServicePort {

    private final MoodRepositoryPort moodRepository;

    public MoodService(MoodRepositoryPort moodRepository) {
        this.moodRepository = moodRepository;
    }

    @Override
    public List<MoodEntry> obtenerMoodsDelUsuario(Long userId) {
        return moodRepository.findAllByCreatedBy(userId);
    }

    @Override
    public MoodEntry registrarMoodParaUsuario(MoodEntry entry, Long userId) {
        entry.setCreatedBy(userId);
        entry.setCreatedAt(LocalDateTime.now());
        return moodRepository.save(entry);
    }

    @Override
    public Map<EstadoAnimo, Long> obtenerEstadisticasMoodDelUsuario(Long userId) {
        return moodRepository.countByEstadoAnimoAndCreatedBy(userId);
    }
}
