package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import java.util.List;

public interface EtiquetaServicePort {
    List<Etiqueta> obtenerTodas();
    Etiqueta crear(Etiqueta etiqueta);
    Etiqueta actualizar(Long id, Etiqueta etiqueta);
    void eliminar(Long id);
}
