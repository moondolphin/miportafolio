package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.agenda.Etiqueta;

import java.time.LocalDateTime;

@Entity
@Table(name = "agenda_tags")
public class EtiquetaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    private String color;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Etiqueta toDomain() {
        return new Etiqueta(id, nombre, color, createdAt);
    }

    public static EtiquetaJpaEntity fromDomain(Etiqueta e) {
        EtiquetaJpaEntity entity = new EtiquetaJpaEntity();
        entity.id = e.getId();
        entity.nombre = e.getNombre();
        entity.color = e.getColor();
        entity.createdAt = e.getCreatedAt();
        return entity;
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
