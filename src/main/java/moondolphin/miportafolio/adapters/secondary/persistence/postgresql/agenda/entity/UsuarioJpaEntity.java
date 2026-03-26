package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity;

import jakarta.persistence.*;
import moondolphin.miportafolio.domain.model.agenda.Role;
import moondolphin.miportafolio.domain.model.agenda.Usuario;

import java.time.LocalDateTime;

@Entity
@Table(name = "agenda_users")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean approved;
    private boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Usuario toDomain() {
        return new Usuario(id, username, email, passwordHash, role, approved, active, createdAt, updatedAt);
    }

    public static UsuarioJpaEntity fromDomain(Usuario u) {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        e.id = u.getId();
        e.username = u.getUsername();
        e.email = u.getEmail();
        e.passwordHash = u.getPasswordHash();
        e.role = u.getRole();
        e.approved = u.isApproved();
        e.active = u.isActive();
        e.createdAt = u.getCreatedAt();
        e.updatedAt = u.getUpdatedAt();
        return e;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
