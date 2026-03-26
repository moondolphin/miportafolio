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
    public List<Recordatorio> obtenerTodos() {
        return recordatorioRepository.findAll();
    }

    @Override
    public Optional<Recordatorio> obtenerPorId(Long id) {
        return recordatorioRepository.findById(id);
    }

    @Override
    public Recordatorio crear(Recordatorio recordatorio, List<Long> etiquetaIds) {
        recordatorio.setCreatedAt(LocalDateTime.now());
        recordatorio.setUpdatedAt(LocalDateTime.now());
        return recordatorioRepository.save(recordatorio, etiquetaIds);
    }

    @Override
    public Recordatorio actualizar(Long id, Recordatorio recordatorio, List<Long> etiquetaIds) {
        recordatorioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recordatorio no encontrado"));
        recordatorio.setId(id);
        recordatorio.setUpdatedAt(LocalDateTime.now());
        return recordatorioRepository.save(recordatorio, etiquetaIds);
    }

    @Override
    public void eliminar(Long id) {
        recordatorioRepository.deleteById(id);
    }
}
