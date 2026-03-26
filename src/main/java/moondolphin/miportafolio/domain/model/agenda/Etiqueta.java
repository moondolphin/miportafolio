package moondolphin.miportafolio.domain.model.agenda;

import java.time.LocalDateTime;

public class Etiqueta {

    private Long id;
    private String nombre;
    private String color;
    private LocalDateTime createdAt;

    public Etiqueta() {}

    public Etiqueta(Long id, String nombre, String color, LocalDateTime createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
