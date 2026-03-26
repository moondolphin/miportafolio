package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.RecordatorioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordatorioJpaRepository extends JpaRepository<RecordatorioJpaEntity, Long> {
}
