package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;
import moondolphin.miportafolio.domain.port.out.agenda.MoodRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoodServiceTest {

    @Mock
    private MoodRepositoryPort moodRepository;

    private MoodService service;

    @BeforeEach
    void setUp() {
        service = new MoodService(moodRepository);
    }

    // --- obtenerTodos ---

    @Test
    void obtenerTodos_retornaTodosLosRegistros() {
        // Arrange
        List<MoodEntry> entries = List.of(new MoodEntry(), new MoodEntry());
        when(moodRepository.findAll()).thenReturn(entries);

        // Act
        List<MoodEntry> resultado = service.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(moodRepository).findAll();
    }

    @Test
    void obtenerTodos_conListaVacia_retornaListaVacia() {
        // Arrange
        when(moodRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodos()).isEmpty();
    }

    // --- registrar ---

    @Test
    void registrar_asignaCreatedAtYGuarda() {
        // Arrange
        MoodEntry entry = new MoodEntry();
        entry.setFecha(LocalDate.now());
        entry.setEstadoAnimo(EstadoAnimo.MUY_BIEN);
        MoodEntry guardado = new MoodEntry();
        guardado.setId(1L);
        when(moodRepository.save(entry)).thenReturn(guardado);

        // Act
        MoodEntry resultado = service.registrar(entry);

        // Assert
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(moodRepository).save(entry);
    }

    @Test
    void registrar_conDiferentesEstados_guardaElEstadoRecibido() {
        // Arrange
        MoodEntry entry = new MoodEntry();
        entry.setEstadoAnimo(EstadoAnimo.MUY_MAL);
        MoodEntry guardado = new MoodEntry();
        guardado.setEstadoAnimo(EstadoAnimo.MUY_MAL);
        guardado.setId(2L);
        when(moodRepository.save(entry)).thenReturn(guardado);

        // Act
        MoodEntry resultado = service.registrar(entry);

        // Assert
        assertThat(resultado.getEstadoAnimo()).isEqualTo(EstadoAnimo.MUY_MAL);
    }

    // --- obtenerEstadisticas ---

    @Test
    void obtenerEstadisticas_retornaMapaDelRepositorio() {
        // Arrange
        Map<EstadoAnimo, Long> stats = Map.of(
                EstadoAnimo.MUY_BIEN, 5L,
                EstadoAnimo.BIEN,     3L,
                EstadoAnimo.MAL,      2L
        );
        when(moodRepository.countByEstadoAnimo()).thenReturn(stats);

        // Act
        Map<EstadoAnimo, Long> resultado = service.obtenerEstadisticas();

        // Assert
        assertThat(resultado).containsEntry(EstadoAnimo.MUY_BIEN, 5L);
        assertThat(resultado).containsEntry(EstadoAnimo.BIEN, 3L);
        assertThat(resultado).containsEntry(EstadoAnimo.MAL, 2L);
        verify(moodRepository).countByEstadoAnimo();
    }

    @Test
    void obtenerEstadisticas_conMapaVacio_retornaMapaVacio() {
        // Arrange
        when(moodRepository.countByEstadoAnimo()).thenReturn(Map.of());

        // Act
        Map<EstadoAnimo, Long> resultado = service.obtenerEstadisticas();

        // Assert
        assertThat(resultado).isEmpty();
    }
}
