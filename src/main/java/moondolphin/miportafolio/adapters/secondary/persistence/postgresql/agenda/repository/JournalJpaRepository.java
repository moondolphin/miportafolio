package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.JournalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JournalJpaRepository extends JpaRepository<JournalJpaEntity, Long> {
    @Query("SELECT j FROM JournalJpaEntity j WHERE LOWER(j.titulo) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(j.contenido) LIKE LOWER(CONCAT('%',:texto,'%'))")
    List<JournalJpaEntity> searchByTexto(@Param("texto") String texto);
}
