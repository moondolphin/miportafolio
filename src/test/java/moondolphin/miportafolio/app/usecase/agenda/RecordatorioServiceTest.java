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

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new RecordatorioService(recordatorioRepository);
    }

    // --- obtenerRecordatoriosDelUsuario ---

    @Test
    void obtenerRecordatoriosDelUsuario_soloRetornaRecordatoriosPropios() {
        List<Recordatorio> recordatorios = List.of(new Recordatorio(), new Recordatorio());
        when(recordatorioRepository.findAllByCreatedBy(USER_ID)).thenReturn(recordatorios);

        List<Recordatorio> resultado = service.obtenerRecordatoriosDelUsuario(USER_ID);

        assertThat(resultado).hasSize(2);
        verify(recordatorioRepository).findAllByCreatedBy(USER_ID);
        verify(recordatorioRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerRecordatoriosDelUsuario_conListaVacia_retornaListaVacia() {
        when(recordatorioRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.obtenerRecordatoriosDelUsuario(USER_ID)).isEmpty();
    }

    // --- obtenerRecordatorioPropioPorId ---

    @Test
    void obtenerRecordatorioPropioPorId_conIdExistente_retornaOptionalConRecordatorio() {
        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setId(1L);
        when(recordatorioRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(recordatorio));

        Optional<Recordatorio> resultado = service.obtenerRecordatorioPropioPorId(1L, USER_ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerRecordatorioPropioPorId_conIdInexistente_retornaOptionalVacio() {
        when(recordatorioRepository.findByIdAndCreatedBy(99L, USER_ID)).thenReturn(Optional.empty());

        Optional<Recordatorio> resultado = service.obtenerRecordatorioPropioPorId(99L, USER_ID);

        assertThat(resultado).isEmpty();
    }

    // --- crearRecordatorioParaUsuario ---

    @Test
    void crearRecordatorioParaUsuario_asignaCreatedByTimestampsYGuarda() {
        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setTitulo("Tomar medicamento");
        Recordatorio guardado = new Recordatorio();
        guardado.setId(1L);
        when(recordatorioRepository.save(eq(recordatorio), anyList())).thenReturn(guardado);

        Recordatorio resultado = service.crearRecordatorioParaUsuario(recordatorio, List.of(), USER_ID);

        assertThat(recordatorio.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(recordatorio.getCreatedAt()).isNotNull();
        assertThat(recordatorio.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizarRecordatorioPropio ---

    @Test
    void actualizarRecordatorioPropio_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        Recordatorio existente = new Recordatorio();
        existente.setId(1L);
        Recordatorio nuevo = new Recordatorio();
        nuevo.setTitulo("Actualizado");
        Recordatorio guardado = new Recordatorio();
        guardado.setId(1L);
        when(recordatorioRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));
        when(recordatorioRepository.save(eq(nuevo), anyList())).thenReturn(guardado);

        Recordatorio resultado = service.actualizarRecordatorioPropio(1L, nuevo, List.of(), USER_ID);

        assertThat(nuevo.getId()).isEqualTo(1L);
        assertThat(nuevo.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(nuevo.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizarRecordatorioPropio_conRecursoAjeno_lanzaNotFoundExceptionSinGuardar() {
        when(recordatorioRepository.findByIdAndCreatedBy(99L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarRecordatorioPropio(99L, new Recordatorio(), List.of(), OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recordatorio no encontrado");
        verify(recordatorioRepository, never()).save(any(), any());
    }

    // --- eliminarRecordatorioPropio ---

    @Test
    void eliminarRecordatorioPropio_conIdPropio_delegaAlRepositorio() {
        Recordatorio existente = new Recordatorio();
        existente.setId(1L);
        when(recordatorioRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));

        service.eliminarRecordatorioPropio(1L, USER_ID);

        verify(recordatorioRepository).deleteById(1L);
    }

    @Test
    void eliminarRecordatorioPropio_conRecursoAjeno_lanzaNotFoundExceptionSinEliminar() {
        when(recordatorioRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarRecordatorioPropio(1L, OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recordatorio no encontrado");
        verify(recordatorioRepository, never()).deleteById(any());
    }
}
