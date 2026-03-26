package moondolphin.miportafolio.domain.model.agenda;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

public class Recordatorio {

    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime hora;
    private EstadoRecordatorio estado;
    private List<Etiqueta> etiquetas;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Recordatorio() {}

    public Recordatorio(Long id, String titulo, String descripcion, LocalDate fecha,
                        LocalTime hora, EstadoRecordatorio estado, List<Etiqueta> etiquetas,
                        Long createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.etiquetas = etiquetas;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    public List<Etiqueta> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<Etiqueta> etiquetas) { this.etiquetas = etiquetas; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
