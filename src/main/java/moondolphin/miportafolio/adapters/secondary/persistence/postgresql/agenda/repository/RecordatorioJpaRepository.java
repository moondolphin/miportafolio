package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.RecordatorioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordatorioJpaRepository extends JpaRepository<RecordatorioJpaEntity, Long> {

    List<RecordatorioJpaEntity> findAllByCreatedByOrderByFechaAsc(Long createdBy);

    Optional<RecordatorioJpaEntity> findByIdAndCreatedBy(Long id, Long createdBy);
}
