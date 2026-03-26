package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.agenda.NotaLibre;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "agenda_notes")
public class NotaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_referencia")
    private LocalDate fechaReferencia;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "agenda_note_tags",
        joinColumns = @JoinColumn(name = "note_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<EtiquetaJpaEntity> etiquetas = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public NotaLibre toDomain() {
        return new NotaLibre(id, titulo, contenido, fechaReferencia,
                etiquetas.stream().map(EtiquetaJpaEntity::toDomain).collect(Collectors.toList()),
                createdBy, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDate getFechaReferencia() { return fechaReferencia; }
    public void setFechaReferencia(LocalDate fechaReferencia) { this.fechaReferencia = fechaReferencia; }
    public List<EtiquetaJpaEntity> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<EtiquetaJpaEntity> etiquetas) { this.etiquetas = etiquetas; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
