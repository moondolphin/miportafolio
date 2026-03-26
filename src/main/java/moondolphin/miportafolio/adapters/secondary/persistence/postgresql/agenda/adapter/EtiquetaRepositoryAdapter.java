package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.EtiquetaJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import moondolphin.miportafolio.domain.port.out.agenda.EtiquetaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EtiquetaRepositoryAdapter implements EtiquetaRepositoryPort {

    private final EtiquetaJpaRepository jpaRepository;

    public EtiquetaRepositoryAdapter(EtiquetaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Etiqueta> findAll() {
        return jpaRepository.findAll().stream().map(EtiquetaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Etiqueta> findById(Long id) {
        return jpaRepository.findById(id).map(EtiquetaJpaEntity::toDomain);
    }

    @Override
    public List<Etiqueta> findByIds(List<Long> ids) {
        return jpaRepository.findByIdIn(ids).stream().map(EtiquetaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Etiqueta save(Etiqueta etiqueta) {
        return jpaRepository.save(EtiquetaJpaEntity.fromDomain(etiqueta)).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
