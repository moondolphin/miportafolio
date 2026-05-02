package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtiquetaJpaRepository extends JpaRepository<EtiquetaJpaEntity, Long> {

    List<EtiquetaJpaEntity> findAllByCreatedByOrderByNombreAsc(Long createdBy);

    Optional<EtiquetaJpaEntity> findByIdAndCreatedBy(Long id, Long createdBy);

    List<EtiquetaJpaEntity> findByIdInAndCreatedBy(List<Long> ids, Long createdBy);
}
