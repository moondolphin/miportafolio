package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import java.util.List;
import java.util.Optional;

public interface NotaRepositoryPort {
    List<NotaLibre> findAllByCreatedBy(Long userId);
    Optional<NotaLibre> findByIdAndCreatedBy(Long id, Long userId);
    NotaLibre save(NotaLibre nota, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<NotaLibre> searchByTextoAndCreatedBy(String texto, Long userId);
}
