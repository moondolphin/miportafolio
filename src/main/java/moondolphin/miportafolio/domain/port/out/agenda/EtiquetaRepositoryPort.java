package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import java.util.List;
import java.util.Optional;

public interface EtiquetaRepositoryPort {
    List<Etiqueta> findAll();
    Optional<Etiqueta> findById(Long id);
    List<Etiqueta> findByIds(List<Long> ids);
    Etiqueta save(Etiqueta etiqueta);
    void deleteById(Long id);
}
