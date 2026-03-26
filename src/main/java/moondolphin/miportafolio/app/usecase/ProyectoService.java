package moondolphin.miportafolio.app.usecase;

import moondolphin.miportafolio.domain.model.Proyecto;
import moondolphin.miportafolio.domain.port.in.ProyectoServicePort;
import moondolphin.miportafolio.domain.port.out.ProyectoRepositoryPort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProyectoService implements ProyectoServicePort {

    private final ProyectoRepositoryPort repositoryPort;

    public ProyectoService(ProyectoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<Proyecto> obtenerTodos() {
        return repositoryPort.findAll();
    }

    @Override
    public List<Proyecto> buscarPorNombre(String nombre) {
        return repositoryPort.findByNombre(nombre);
    }

    @Override
    public List<Proyecto> buscarPorTerminoEnDescripcion(String termino) {
        return repositoryPort.buscarPorTerminoEnDescripcion(termino);
    }
}
