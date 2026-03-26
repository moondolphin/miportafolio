package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import moondolphin.miportafolio.domain.port.out.agenda.NotaRepositoryPort;
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
class NotaServiceTest {

    @Mock
    private NotaRepositoryPort notaRepository;

    private NotaService service;

    @BeforeEach
    void setUp() {
        service = new NotaService(notaRepository);
    }

    // --- obtenerTodas ---

    @Test
    void obtenerTodas_retornaTodasLasNotas() {
        // Arrange
        List<NotaLibre> notas = List.of(new NotaLibre(), new NotaLibre());
        when(notaRepository.findAll()).thenReturn(notas);

        // Act
        List<NotaLibre> resultado = service.obtenerTodas();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(notaRepository).findAll();
    }

    @Test
    void obtenerTodas_conListaVacia_retornaListaVacia() {
        // Arrange
        when(notaRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodas()).isEmpty();
    }

    // --- obtenerPorId ---

    @Test
    void obtenerPorId_conIdExistente_retornaOptionalConNota() {
        // Arrange
        NotaLibre nota = new NotaLibre();
        nota.setId(1L);
        when(notaRepository.findById(1L)).thenReturn(Optional.of(nota));

        // Act
        Optional<NotaLibre> resultado = service.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_conIdInexistente_retornaOptionalVacio() {
        // Arrange
        when(notaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<NotaLibre> resultado = service.obtenerPorId(99L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- crear ---

    @Test
    void crear_asignaTimestampsYGuarda() {
        // Arrange
        NotaLibre nota = new NotaLibre();
        nota.setTitulo("Mi nota");
        NotaLibre guardada = new NotaLibre();
        guardada.setId(1L);
        when(notaRepository.save(eq(nota), anyList())).thenReturn(guardada);

        // Act
        NotaLibre resultado = service.crear(nota, List.of());

        // Assert
        assertThat(nota.getCreatedAt()).isNotNull();
        assertThat(nota.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizar ---

    @Test
    void actualizar_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        // Arrange
        NotaLibre existente = new NotaLibre();
        existente.setId(1L);
        NotaLibre nueva = new NotaLibre();
        nueva.setTitulo("Actualizada");
        NotaLibre guardada = new NotaLibre();
        guardada.setId(1L);
        when(notaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(notaRepository.save(eq(nueva), anyList())).thenReturn(guardada);

        // Act
        NotaLibre resultado = service.actualizar(1L, nueva, List.of());

        // Assert
        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(nueva.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizar_conIdInexistente_lanzaExcepcionSinGuardar() {
        // Arrange
        when(notaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.actualizar(99L, new NotaLibre(), List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Nota no encontrada");
        verify(notaRepository, never()).save(any(), any());
    }

    // --- eliminar ---

    @Test
    void eliminar_delegaAlRepositorio() {
        // Act
        service.eliminar(1L);

        // Assert
        verify(notaRepository).deleteById(1L);
    }
}
