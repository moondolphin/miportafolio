package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import moondolphin.miportafolio.domain.port.in.agenda.LinkServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.LinkRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LinkService implements LinkServicePort {

    private final LinkRepositoryPort linkRepository;

    public LinkService(LinkRepositoryPort linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public List<LinkItem> obtenerTodos() {
        return linkRepository.findAll();
    }

    @Override
    public Optional<LinkItem> obtenerPorId(Long id) {
        return linkRepository.findById(id);
    }

    @Override
    public LinkItem crear(LinkItem link, List<Long> etiquetaIds) {
        link.setCreatedAt(LocalDateTime.now());
        link.setUpdatedAt(LocalDateTime.now());
        return linkRepository.save(link, etiquetaIds);
    }

    @Override
    public LinkItem actualizar(Long id, LinkItem link, List<Long> etiquetaIds) {
        linkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Link no encontrado"));
        link.setId(id);
        link.setUpdatedAt(LocalDateTime.now());
        return linkRepository.save(link, etiquetaIds);
    }

    @Override
    public void eliminar(Long id) {
        linkRepository.deleteById(id);
    }
}
