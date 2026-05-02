package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.NotaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.EtiquetaJpaRepository;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.NotaJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import moondolphin.miportafolio.domain.port.out.agenda.NotaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NotaRepositoryAdapter implements NotaRepositoryPort {

    private final NotaJpaRepository jpaRepository;
    private final EtiquetaJpaRepository etiquetaJpaRepository;

    public NotaRepositoryAdapter(NotaJpaRepository jpaRepository,
                                  EtiquetaJpaRepository etiquetaJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.etiquetaJpaRepository = etiquetaJpaRepository;
    }

    @Override
    public List<NotaLibre> findAllByCreatedBy(Long userId) {
        return jpaRepository.findAllByCreatedByOrderByCreatedAtDesc(userId).stream()
                .map(NotaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<NotaLibre> findByIdAndCreatedBy(Long id, Long userId) {
        return jpaRepository.findByIdAndCreatedBy(id, userId).map(NotaJpaEntity::toDomain);
    }

    @Override
    public NotaLibre save(NotaLibre nota, List<Long> etiquetaIds) {
        NotaJpaEntity entity = new NotaJpaEntity();
        entity.setId(nota.getId());
        entity.setTitulo(nota.getTitulo());
        entity.setContenido(nota.getContenido());
        entity.setFechaReferencia(nota.getFechaReferencia());
        entity.setCreatedBy(nota.getCreatedBy());
        entity.setCreatedAt(nota.getCreatedAt());
        entity.setUpdatedAt(nota.getUpdatedAt());

        if (etiquetaIds != null && !etiquetaIds.isEmpty()) {
            List<EtiquetaJpaEntity> etiquetas = etiquetaJpaRepository.findByIdInAndCreatedBy(etiquetaIds, nota.getCreatedBy());
            entity.setEtiquetas(etiquetas);
        }

        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<NotaLibre> searchByTextoAndCreatedBy(String texto, Long userId) {
        return jpaRepository.searchByTextoAndCreatedBy(texto, userId).stream()
                .map(NotaJpaEntity::toDomain).collect(Collectors.toList());
    }
}
