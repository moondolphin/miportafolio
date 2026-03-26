package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtiquetaJpaRepository extends JpaRepository<EtiquetaJpaEntity, Long> {
    List<EtiquetaJpaEntity> findByIdIn(List<Long> ids);
}
