package moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.adapter;

import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.entity.UsuarioJpaEntity;
import moondolphin.miportafolio.adapters.secondary.persistence.postgresql.agenda.repository.UsuarioJpaRepository;
import moondolphin.miportafolio.domain.model.agenda.Usuario;
import moondolphin.miportafolio.domain.port.out.agenda.UsuarioRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findByUsernameOrEmail(String username, String email) {
        return jpaRepository.findByUsernameOrEmail(username, email).map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id).map(UsuarioJpaEntity::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll().stream().map(UsuarioJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Usuario> findByApprovedFalse() {
        return jpaRepository.findByApprovedFalse().stream().map(UsuarioJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Usuario save(Usuario usuario) {
        return jpaRepository.save(UsuarioJpaEntity.fromDomain(usuario)).toDomain();
    }
}
