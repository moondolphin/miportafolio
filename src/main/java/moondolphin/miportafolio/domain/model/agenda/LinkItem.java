package moondolphin.miportafolio.domain.model.agenda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LinkItem {

    private Long id;
    private String titulo;
    private String url;
    private String descripcion;
    private LocalDate fechaReferencia;
    private List<Etiqueta> etiquetas;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LinkItem() {}

    public LinkItem(Long id, String titulo, String url, String descripcion,
                    LocalDate fechaReferencia, List<Etiqueta> etiquetas,
                    Long createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.titulo = titulo;
        this.url = url;
        this.descripcion = descripcion;
        this.fechaReferencia = fechaReferencia;
        this.etiquetas = etiquetas;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    public List<Etiqueta> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<Etiqueta> etiquetas) { this.etiquetas = etiquetas; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
