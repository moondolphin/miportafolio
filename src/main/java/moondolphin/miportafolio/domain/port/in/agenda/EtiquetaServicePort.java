package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import java.util.List;

public interface EtiquetaServicePort {
    List<Etiqueta> obtenerEtiquetasDelUsuario(Long userId);
    Etiqueta crearEtiquetaParaUsuario(Etiqueta etiqueta, Long userId);
    Etiqueta actualizarEtiquetaPropia(Long id, Etiqueta etiqueta, Long userId);
    void eliminarEtiquetaPropia(Long id, Long userId);
}
