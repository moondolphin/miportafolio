package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByUsername(String username);
    Optional<UsuarioJpaEntity> findByEmail(String email);
    Optional<UsuarioJpaEntity> findByUsernameOrEmail(String username, String email);
    List<UsuarioJpaEntity> findByApprovedFalse();
}
