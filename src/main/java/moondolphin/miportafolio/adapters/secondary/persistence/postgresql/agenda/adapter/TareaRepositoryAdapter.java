package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.EtiquetaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.TareaJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.EtiquetaJpaRepository;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.TareaJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import moondolphin.miportafolio.domain.port.out.agenda.TareaRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TareaRepositoryAdapter implements TareaRepositoryPort {

    private final TareaJpaRepository jpaRepository;
    private final EtiquetaJpaRepository etiquetaJpaRepository;

    public TareaRepositoryAdapter(TareaJpaRepository jpaRepository,
                                   EtiquetaJpaRepository etiquetaJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.etiquetaJpaRepository = etiquetaJpaRepository;
    }

    @Override
    public List<Tarea> findAllByCreatedBy(Long userId) {
        return jpaRepository.findAllByCreatedByOrderByFechaDesc(userId).stream()
                .map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByFechaAndCreatedBy(LocalDate fecha, Long userId) {
        return jpaRepository.findByFechaAndCreatedBy(fecha, userId).stream()
                .map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByPrioridadAndCreatedBy(Prioridad prioridad, Long userId) {
        return jpaRepository.findByPrioridadAndCreatedBy(prioridad, userId).stream()
                .map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByEstadoAndCreatedBy(EstadoTarea estado, Long userId) {
        return jpaRepository.findByEstadoAndCreatedBy(estado, userId).stream()
                .map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByEtiquetaNombreAndCreatedBy(String nombre, Long userId) {
        return jpaRepository.findByEtiquetaNombreAndCreatedBy(nombre, userId).stream()
                .map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Tarea> findByIdAndCreatedBy(Long id, Long userId) {
        return jpaRepository.findByIdAndCreatedBy(id, userId).map(TareaJpaEntity::toDomain);
    }

    @Override
    public Tarea save(Tarea tarea, List<Long> etiquetaIds) {
        TareaJpaEntity entity = new TareaJpaEntity();
        entity.setId(tarea.getId());
        entity.setTitulo(tarea.getTitulo());
        entity.setDescripcion(tarea.getDescripcion());
        entity.setFecha(tarea.getFecha());
        entity.setHoraInicio(tarea.getHoraInicio());
        entity.setHoraFin(tarea.getHoraFin());
        entity.setPrioridad(tarea.getPrioridad());
        entity.setEstado(tarea.getEstado());
        entity.setCreatedBy(tarea.getCreatedBy());
        entity.setCreatedAt(tarea.getCreatedAt());
        entity.setUpdatedAt(tarea.getUpdatedAt());

        if (etiquetaIds != null && !etiquetaIds.isEmpty()) {
            List<EtiquetaJpaEntity> etiquetas = etiquetaJpaRepository.findByIdInAndCreatedBy(etiquetaIds, tarea.getCreatedBy());
            entity.setEtiquetas(etiquetas);
        }

        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Tarea> searchByTextoAndCreatedBy(String texto, Long userId) {
        return jpaRepository.searchByTextoAndCreatedBy(texto, userId).stream()
                .map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }
}
