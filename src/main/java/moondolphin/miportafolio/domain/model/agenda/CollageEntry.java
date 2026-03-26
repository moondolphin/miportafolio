package moondolphin.miportafolio.domain.model.agenda;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CollageEntry {

    private Long id;
    private String titulo;
    private String imageUrl;
    private String gifUrl;
    private String quote;
    private LocalDate fechaReferencia;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CollageEntry() {}

    public CollageEntry(Long id, String titulo, String imageUrl, String gifUrl,
                        String quote, LocalDate fechaReferencia, Long createdBy,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.titulo = titulo;
        this.imageUrl = imageUrl;
        this.gifUrl = gifUrl;
        this.quote = quote;
        this.fechaReferencia = fechaReferencia;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
