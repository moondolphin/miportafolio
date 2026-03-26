package moondolphin.miportafolio.adapters.secondary.persistence.postgresql;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.Proyecto;

@Entity
@Table(name = "proyectos")
public class ProyectoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;

    @Column(name = "url_imagen")
    private String urlImagen;

    public Proyecto toDomain() {
        return new Proyecto(id, nombre, descripcion, urlImagen);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
}
