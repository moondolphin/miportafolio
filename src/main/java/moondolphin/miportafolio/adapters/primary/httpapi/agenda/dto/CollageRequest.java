package moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto;

import java.time.LocalDate;

public class CollageRequest {
    private String titulo;
    private String imageUrl;
    private String gifUrl;
    private String quote;
    private LocalDate fechaReferencia;

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
}
