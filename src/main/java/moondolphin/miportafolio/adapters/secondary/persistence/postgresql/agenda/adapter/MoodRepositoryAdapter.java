package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.MoodJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.MoodJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;
import moondolphin.miportafolio.domain.port.out.agenda.MoodRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MoodRepositoryAdapter implements MoodRepositoryPort {

    private final MoodJpaRepository jpaRepository;

    public MoodRepositoryAdapter(MoodJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<MoodEntry> findAllByCreatedBy(Long userId) {
        return jpaRepository.findAllByCreatedByOrderByFechaDesc(userId).stream()
                .map(MoodJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public MoodEntry save(MoodEntry entry) {
        return jpaRepository.save(MoodJpaEntity.fromDomain(entry)).toDomain();
    }

    @Override
    public Map<EstadoAnimo, Long> countByEstadoAnimoAndCreatedBy(Long userId) {
        Map<EstadoAnimo, Long> resultado = new HashMap<>();
        for (Object[] row : jpaRepository.countGroupByEstadoAnimoAndCreatedBy(userId)) {
            resultado.put((EstadoAnimo) row[0], (Long) row[1]);
        }
        return resultado;
    }
}
