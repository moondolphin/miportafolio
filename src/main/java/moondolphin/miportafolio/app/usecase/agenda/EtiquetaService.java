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
    public List<Etiqueta> obtenerEtiquetasDelUsuario(Long userId) {
        return etiquetaRepository.findAllByCreatedBy(userId);
    }

    @Override
    public Etiqueta crearEtiquetaParaUsuario(Etiqueta etiqueta, Long userId) {
        etiqueta.setCreatedBy(userId);
        etiqueta.setCreatedAt(LocalDateTime.now());
        return etiquetaRepository.save(etiqueta);
    }

    @Override
    public Etiqueta actualizarEtiquetaPropia(Long id, Etiqueta etiqueta, Long userId) {
        Etiqueta existente = etiquetaRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Etiqueta no encontrada"));
        etiqueta.setId(id);
        etiqueta.setCreatedBy(userId);
        etiqueta.setCreatedAt(existente.getCreatedAt());
        return etiquetaRepository.save(etiqueta);
    }

    @Override
    public void eliminarEtiquetaPropia(Long id, Long userId) {
        etiquetaRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Etiqueta no encontrada"));
        etiquetaRepository.deleteById(id);
    }
}
