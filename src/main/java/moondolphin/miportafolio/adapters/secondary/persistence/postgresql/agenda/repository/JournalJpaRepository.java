package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.JournalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalJpaRepository extends JpaRepository<JournalJpaEntity, Long> {

    List<JournalJpaEntity> findAllByCreatedByOrderByFechaReferenciaDesc(Long createdBy);

    Optional<JournalJpaEntity> findByIdAndCreatedBy(Long id, Long createdBy);

    @Query("SELECT j FROM JournalJpaEntity j WHERE j.createdBy = :createdBy AND (LOWER(j.titulo) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(j.contenido) LIKE LOWER(CONCAT('%',:texto,'%')))")
    List<JournalJpaEntity> searchByTextoAndCreatedBy(@Param("texto") String texto, @Param("createdBy") Long createdBy);
}
