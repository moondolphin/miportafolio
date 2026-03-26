package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import moondolphin.miportafolio.domain.port.in.agenda.EtiquetaServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.EtiquetaRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EtiquetaService implements EtiquetaServicePort {

    private final EtiquetaRepositoryPort etiquetaRepository;

    public EtiquetaService(EtiquetaRepositoryPort etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    @Override
    public List<Etiqueta> obtenerTodas() {
        return etiquetaRepository.findAll();
    }

    @Override
    public Etiqueta crear(Etiqueta etiqueta) {
        etiqueta.setCreatedAt(LocalDateTime.now());
        return etiquetaRepository.save(etiqueta);
    }

    @Override
    public Etiqueta actualizar(Long id, Etiqueta etiqueta) {
        etiquetaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Etiqueta no encontrada"));
        etiqueta.setId(id);
        return etiquetaRepository.save(etiqueta);
    }

    @Override
    public void eliminar(Long id) {
        etiquetaRepository.deleteById(id);
    }
}
