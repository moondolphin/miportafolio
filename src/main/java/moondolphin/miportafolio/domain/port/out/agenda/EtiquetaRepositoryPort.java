package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import java.util.List;
import java.util.Optional;

public interface EtiquetaRepositoryPort {
    List<Etiqueta> findAllByCreatedBy(Long userId);
    Optional<Etiqueta> findByIdAndCreatedBy(Long id, Long userId);
    List<Etiqueta> findByIdsAndCreatedBy(List<Long> ids, Long userId);
    Etiqueta save(Etiqueta etiqueta);
    void deleteById(Long id);
}
