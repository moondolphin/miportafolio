package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.MoodJpaEntity;
import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MoodJpaRepository extends JpaRepository<MoodJpaEntity, Long> {
    @Query("SELECT m.estadoAnimo, COUNT(m) FROM MoodJpaEntity m GROUP BY m.estadoAnimo")
    List<Object[]> countGroupByEstadoAnimo();
}
