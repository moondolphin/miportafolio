package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.TareaJpaEntity;
import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaJpaRepository extends JpaRepository<TareaJpaEntity, Long> {

    List<TareaJpaEntity> findAllByCreatedByOrderByFechaDesc(Long createdBy);

    Optional<TareaJpaEntity> findByIdAndCreatedBy(Long id, Long createdBy);

    List<TareaJpaEntity> findByFechaAndCreatedBy(LocalDate fecha, Long createdBy);

    List<TareaJpaEntity> findByPrioridadAndCreatedBy(Prioridad prioridad, Long createdBy);

    List<TareaJpaEntity> findByEstadoAndCreatedBy(EstadoTarea estado, Long createdBy);

    @Query("SELECT t FROM TareaJpaEntity t JOIN t.etiquetas e WHERE e.nombre = :nombre AND t.createdBy = :createdBy")
    List<TareaJpaEntity> findByEtiquetaNombreAndCreatedBy(@Param("nombre") String nombre, @Param("createdBy") Long createdBy);

    @Query("SELECT t FROM TareaJpaEntity t WHERE t.createdBy = :createdBy AND (LOWER(t.titulo) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(t.descripcion) LIKE LOWER(CONCAT('%',:texto,'%')))")
    List<TareaJpaEntity> searchByTextoAndCreatedBy(@Param("texto") String texto, @Param("createdBy") Long createdBy);
}
