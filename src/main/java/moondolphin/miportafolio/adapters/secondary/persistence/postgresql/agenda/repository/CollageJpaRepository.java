package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.CollageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollageJpaRepository extends JpaRepository<CollageJpaEntity, Long> {
}
