package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.NotaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotaJpaRepository extends JpaRepository<NotaJpaEntity, Long> {

    List<NotaJpaEntity> findAllByCreatedByOrderByCreatedAtDesc(Long createdBy);

    Optional<NotaJpaEntity> findByIdAndCreatedBy(Long id, Long createdBy);

    @Query("SELECT n FROM NotaJpaEntity n WHERE n.createdBy = :createdBy AND (LOWER(n.titulo) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(n.contenido) LIKE LOWER(CONCAT('%',:texto,'%')))")
    List<NotaJpaEntity> searchByTextoAndCreatedBy(@Param("texto") String texto, @Param("createdBy") Long createdBy);
}
