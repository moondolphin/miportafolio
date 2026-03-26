package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.agenda.LinkItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "agenda_links")
public class LinkJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String url;

    private String descripcion;

    @Column(name = "fecha_referencia")
    private LocalDate fechaReferencia;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "agenda_link_tags",
        joinColumns = @JoinColumn(name = "link_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<EtiquetaJpaEntity> etiquetas = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LinkItem toDomain() {
        return new LinkItem(id, titulo, url, descripcion, fechaReferencia,
                etiquetas.stream().map(EtiquetaJpaEntity::toDomain).collect(Collectors.toList()),
                createdBy, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
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
