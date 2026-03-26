package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import java.util.List;
import java.util.Optional;

public interface NotaServicePort {
    List<NotaLibre> obtenerTodas();
    Optional<NotaLibre> obtenerPorId(Long id);
    NotaLibre crear(NotaLibre nota, List<Long> etiquetaIds);
    NotaLibre actualizar(Long id, NotaLibre nota, List<Long> etiquetaIds);
    void eliminar(Long id);
}
