package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Recordatorio;
import moondolphin.miportafolio.domain.port.out.agenda.RecordatorioRepositoryPort;
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
class RecordatorioServiceTest {

    @Mock
    private RecordatorioRepositoryPort recordatorioRepository;

    private RecordatorioService service;

    @BeforeEach
    void setUp() {
        service = new RecordatorioService(recordatorioRepository);
    }

    // --- obtenerTodos ---

    @Test
    void obtenerTodos_retornaTodosLosRecordatorios() {
        // Arrange
        List<Recordatorio> recordatorios = List.of(new Recordatorio(), new Recordatorio());
        when(recordatorioRepository.findAll()).thenReturn(recordatorios);

        // Act
        List<Recordatorio> resultado = service.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(recordatorioRepository).findAll();
    }

    @Test
    void obtenerTodos_conListaVacia_retornaListaVacia() {
        // Arrange
        when(recordatorioRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodos()).isEmpty();
    }

    // --- obtenerPorId ---

    @Test
    void obtenerPorId_conIdExistente_retornaOptionalConRecordatorio() {
        // Arrange
        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setId(1L);
        when(recordatorioRepository.findById(1L)).thenReturn(Optional.of(recordatorio));

        // Act
        Optional<Recordatorio> resultado = service.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_conIdInexistente_retornaOptionalVacio() {
        // Arrange
        when(recordatorioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Recordatorio> resultado = service.obtenerPorId(99L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- crear ---

    @Test
    void crear_asignaTimestampsYGuarda() {
        // Arrange
        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setTitulo("Tomar medicamento");
        Recordatorio guardado = new Recordatorio();
        guardado.setId(1L);
        when(recordatorioRepository.save(eq(recordatorio), anyList())).thenReturn(guardado);

        // Act
        Recordatorio resultado = service.crear(recordatorio, List.of());

        // Assert
        assertThat(recordatorio.getCreatedAt()).isNotNull();
        assertThat(recordatorio.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizar ---

    @Test
    void actualizar_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        // Arrange
        Recordatorio existente = new Recordatorio();
        existente.setId(1L);
        Recordatorio nuevo = new Recordatorio();
        nuevo.setTitulo("Actualizado");
        Recordatorio guardado = new Recordatorio();
        guardado.setId(1L);
        when(recordatorioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(recordatorioRepository.save(eq(nuevo), anyList())).thenReturn(guardado);

        // Act
        Recordatorio resultado = service.actualizar(1L, nuevo, List.of());

        // Assert
        assertThat(nuevo.getId()).isEqualTo(1L);
        assertThat(nuevo.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizar_conIdInexistente_lanzaExcepcionSinGuardar() {
        // Arrange
        when(recordatorioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.actualizar(99L, new Recordatorio(), List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recordatorio no encontrado");
        verify(recordatorioRepository, never()).save(any(), any());
    }

    // --- eliminar ---

    @Test
    void eliminar_delegaAlRepositorio() {
        // Act
        service.eliminar(1L);

        // Assert
        verify(recordatorioRepository).deleteById(1L);
    }
}
