package moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto;

import java.time.LocalDate;
import java.util.List;

public class JournalRequest {
    private String titulo;
    private String contenido;
    private Long moodId;
    private LocalDate fechaReferencia;
    private List<Long> etiquetaIds;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public Long getMoodId() { return moodId; }
    public void setMoodId(Long moodId) { this.moodId = moodId; }
    public LocalDate getFechaReferencia() { return fechaReferencia; }
    public void setFechaReferencia(LocalDate fechaReferencia) { this.fechaReferencia = fechaReferencia; }
    public List<Long> getEtiquetaIds() { return etiquetaIds; }
    public void setEtiquetaIds(List<Long> etiquetaIds) { this.etiquetaIds = etiquetaIds; }
}
