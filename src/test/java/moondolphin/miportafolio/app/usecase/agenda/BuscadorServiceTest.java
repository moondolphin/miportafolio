package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import moondolphin.miportafolio.domain.port.in.agenda.BuscadorServicePort;
import moondolphin.miportafolio.domain.port.out.agenda.JournalRepositoryPort;
import moondolphin.miportafolio.domain.port.out.agenda.LinkRepositoryPort;
import moondolphin.miportafolio.domain.port.out.agenda.NotaRepositoryPort;
import moondolphin.miportafolio.domain.port.out.agenda.TareaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuscadorServiceTest {

    @Mock
    private TareaRepositoryPort tareaRepository;
    @Mock
    private NotaRepositoryPort notaRepository;
    @Mock
    private JournalRepositoryPort journalRepository;
    @Mock
    private LinkRepositoryPort linkRepository;

    private BuscadorService service;

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new BuscadorService(tareaRepository, notaRepository, journalRepository, linkRepository);
    }

    // --- buscarEnAgendaDelUsuario ---

    @Test
    void buscarEnAgendaDelUsuario_consultaTodosLosRepositoriosFiltradosPorUsuario() {
        String texto = "Java";
        List<Tarea> tareas = List.of(new Tarea());
        List<NotaLibre> notas = List.of(new NotaLibre(), new NotaLibre());
        List<JournalEntry> journal = List.of();
        List<LinkItem> links = List.of(new LinkItem());
        when(tareaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(tareas);
        when(notaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(notas);
        when(journalRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(journal);
        when(linkRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(links);

        BuscadorServicePort.Resultado resultado = service.buscarEnAgendaDelUsuario(texto, USER_ID);

        assertThat(resultado.getTareas()).hasSize(1);
        assertThat(resultado.getNotas()).hasSize(2);
        assertThat(resultado.getJournal()).isEmpty();
        assertThat(resultado.getLinks()).hasSize(1);
        verify(tareaRepository).searchByTextoAndCreatedBy(texto, USER_ID);
        verify(notaRepository).searchByTextoAndCreatedBy(texto, USER_ID);
        verify(journalRepository).searchByTextoAndCreatedBy(texto, USER_ID);
        verify(linkRepository).searchByTextoAndCreatedBy(texto, USER_ID);
    }

    @Test
    void buscarEnAgendaDelUsuario_noDevuelveDatosDeOtrosUsuarios() {
        String texto = "Java";
        when(tareaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of(new Tarea()));
        when(notaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(journalRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(linkRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());

        service.buscarEnAgendaDelUsuario(texto, USER_ID);

        verify(tareaRepository, never()).searchByTextoAndCreatedBy(texto, OTRO_USER_ID);
        verify(notaRepository, never()).searchByTextoAndCreatedBy(texto, OTRO_USER_ID);
        verify(journalRepository, never()).searchByTextoAndCreatedBy(texto, OTRO_USER_ID);
        verify(linkRepository, never()).searchByTextoAndCreatedBy(texto, OTRO_USER_ID);
    }

    @Test
    void buscarEnAgendaDelUsuario_sinCoincidencias_retornaTodasLasListasVacias() {
        String texto = "xyzabc123";
        when(tareaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(notaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(journalRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(linkRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());

        BuscadorServicePort.Resultado resultado = service.buscarEnAgendaDelUsuario(texto, USER_ID);

        assertThat(resultado.getTareas()).isEmpty();
        assertThat(resultado.getNotas()).isEmpty();
        assertThat(resultado.getJournal()).isEmpty();
        assertThat(resultado.getLinks()).isEmpty();
    }

    @Test
    void buscarEnAgendaDelUsuario_conTextoVacio_consultaRepositoriosIgualmente() {
        String texto = "";
        when(tareaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(notaRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(journalRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());
        when(linkRepository.searchByTextoAndCreatedBy(texto, USER_ID)).thenReturn(List.of());

        service.buscarEnAgendaDelUsuario(texto, USER_ID);

        verify(tareaRepository).searchByTextoAndCreatedBy(texto, USER_ID);
        verify(notaRepository).searchByTextoAndCreatedBy(texto, USER_ID);
        verify(journalRepository).searchByTextoAndCreatedBy(texto, USER_ID);
        verify(linkRepository).searchByTextoAndCreatedBy(texto, USER_ID);
    }
}
