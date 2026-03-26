package moondolphin.miportafolio.domain.port.out.agenda;

import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import java.util.List;
import java.util.Optional;

public interface RecordatorioRepositoryPort {
    List<Recordatorio> findAll();
    Optional<Recordatorio> findById(Long id);
    Recordatorio save(Recordatorio recordatorio, List<Long> etiquetaIds);
    void deleteById(Long id);
}
