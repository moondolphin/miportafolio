package moondolphin.miportafolio.adapters.primary.httpapi.agenda.dto;

import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;

import java.time.LocalDate;

public class MoodRequest {
    private LocalDate fecha;
    private EstadoAnimo estadoAnimo;
    private String nota;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public EstadoAnimo getEstadoAnimo() { return estadoAnimo; }
    public void setEstadoAnimo(EstadoAnimo estadoAnimo) { this.estadoAnimo = estadoAnimo; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}
