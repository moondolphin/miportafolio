package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import java.util.List;
import java.util.Optional;

public interface NotaServicePort {
    List<NotaLibre> obtenerNotasDelUsuario(Long userId);
    Optional<NotaLibre> obtenerNotaPropiaPorId(Long id, Long userId);
    NotaLibre crearNotaParaUsuario(NotaLibre nota, List<Long> etiquetaIds, Long userId);
    NotaLibre actualizarNotaPropia(Long id, NotaLibre nota, List<Long> etiquetaIds, Long userId);
    void eliminarNotaPropia(Long id, Long userId);
}
