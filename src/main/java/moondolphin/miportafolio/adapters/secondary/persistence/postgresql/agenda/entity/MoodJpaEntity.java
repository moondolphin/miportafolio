package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agenda_moods")
public class MoodJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_animo", nullable = false)
    private EstadoAnimo estadoAnimo;

    private String nota;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public MoodEntry toDomain() {
        return new MoodEntry(id, fecha, estadoAnimo, nota, createdBy, createdAt);
    }

    public static MoodJpaEntity fromDomain(MoodEntry m) {
        MoodJpaEntity e = new MoodJpaEntity();
        e.id = m.getId();
        e.fecha = m.getFecha();
        e.estadoAnimo = m.getEstadoAnimo();
        e.nota = m.getNota();
        e.createdBy = m.getCreatedBy();
        e.createdAt = m.getCreatedAt();
        return e;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public EstadoAnimo getEstadoAnimo() { return estadoAnimo; }
    public void setEstadoAnimo(EstadoAnimo estadoAnimo) { this.estadoAnimo = estadoAnimo; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
