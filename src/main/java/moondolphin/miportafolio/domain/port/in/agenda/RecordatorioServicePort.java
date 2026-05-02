package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import java.util.List;
import java.util.Optional;

public interface RecordatorioServicePort {
    List<Recordatorio> obtenerRecordatoriosDelUsuario(Long userId);
    Optional<Recordatorio> obtenerRecordatorioPropioPorId(Long id, Long userId);
    Recordatorio crearRecordatorioParaUsuario(Recordatorio recordatorio, List<Long> etiquetaIds, Long userId);
    Recordatorio actualizarRecordatorioPropio(Long id, Recordatorio recordatorio, List<Long> etiquetaIds, Long userId);
    void eliminarRecordatorioPropio(Long id, Long userId);
}
