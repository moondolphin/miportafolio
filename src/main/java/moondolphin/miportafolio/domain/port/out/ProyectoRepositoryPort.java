package moondolphin.miportafolio.domain.port.out;

import moondolphin.miportafolio.domain.model.Proyecto;
import java.util.List;

public interface ProyectoRepositoryPort {
    List<Proyecto> findAll();
    List<Proyecto> findByNombre(String nombre);
    List<Proyecto> buscarPorTerminoEnDescripcion(String termino);
}
