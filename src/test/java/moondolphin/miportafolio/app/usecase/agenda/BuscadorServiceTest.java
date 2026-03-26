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

    @BeforeEach
    void setUp() {
        service = new BuscadorService(tareaRepository, notaRepository, journalRepository, linkRepository);
    }

    // --- buscar ---

    @Test
    void buscar_consultaTodosLosRepositoriosYAgregaResultados() {
        // Arrange
        String texto = "Java";
        List<Tarea> tareas = List.of(new Tarea());
        List<NotaLibre> notas = List.of(new NotaLibre(), new NotaLibre());
        List<JournalEntry> journal = List.of();
        List<LinkItem> links = List.of(new LinkItem());
        when(tareaRepository.searchByTexto(texto)).thenReturn(tareas);
        when(notaRepository.searchByTexto(texto)).thenReturn(notas);
        when(journalRepository.searchByTexto(texto)).thenReturn(journal);
        when(linkRepository.searchByTexto(texto)).thenReturn(links);

        // Act
        BuscadorServicePort.Resultado resultado = service.buscar(texto);

        // Assert
        assertThat(resultado.getTareas()).hasSize(1);
        assertThat(resultado.getNotas()).hasSize(2);
        assertThat(resultado.getJournal()).isEmpty();
        assertThat(resultado.getLinks()).hasSize(1);
        verify(tareaRepository).searchByTexto(texto);
        verify(notaRepository).searchByTexto(texto);
        verify(journalRepository).searchByTexto(texto);
        verify(linkRepository).searchByTexto(texto);
    }

    @Test
    void buscar_sinCoincidencias_retornaTodasLasListasVacias() {
        // Arrange
        String texto = "xyzabc123";
        when(tareaRepository.searchByTexto(texto)).thenReturn(List.of());
        when(notaRepository.searchByTexto(texto)).thenReturn(List.of());
        when(journalRepository.searchByTexto(texto)).thenReturn(List.of());
        when(linkRepository.searchByTexto(texto)).thenReturn(List.of());

        // Act
        BuscadorServicePort.Resultado resultado = service.buscar(texto);

        // Assert
        assertThat(resultado.getTareas()).isEmpty();
        assertThat(resultado.getNotas()).isEmpty();
        assertThat(resultado.getJournal()).isEmpty();
        assertThat(resultado.getLinks()).isEmpty();
    }

    @Test
    void buscar_conTextoVacio_consultaRepositoriosIgualmente() {
        // Arrange — texto vacío es un caso válido; la responsabilidad de filtrar es del repositorio
        String texto = "";
        when(tareaRepository.searchByTexto(texto)).thenReturn(List.of());
        when(notaRepository.searchByTexto(texto)).thenReturn(List.of());
        when(journalRepository.searchByTexto(texto)).thenReturn(List.of());
        when(linkRepository.searchByTexto(texto)).thenReturn(List.of());

        // Act
        service.buscar(texto);

        // Assert — los 4 repositorios deben ser invocados independientemente del texto
        verify(tareaRepository).searchByTexto(texto);
        verify(notaRepository).searchByTexto(texto);
        verify(journalRepository).searchByTexto(texto);
        verify(linkRepository).searchByTexto(texto);
    }
}
