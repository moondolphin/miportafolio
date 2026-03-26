package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.JournalJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.EtiquetaJpaRepository;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.JournalJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import moondolphin.miportafolio.domain.port.out.agenda.JournalRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JournalRepositoryAdapter implements JournalRepositoryPort {

    private final JournalJpaRepository jpaRepository;
    private final EtiquetaJpaRepository etiquetaJpaRepository;

    public JournalRepositoryAdapter(JournalJpaRepository jpaRepository,
                                     EtiquetaJpaRepository etiquetaJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.etiquetaJpaRepository = etiquetaJpaRepository;
    }

    @Override
    public List<JournalEntry> findAll() {
        return jpaRepository.findAll().stream().map(JournalJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<JournalEntry> findById(Long id) {
        return jpaRepository.findById(id).map(JournalJpaEntity::toDomain);
    }

    @Override
    public JournalEntry save(JournalEntry entry, List<Long> etiquetaIds) {
        JournalJpaEntity entity = new JournalJpaEntity();
        entity.setId(entry.getId());
        entity.setTitulo(entry.getTitulo());
        entity.setContenido(entry.getContenido());
        entity.setMoodId(entry.getMoodId());
        entity.setFechaReferencia(entry.getFechaReferencia());
        entity.setCreatedBy(entry.getCreatedBy());
        entity.setCreatedAt(entry.getCreatedAt());
        entity.setUpdatedAt(entry.getUpdatedAt());

        if (etiquetaIds != null && !etiquetaIds.isEmpty()) {
            List<EtiquetaJpaEntity> etiquetas = etiquetaJpaRepository.findByIdIn(etiquetaIds);
            entity.setEtiquetas(etiquetas);
        }

        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<JournalEntry> searchByTexto(String texto) {
        return jpaRepository.searchByTexto(texto).stream().map(JournalJpaEntity::toDomain).collect(Collectors.toList());
    }
}
