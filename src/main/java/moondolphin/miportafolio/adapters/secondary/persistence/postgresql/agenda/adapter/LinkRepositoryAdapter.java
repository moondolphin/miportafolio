package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.LinkJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.EtiquetaJpaRepository;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.LinkJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import moondolphin.miportafolio.domain.port.out.agenda.LinkRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LinkRepositoryAdapter implements LinkRepositoryPort {

    private final LinkJpaRepository jpaRepository;
    private final EtiquetaJpaRepository etiquetaJpaRepository;

    public LinkRepositoryAdapter(LinkJpaRepository jpaRepository,
                                  EtiquetaJpaRepository etiquetaJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.etiquetaJpaRepository = etiquetaJpaRepository;
    }

    @Override
    public List<LinkItem> findAllByCreatedBy(Long userId) {
        return jpaRepository.findAllByCreatedByOrderByCreatedAtDesc(userId).stream()
                .map(LinkJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<LinkItem> findByIdAndCreatedBy(Long id, Long userId) {
        return jpaRepository.findByIdAndCreatedBy(id, userId).map(LinkJpaEntity::toDomain);
    }

    @Override
    public LinkItem save(LinkItem link, List<Long> etiquetaIds) {
        LinkJpaEntity entity = new LinkJpaEntity();
        entity.setId(link.getId());
        entity.setTitulo(link.getTitulo());
        entity.setUrl(link.getUrl());
        entity.setDescripcion(link.getDescripcion());
        entity.setFechaReferencia(link.getFechaReferencia());
        entity.setCreatedBy(link.getCreatedBy());
        entity.setCreatedAt(link.getCreatedAt());
        entity.setUpdatedAt(link.getUpdatedAt());

        if (etiquetaIds != null && !etiquetaIds.isEmpty()) {
            List<EtiquetaJpaEntity> etiquetas = etiquetaJpaRepository.findByIdInAndCreatedBy(etiquetaIds, link.getCreatedBy());
            entity.setEtiquetas(etiquetas);
        }

        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<LinkItem> searchByTextoAndCreatedBy(String texto, Long userId) {
        return jpaRepository.searchByTextoAndCreatedBy(texto, userId).stream()
                .map(LinkJpaEntity::toDomain).collect(Collectors.toList());
    }
}
