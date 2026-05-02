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
    public List<LinkItem> obtenerLinksDelUsuario(Long userId) {
        return linkRepository.findAllByCreatedBy(userId);
    }

    @Override
    public Optional<LinkItem> obtenerLinkPropioPorId(Long id, Long userId) {
        return linkRepository.findByIdAndCreatedBy(id, userId);
    }

    @Override
    public LinkItem crearLinkParaUsuario(LinkItem link, List<Long> etiquetaIds, Long userId) {
        link.setCreatedBy(userId);
        link.setCreatedAt(LocalDateTime.now());
        link.setUpdatedAt(LocalDateTime.now());
        return linkRepository.save(link, etiquetaIds);
    }

    @Override
    public LinkItem actualizarLinkPropio(Long id, LinkItem link, List<Long> etiquetaIds, Long userId) {
        linkRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Link no encontrado"));
        link.setId(id);
        link.setCreatedBy(userId);
        link.setUpdatedAt(LocalDateTime.now());
        return linkRepository.save(link, etiquetaIds);
    }

    @Override
    public void eliminarLinkPropio(Long id, Long userId) {
        linkRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new NotFoundException("Link no encontrado"));
        linkRepository.deleteById(id);
    }
}
