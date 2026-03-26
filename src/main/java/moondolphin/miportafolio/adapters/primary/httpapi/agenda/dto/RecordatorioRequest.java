package moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto;

import moondolphin.miportafolio.domain.model.agenda.EstadoRecordatorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class RecordatorioRequest {
    private String titulo;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime hora;
    private EstadoRecordatorio estado;
    private List<Long> etiquetaIds;

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
    public List<Long> getEtiquetaIds() { return etiquetaIds; }
    public void setEtiquetaIds(List<Long> etiquetaIds) { this.etiquetaIds = etiquetaIds; }
}
