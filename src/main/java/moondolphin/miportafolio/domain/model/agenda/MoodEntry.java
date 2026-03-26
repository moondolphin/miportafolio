package moondolphin.miportafolio.domain.model.agenda;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MoodEntry {

    private Long id;
    private LocalDate fecha;
    private EstadoAnimo estadoAnimo;
    private String nota;
    private Long createdBy;
    private LocalDateTime createdAt;

    public MoodEntry() {}

    public MoodEntry(Long id, LocalDate fecha, EstadoAnimo estadoAnimo,
                     String nota, Long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.fecha = fecha;
        this.estadoAnimo = estadoAnimo;
        this.nota = nota;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public EstadoAnimo getEstadoAnimo() { return estadoAnimo; }
    public void setEstadoAnimo(EstadoAnimo estadoAnimo) { this.estadoAnimo = estadoAnimo; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
