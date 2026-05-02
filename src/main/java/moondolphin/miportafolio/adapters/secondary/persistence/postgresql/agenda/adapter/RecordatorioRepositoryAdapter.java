package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.RecordatorioJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.EtiquetaJpaRepository;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.RecordatorioJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import moondolphin.miportafolio.domain.port.out.agenda.RecordatorioRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RecordatorioRepositoryAdapter implements RecordatorioRepositoryPort {

    private final RecordatorioJpaRepository jpaRepository;
    private final EtiquetaJpaRepository etiquetaJpaRepository;

    public RecordatorioRepositoryAdapter(RecordatorioJpaRepository jpaRepository,
                                          EtiquetaJpaRepository etiquetaJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.etiquetaJpaRepository = etiquetaJpaRepository;
    }

    @Override
    public List<Recordatorio> findAllByCreatedBy(Long userId) {
        return jpaRepository.findAllByCreatedByOrderByFechaAsc(userId).stream()
                .map(RecordatorioJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Recordatorio> findByIdAndCreatedBy(Long id, Long userId) {
        return jpaRepository.findByIdAndCreatedBy(id, userId).map(RecordatorioJpaEntity::toDomain);
    }

    @Override
    public Recordatorio save(Recordatorio recordatorio, List<Long> etiquetaIds) {
        RecordatorioJpaEntity entity = new RecordatorioJpaEntity();
        entity.setId(recordatorio.getId());
        entity.setTitulo(recordatorio.getTitulo());
        entity.setDescripcion(recordatorio.getDescripcion());
        entity.setFecha(recordatorio.getFecha());
        entity.setHora(recordatorio.getHora());
        entity.setEstado(recordatorio.getEstado());
        entity.setCreatedBy(recordatorio.getCreatedBy());
        entity.setCreatedAt(recordatorio.getCreatedAt());
        entity.setUpdatedAt(recordatorio.getUpdatedAt());

        if (etiquetaIds != null && !etiquetaIds.isEmpty()) {
            List<EtiquetaJpaEntity> etiquetas = etiquetaJpaRepository.findByIdInAndCreatedBy(etiquetaIds, recordatorio.getCreatedBy());
            entity.setEtiquetas(etiquetas);
        }

        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
