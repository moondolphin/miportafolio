/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package moondolphin.miportafolio;

/**
 *
 * @author Administrator
 */
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    List<Proyecto> findByNombre(String nombre);
    
    
    // MÉTODO NUEVO CON QUERY EXPLÍCITA
    @Query("SELECT p FROM Proyecto p WHERE p.descripcion LIKE %:termino%")
    List<Proyecto> buscarPorTerminoEnDescripcion(@Param("termino") String termino);

}
