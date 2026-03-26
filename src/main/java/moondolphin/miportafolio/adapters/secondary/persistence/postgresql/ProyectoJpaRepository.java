package moondolphin.miportafolio.adapters.secondary.persistence.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProyectoJpaRepository extends JpaRepository<ProyectoJpaEntity, Long> {

    List<ProyectoJpaEntity> findByNombre(String nombre);

    @Query("SELECT p FROM ProyectoJpaEntity p WHERE p.descripcion LIKE %:termino%")
    List<ProyectoJpaEntity> buscarPorTerminoEnDescripcion(@Param("termino") String termino);
}
