package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.EstadoTarea;
import moondolphin.miportafolio.domain.model.agenda.Prioridad;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import moondolphin.miportafolio.domain.port.out.agenda.TareaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepositoryPort tareaRepository;

    private TareaService service;

    @BeforeEach
    void setUp() {
        service = new TareaService(tareaRepository);
    }

    private Tarea tareaBase() {
        Tarea t = new Tarea();
        t.setTitulo("Estudiar");
        t.setEstado(EstadoTarea.PENDIENTE);
        t.setPrioridad(Prioridad.ALTA);
        return t;
    }

    // --- obtenerTodas ---

    @Test
    void obtenerTodas_retornaTodasLasTareas() {
        // Arrange
        List<Tarea> tareas = List.of(tareaBase(), tareaBase());
        when(tareaRepository.findAll()).thenReturn(tareas);

        // Act
        List<Tarea> resultado = service.obtenerTodas();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(tareaRepository).findAll();
    }

    @Test
    void obtenerTodas_conListaVacia_retornaListaVacia() {
        // Arrange
        when(tareaRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodas()).isEmpty();
    }

    // --- buscar ---

    @Test
    void buscar_conFecha_usaFindByFecha() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByFecha(hoy)).thenReturn(tareas);

        // Act
        List<Tarea> resultado = service.buscar(hoy, null, null, null);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByFecha(hoy);
        verify(tareaRepository, never()).findByPrioridad(any());
        verify(tareaRepository, never()).findByEstado(any());
        verify(tareaRepository, never()).findByEtiquetaNombre(any());
    }

    @Test
    void buscar_conPrioridad_usaFindByPrioridad() {
        // Arrange
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByPrioridad(Prioridad.ALTA)).thenReturn(tareas);

        // Act
        List<Tarea> resultado = service.buscar(null, Prioridad.ALTA, null, null);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByPrioridad(Prioridad.ALTA);
    }

    @Test
    void buscar_conEstado_usaFindByEstado() {
        // Arrange
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByEstado(EstadoTarea.PENDIENTE)).thenReturn(tareas);

        // Act
        List<Tarea> resultado = service.buscar(null, null, EstadoTarea.PENDIENTE, null);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByEstado(EstadoTarea.PENDIENTE);
    }

    @Test
    void buscar_conTag_usaFindByEtiquetaNombre() {
        // Arrange
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByEtiquetaNombre("trabajo")).thenReturn(tareas);

        // Act
        List<Tarea> resultado = service.buscar(null, null, null, "trabajo");

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByEtiquetaNombre("trabajo");
    }

    @Test
    void buscar_sinFiltros_retornaTodas() {
        // Arrange
        List<Tarea> tareas = List.of(tareaBase(), tareaBase());
        when(tareaRepository.findAll()).thenReturn(tareas);

        // Act
        List<Tarea> resultado = service.buscar(null, null, null, null);

        // Assert
        assertThat(resultado).hasSize(2);
        verify(tareaRepository).findAll();
    }

    @Test
    void buscar_conFechaYPrioridad_laFechaTienePrioridad() {
        // Arrange — cuando se pasan ambos, el if de fecha gana por orden de evaluación
        LocalDate hoy = LocalDate.now();
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByFecha(hoy)).thenReturn(tareas);

        // Act
        List<Tarea> resultado = service.buscar(hoy, Prioridad.ALTA, null, null);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByFecha(hoy);
        verify(tareaRepository, never()).findByPrioridad(any());
    }

    // --- obtenerPorId ---

    @Test
    void obtenerPorId_conIdExistente_retornaOptionalConTarea() {
        // Arrange
        Tarea tarea = tareaBase();
        tarea.setId(1L);
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        // Act
        Optional<Tarea> resultado = service.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_conIdInexistente_retornaOptionalVacio() {
        // Arrange
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Tarea> resultado = service.obtenerPorId(99L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- crear ---

    @Test
    void crear_asignaTimestampsYGuarda() {
        // Arrange
        Tarea tarea = tareaBase();
        Tarea guardada = tareaBase();
        guardada.setId(1L);
        when(tareaRepository.save(eq(tarea), anyList())).thenReturn(guardada);

        // Act
        Tarea resultado = service.crear(tarea, List.of());

        // Assert
        assertThat(tarea.getCreatedAt()).isNotNull();
        assertThat(tarea.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(tareaRepository).save(tarea, List.of());
    }

    // --- actualizar ---

    @Test
    void actualizar_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        // Arrange
        Tarea existente = tareaBase();
        existente.setId(1L);
        Tarea nueva = tareaBase();
        nueva.setTitulo("Actualizado");
        Tarea guardada = tareaBase();
        guardada.setId(1L);
        guardada.setTitulo("Actualizado");
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tareaRepository.save(eq(nueva), anyList())).thenReturn(guardada);

        // Act
        Tarea resultado = service.actualizar(1L, nueva, List.of());

        // Assert
        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(nueva.getUpdatedAt()).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Actualizado");
    }

    @Test
    void actualizar_conIdInexistente_lanzaExcepcionSinGuardar() {
        // Arrange
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.actualizar(99L, tareaBase(), List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Tarea no encontrada");
        verify(tareaRepository, never()).save(any(), any());
    }

    // --- eliminar ---

    @Test
    void eliminar_delegaAlRepositorio() {
        // Act
        service.eliminar(1L);

        // Assert
        verify(tareaRepository).deleteById(1L);
    }
}
