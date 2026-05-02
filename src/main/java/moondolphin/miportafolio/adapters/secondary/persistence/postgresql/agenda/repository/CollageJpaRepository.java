package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.CollageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollageJpaRepository extends JpaRepository<CollageJpaEntity, Long> {

    List<CollageJpaEntity> findAllByCreatedByOrderByCreatedAtDesc(Long createdBy);

    Optional<CollageJpaEntity> findByIdAndCreatedBy(Long id, Long createdBy);
}
