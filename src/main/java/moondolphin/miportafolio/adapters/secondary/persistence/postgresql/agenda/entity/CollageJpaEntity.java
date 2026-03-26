package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.agenda.CollageEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agenda_collage")
public class CollageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "gif_url")
    private String gifUrl;

    @Column(name = "quote", columnDefinition = "TEXT")
    private String quote;

    @Column(name = "fecha_referencia")
    private LocalDate fechaReferencia;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CollageEntry toDomain() {
        return new CollageEntry(id, titulo, imageUrl, gifUrl, quote, fechaReferencia, createdBy, createdAt, updatedAt);
    }

    public static CollageJpaEntity fromDomain(CollageEntry c) {
        CollageJpaEntity e = new CollageJpaEntity();
        e.id = c.getId();
        e.titulo = c.getTitulo();
        e.imageUrl = c.getImageUrl();
        e.gifUrl = c.getGifUrl();
        e.quote = c.getQuote();
        e.fechaReferencia = c.getFechaReferencia();
        e.createdBy = c.getCreatedBy();
        e.createdAt = c.getCreatedAt();
        e.updatedAt = c.getUpdatedAt();
        return e;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }
    public String getQuote() { return quote; }
    public void setQuote(String quote) { this.quote = quote; }
    public LocalDate getFechaReferencia() { return fechaReferencia; }
    public void setFechaReferencia(LocalDate fechaReferencia) { this.fechaReferencia = fechaReferencia; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
