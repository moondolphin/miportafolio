package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import java.util.List;
import java.util.Optional;

public interface RecordatorioServicePort {
    List<Recordatorio> obtenerTodos();
    Optional<Recordatorio> obtenerPorId(Long id);
    Recordatorio crear(Recordatorio recordatorio, List<Long> etiquetaIds);
    Recordatorio actualizar(Long id, Recordatorio recordatorio, List<Long> etiquetaIds);
    void eliminar(Long id);
}
