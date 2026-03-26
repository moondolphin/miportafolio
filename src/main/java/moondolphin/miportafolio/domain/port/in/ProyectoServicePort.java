package moondolphin.miportafolio.domain.port.in;

import moondolphin.miportafolio.domain.model.Proyecto;
import java.util.List;

public interface ProyectoServicePort {
    List<Proyecto> obtenerTodos();
    List<Proyecto> buscarPorNombre(String nombre);
    List<Proyecto> buscarPorTerminoEnDescripcion(String termino);
}
