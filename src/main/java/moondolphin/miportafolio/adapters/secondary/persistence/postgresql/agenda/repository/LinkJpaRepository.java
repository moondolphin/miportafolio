package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.LinkJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkJpaRepository extends JpaRepository<LinkJpaEntity, Long> {
    @Query("SELECT l FROM LinkJpaEntity l WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(l.descripcion) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(l.url) LIKE LOWER(CONCAT('%',:texto,'%'))")
    List<LinkJpaEntity> searchByTexto(@Param("texto") String texto);
}
