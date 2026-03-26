package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.agenda.EstadoRecordatorio;
import moondolphin.miportafolio.domain.model.agenda.Recordatorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "agenda_reminders")
public class RecordatorioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    private EstadoRecordatorio estado;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "agenda_reminder_tags",
        joinColumns = @JoinColumn(name = "reminder_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<EtiquetaJpaEntity> etiquetas = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Recordatorio toDomain() {
        return new Recordatorio(id, titulo, descripcion, fecha, hora, estado,
                etiquetas.stream().map(EtiquetaJpaEntity::toDomain).collect(Collectors.toList()),
                createdBy, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public EstadoRecordatorio getEstado() { return estado; }
    public void setEstado(EstadoRecordatorio estado) { this.estado = estado; }
    public List<EtiquetaJpaEntity> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<EtiquetaJpaEntity> etiquetas) { this.etiquetas = etiquetas; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
