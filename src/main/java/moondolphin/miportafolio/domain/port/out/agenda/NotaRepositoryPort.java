package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import java.util.List;
import java.util.Optional;

public interface NotaRepositoryPort {
    List<NotaLibre> findAll();
    Optional<NotaLibre> findById(Long id);
    NotaLibre save(NotaLibre nota, List<Long> etiquetaIds);
    void deleteById(Long id);
    List<NotaLibre> searchByTexto(String texto);
}
