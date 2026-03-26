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
    public List<Tarea> findAll() {
        return jpaRepository.findAll().stream().map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByFecha(LocalDate fecha) {
        return jpaRepository.findByFecha(fecha).stream().map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByPrioridad(Prioridad prioridad) {
        return jpaRepository.findByPrioridad(prioridad).stream().map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByEstado(EstadoTarea estado) {
        return jpaRepository.findByEstado(estado).stream().map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tarea> findByEtiquetaNombre(String nombre) {
        return jpaRepository.findByEtiquetaNombre(nombre).stream().map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Tarea> findById(Long id) {
        return jpaRepository.findById(id).map(TareaJpaEntity::toDomain);
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
    public List<Tarea> searchByTexto(String texto) {
        return jpaRepository.searchByTexto(texto).stream().map(TareaJpaEntity::toDomain).collect(Collectors.toList());
    }
}
