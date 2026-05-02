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

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

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

    // --- buscarTareasDelUsuario ---

    @Test
    void buscarTareasDelUsuario_soloRetornaTareasDelUsuario() {
        List<Tarea> tareas = List.of(tareaBase(), tareaBase());
        when(tareaRepository.findAllByCreatedBy(USER_ID)).thenReturn(tareas);

        List<Tarea> resultado = service.buscarTareasDelUsuario(USER_ID, null, null, null, null);

        assertThat(resultado).hasSize(2);
        verify(tareaRepository).findAllByCreatedBy(USER_ID);
        verify(tareaRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void buscarTareasDelUsuario_conListaVacia_retornaListaVacia() {
        when(tareaRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.buscarTareasDelUsuario(USER_ID, null, null, null, null)).isEmpty();
    }

    @Test
    void buscarTareasDelUsuario_conFecha_usaFindByFechaAndCreatedBy() {
        LocalDate hoy = LocalDate.now();
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByFechaAndCreatedBy(hoy, USER_ID)).thenReturn(tareas);

        List<Tarea> resultado = service.buscarTareasDelUsuario(USER_ID, hoy, null, null, null);

        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByFechaAndCreatedBy(hoy, USER_ID);
        verify(tareaRepository, never()).findByPrioridadAndCreatedBy(any(), any());
    }

    @Test
    void buscarTareasDelUsuario_conPrioridad_usaFindByPrioridadAndCreatedBy() {
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByPrioridadAndCreatedBy(Prioridad.ALTA, USER_ID)).thenReturn(tareas);

        List<Tarea> resultado = service.buscarTareasDelUsuario(USER_ID, null, Prioridad.ALTA, null, null);

        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByPrioridadAndCreatedBy(Prioridad.ALTA, USER_ID);
    }

    @Test
    void buscarTareasDelUsuario_conEstado_usaFindByEstadoAndCreatedBy() {
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByEstadoAndCreatedBy(EstadoTarea.PENDIENTE, USER_ID)).thenReturn(tareas);

        List<Tarea> resultado = service.buscarTareasDelUsuario(USER_ID, null, null, EstadoTarea.PENDIENTE, null);

        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByEstadoAndCreatedBy(EstadoTarea.PENDIENTE, USER_ID);
    }

    @Test
    void buscarTareasDelUsuario_conTag_usaFindByEtiquetaNombreAndCreatedBy() {
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByEtiquetaNombreAndCreatedBy("trabajo", USER_ID)).thenReturn(tareas);

        List<Tarea> resultado = service.buscarTareasDelUsuario(USER_ID, null, null, null, "trabajo");

        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByEtiquetaNombreAndCreatedBy("trabajo", USER_ID);
    }

    @Test
    void buscarTareasDelUsuario_conFechaYPrioridad_laFechaTienePrioridad() {
        LocalDate hoy = LocalDate.now();
        List<Tarea> tareas = List.of(tareaBase());
        when(tareaRepository.findByFechaAndCreatedBy(hoy, USER_ID)).thenReturn(tareas);

        List<Tarea> resultado = service.buscarTareasDelUsuario(USER_ID, hoy, Prioridad.ALTA, null, null);

        assertThat(resultado).hasSize(1);
        verify(tareaRepository).findByFechaAndCreatedBy(hoy, USER_ID);
        verify(tareaRepository, never()).findByPrioridadAndCreatedBy(any(), any());
    }

    // --- obtenerTareaPropiaPorId ---

    @Test
    void obtenerTareaPropiaPorId_conIdExistente_retornaOptionalConTarea() {
        Tarea tarea = tareaBase();
        tarea.setId(1L);
        when(tareaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(tarea));

        Optional<Tarea> resultado = service.obtenerTareaPropiaPorId(1L, USER_ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerTareaPropiaPorId_conIdInexistente_retornaOptionalVacio() {
        when(tareaRepository.findByIdAndCreatedBy(99L, USER_ID)).thenReturn(Optional.empty());

        Optional<Tarea> resultado = service.obtenerTareaPropiaPorId(99L, USER_ID);

        assertThat(resultado).isEmpty();
    }

    // --- crearTareaParaUsuario ---

    @Test
    void crearTareaParaUsuario_asignaCreatedByTimestampsYGuarda() {
        Tarea tarea = tareaBase();
        Tarea guardada = tareaBase();
        guardada.setId(1L);
        when(tareaRepository.save(eq(tarea), anyList())).thenReturn(guardada);

        Tarea resultado = service.crearTareaParaUsuario(tarea, List.of(), USER_ID);

        assertThat(tarea.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(tarea.getCreatedAt()).isNotNull();
        assertThat(tarea.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(tareaRepository).save(tarea, List.of());
    }

    // --- actualizarTareaPropia ---

    @Test
    void actualizarTareaPropia_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        Tarea existente = tareaBase();
        existente.setId(1L);
        Tarea nueva = tareaBase();
        nueva.setTitulo("Actualizado");
        Tarea guardada = tareaBase();
        guardada.setId(1L);
        guardada.setTitulo("Actualizado");
        when(tareaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));
        when(tareaRepository.save(eq(nueva), anyList())).thenReturn(guardada);

        Tarea resultado = service.actualizarTareaPropia(1L, nueva, List.of(), USER_ID);

        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(nueva.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(nueva.getUpdatedAt()).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Actualizado");
    }

    @Test
    void actualizarTareaPropia_conRecursoAjeno_lanzaNotFoundExceptionSinGuardar() {
        when(tareaRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarTareaPropia(1L, tareaBase(), List.of(), OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Tarea no encontrada");
        verify(tareaRepository, never()).save(any(), any());
    }

    // --- eliminarTareaPropia ---

    @Test
    void eliminarTareaPropia_conIdPropio_delegaAlRepositorio() {
        Tarea existente = tareaBase();
        existente.setId(1L);
        when(tareaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));

        service.eliminarTareaPropia(1L, USER_ID);

        verify(tareaRepository).deleteById(1L);
    }

    @Test
    void eliminarTareaPropia_conRecursoAjeno_lanzaNotFoundExceptionSinEliminar() {
        when(tareaRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarTareaPropia(1L, OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Tarea no encontrada");
        verify(tareaRepository, never()).deleteById(any());
    }
}
