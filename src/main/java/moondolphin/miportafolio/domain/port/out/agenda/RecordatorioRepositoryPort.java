package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import java.util.List;
import java.util.Optional;

public interface RecordatorioRepositoryPort {
    List<Recordatorio> findAllByCreatedBy(Long userId);
    Optional<Recordatorio> findByIdAndCreatedBy(Long id, Long userId);
    Recordatorio save(Recordatorio recordatorio, List<Long> etiquetaIds);
    void deleteById(Long id);
}
