package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.TareaJpaEntity;
import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TareaJpaRepository extends JpaRepository<TareaJpaEntity, Long> {
    List<TareaJpaEntity> findByFecha(LocalDate fecha);
    List<TareaJpaEntity> findByPrioridad(Prioridad prioridad);
    List<TareaJpaEntity> findByEstado(EstadoTarea estado);

    @Query("SELECT t FROM TareaJpaEntity t JOIN t.etiquetas e WHERE e.nombre = :nombre")
    List<TareaJpaEntity> findByEtiquetaNombre(@Param("nombre") String nombre);

    @Query("SELECT t FROM TareaJpaEntity t WHERE LOWER(t.titulo) LIKE LOWER(CONCAT('%',:texto,'%')) OR LOWER(t.descripcion) LIKE LOWER(CONCAT('%',:texto,'%'))")
    List<TareaJpaEntity> searchByTexto(@Param("texto") String texto);
}
