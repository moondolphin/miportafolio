package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.MoodJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MoodJpaRepository extends JpaRepository<MoodJpaEntity, Long> {

    List<MoodJpaEntity> findAllByCreatedByOrderByFechaDesc(Long createdBy);

    @Query("SELECT m.estadoAnimo, COUNT(m) FROM MoodJpaEntity m WHERE m.createdBy = :createdBy GROUP BY m.estadoAnimo")
    List<Object[]> countGroupByEstadoAnimoAndCreatedBy(@Param("createdBy") Long createdBy);
}
