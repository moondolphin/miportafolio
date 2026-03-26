package moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto;

import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TareaRequest {
    private String titulo;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Prioridad prioridad;
    private EstadoTarea estado;
    private List<Long> etiquetaIds;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }
    public List<Long> getEtiquetaIds() { return etiquetaIds; }
    public void setEtiquetaIds(List<Long> etiquetaIds) { this.etiquetaIds = etiquetaIds; }
}
