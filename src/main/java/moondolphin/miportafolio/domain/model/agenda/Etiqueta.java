package moondolphin.miportafolio.domain.model.agenda;

import java.time.LocalDateTime;

public class Etiqueta {

    private Long id;
    private String nombre;
    private String color;
    private Long createdBy;
    private LocalDateTime createdAt;

    public Etiqueta() {}

    public Etiqueta(Long id, String nombre, String color, Long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
