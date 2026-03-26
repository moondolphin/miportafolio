package moondolphin.miportafolio.adapters.secondary.persistence.postgresql;

import moondolphin.miportafolio.domain.model.Proyecto;
import moondolphin.miportafolio.domain.port.out.ProyectoRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProyectoRepositoryAdapter implements ProyectoRepositoryPort {

    private final ProyectoJpaRepository jpaRepository;

    public ProyectoRepositoryAdapter(ProyectoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Proyecto> findAll() {
        return jpaRepository.findAll().stream()
                .map(ProyectoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Proyecto> findByNombre(String nombre) {
        return jpaRepository.findByNombre(nombre).stream()
                .map(ProyectoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Proyecto> buscarPorTerminoEnDescripcion(String termino) {
        return jpaRepository.buscarPorTerminoEnDescripcion(termino).stream()
                .map(ProyectoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}
