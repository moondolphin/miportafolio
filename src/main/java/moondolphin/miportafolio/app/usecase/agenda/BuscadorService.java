package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.port.in.agenda.BuscadorServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.JournalRepositoryPort;
import moondolphin.miportafolio.domain.port.out.agenda.LinkRepositoryPort;
import moondolphin.miportafolio.domain.port.out.agenda.NotaRepositoryPort;
import moondolphin.miportafolio.domain.port.out.agenda.TareaRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class BuscadorService implements BuscadorServicePort {

    private final TareaRepositoryPort tareaRepository;
    private final NotaRepositoryPort notaRepository;
    private final JournalRepositoryPort journalRepository;
    private final LinkRepositoryPort linkRepository;

    public BuscadorService(TareaRepositoryPort tareaRepository,
                           NotaRepositoryPort notaRepository,
                           JournalRepositoryPort journalRepository,
                           LinkRepositoryPort linkRepository) {
        this.tareaRepository = tareaRepository;
        this.notaRepository = notaRepository;
        this.journalRepository = journalRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    public Resultado buscar(String texto) {
        return new Resultado(
                tareaRepository.searchByTexto(texto),
                notaRepository.searchByTexto(texto),
                journalRepository.searchByTexto(texto),
                linkRepository.searchByTexto(texto)
        );
    }
}
