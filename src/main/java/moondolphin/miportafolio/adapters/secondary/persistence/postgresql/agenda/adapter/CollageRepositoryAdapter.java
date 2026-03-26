package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.CollageJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.CollageJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.CollageEntry;
import moondolphin.miportafolio.domain.port.out.agenda.CollageRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CollageRepositoryAdapter implements CollageRepositoryPort {

    private final CollageJpaRepository jpaRepository;

    public CollageRepositoryAdapter(CollageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CollageEntry> findAll() {
        return jpaRepository.findAll().stream().map(CollageJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<CollageEntry> findById(Long id) {
        return jpaRepository.findById(id).map(CollageJpaEntity::toDomain);
    }

    @Override
    public CollageEntry save(CollageEntry entry) {
        return jpaRepository.save(CollageJpaEntity.fromDomain(entry)).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
