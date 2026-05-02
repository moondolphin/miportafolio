package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import moondolphin.miportafolio.domain.port.in.agenda.RecordatorioServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.RecordatorioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecordatorioService implements RecordatorioServicePort {

    private final RecordatorioRepositoryPort recordatorioRepository;

    public RecordatorioService(RecordatorioRepositoryPort recordatorioRepository) {
        this.recordatorioRepository = recordatorioRepository;
    }

    @Override
    public List<Recordatorio> obtenerRecordatoriosDelUsuario(Long userId) {
        return recordatorioRepository.findAllByCreatedBy(userId);
    }

    @Override
    public Optional<Recordatorio> obtenerRecordatorioPropioPorId(Long id, Long userId) {
        return recordatorioRepository.findByIdAndCreatedBy(id, userId);
    }

    @Override
    public Recordatorio crearRecordatorioParaUsuario(Recordatorio recordatorio, List<Long> etiquetaIds, Long userId) {
        recordatorio.setCreatedBy(userId);
        recordatorio.setCreatedAt(LocalDateTime.now());
        recordatorio.setUpdatedAt(LocalDateTime.now());
        return recordatorioRepository.save(recordatorio, etiquetaIds);
    }

    @Override
    public Recordatorio actualizarRecordatorioPropio(Long id, Recordatorio recordatorio, List<Long> etiquetaIds, Long userId) {
        recordatorioRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Recordatorio no encontrado"));
        recordatorio.setId(id);
        recordatorio.setCreatedBy(userId);
        recordatorio.setUpdatedAt(LocalDateTime.now());
        return recordatorioRepository.save(recordatorio, etiquetaIds);
    }

    @Override
    public void eliminarRecordatorioPropio(Long id, Long userId) {
        recordatorioRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Recordatorio no encontrado"));
        recordatorioRepository.deleteById(id);
    }
}
