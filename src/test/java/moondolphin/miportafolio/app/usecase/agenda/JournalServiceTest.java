package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import moondolphin.miportafolio.domain.port.out.agenda.JournalRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock
    private JournalRepositoryPort journalRepository;

    private JournalService service;

    @BeforeEach
    void setUp() {
        service = new JournalService(journalRepository);
    }

    // --- obtenerTodas ---

    @Test
    void obtenerTodas_retornaTodasLasEntradas() {
        // Arrange
        List<JournalEntry> entradas = List.of(new JournalEntry(), new JournalEntry());
        when(journalRepository.findAll()).thenReturn(entradas);

        // Act
        List<JournalEntry> resultado = service.obtenerTodas();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(journalRepository).findAll();
    }

    @Test
    void obtenerTodas_conListaVacia_retornaListaVacia() {
        // Arrange
        when(journalRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodas()).isEmpty();
    }

    // --- obtenerPorId ---

    @Test
    void obtenerPorId_conIdExistente_retornaOptionalConEntrada() {
        // Arrange
        JournalEntry entry = new JournalEntry();
        entry.setId(1L);
        when(journalRepository.findById(1L)).thenReturn(Optional.of(entry));

        // Act
        Optional<JournalEntry> resultado = service.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_conIdInexistente_retornaOptionalVacio() {
        // Arrange
        when(journalRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<JournalEntry> resultado = service.obtenerPorId(99L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- crear ---

    @Test
    void crear_asignaTimestampsYGuarda() {
        // Arrange
        JournalEntry entry = new JournalEntry();
        entry.setTitulo("Día feliz");
        JournalEntry guardada = new JournalEntry();
        guardada.setId(1L);
        when(journalRepository.save(eq(entry), anyList())).thenReturn(guardada);

        // Act
        JournalEntry resultado = service.crear(entry, List.of());

        // Assert
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizar ---

    @Test
    void actualizar_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        // Arrange
        JournalEntry existente = new JournalEntry();
        existente.setId(1L);
        JournalEntry nueva = new JournalEntry();
        nueva.setTitulo("Actualizada");
        JournalEntry guardada = new JournalEntry();
        guardada.setId(1L);
        when(journalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(journalRepository.save(eq(nueva), anyList())).thenReturn(guardada);

        // Act
        JournalEntry resultado = service.actualizar(1L, nueva, List.of());

        // Assert
        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(nueva.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizar_conIdInexistente_lanzaExcepcionSinGuardar() {
        // Arrange
        when(journalRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.actualizar(99L, new JournalEntry(), List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Entrada de journal no encontrada");
        verify(journalRepository, never()).save(any(), any());
    }

    // --- eliminar ---

    @Test
    void eliminar_delegaAlRepositorio() {
        // Act
        service.eliminar(1L);

        // Assert
        verify(journalRepository).deleteById(1L);
    }
}
