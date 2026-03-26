package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.NotaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotaJpaRepository extends JpaRepository<NotaJpaEntity, Long> {
    @Query("SELECT n FROM NotaJpaEntity n WHERE LOWER(n.titulo) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(n.contenido) LIKE LOWER(CONCAT('%',:texto,'%'))")
    List<NotaJpaEntity> searchByTexto(@Param("texto") String texto);
}
