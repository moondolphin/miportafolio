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

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new NotaService(notaRepository);
    }

    // --- obtenerNotasDelUsuario ---

    @Test
    void obtenerNotasDelUsuario_soloRetornaNotasPropias() {
        List<NotaLibre> notas = List.of(new NotaLibre(), new NotaLibre());
        when(notaRepository.findAllByCreatedBy(USER_ID)).thenReturn(notas);

        List<NotaLibre> resultado = service.obtenerNotasDelUsuario(USER_ID);

        assertThat(resultado).hasSize(2);
        verify(notaRepository).findAllByCreatedBy(USER_ID);
        verify(notaRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerNotasDelUsuario_conListaVacia_retornaListaVacia() {
        when(notaRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.obtenerNotasDelUsuario(USER_ID)).isEmpty();
    }

    // --- obtenerNotaPropiaPorId ---

    @Test
    void obtenerNotaPropiaPorId_conIdExistente_retornaOptionalConNota() {
        NotaLibre nota = new NotaLibre();
        nota.setId(1L);
        when(notaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(nota));

        Optional<NotaLibre> resultado = service.obtenerNotaPropiaPorId(1L, USER_ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerNotaPropiaPorId_conIdInexistente_retornaOptionalVacio() {
        when(notaRepository.findByIdAndCreatedBy(99L, USER_ID)).thenReturn(Optional.empty());

        Optional<NotaLibre> resultado = service.obtenerNotaPropiaPorId(99L, USER_ID);

        assertThat(resultado).isEmpty();
    }

    // --- crearNotaParaUsuario ---

    @Test
    void crearNotaParaUsuario_asignaCreatedByTimestampsYGuarda() {
        NotaLibre nota = new NotaLibre();
        nota.setTitulo("Mi nota");
        NotaLibre guardada = new NotaLibre();
        guardada.setId(1L);
        when(notaRepository.save(eq(nota), anyList())).thenReturn(guardada);

        NotaLibre resultado = service.crearNotaParaUsuario(nota, List.of(), USER_ID);

        assertThat(nota.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(nota.getCreatedAt()).isNotNull();
        assertThat(nota.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizarNotaPropia ---

    @Test
    void actualizarNotaPropia_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        NotaLibre existente = new NotaLibre();
        existente.setId(1L);
        NotaLibre nueva = new NotaLibre();
        nueva.setTitulo("Actualizada");
        NotaLibre guardada = new NotaLibre();
        guardada.setId(1L);
        when(notaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));
        when(notaRepository.save(eq(nueva), anyList())).thenReturn(guardada);

        NotaLibre resultado = service.actualizarNotaPropia(1L, nueva, List.of(), USER_ID);

        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(nueva.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(nueva.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizarNotaPropia_conRecursoAjeno_lanzaNotFoundExceptionSinGuardar() {
        when(notaRepository.findByIdAndCreatedBy(99L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarNotaPropia(99L, new NotaLibre(), List.of(), OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Nota no encontrada");
        verify(notaRepository, never()).save(any(), any());
    }

    // --- eliminarNotaPropia ---

    @Test
    void eliminarNotaPropia_conIdPropio_delegaAlRepositorio() {
        NotaLibre existente = new NotaLibre();
        existente.setId(1L);
        when(notaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));

        service.eliminarNotaPropia(1L, USER_ID);

        verify(notaRepository).deleteById(1L);
    }

    @Test
    void eliminarNotaPropia_conRecursoAjeno_lanzaNotFoundExceptionSinEliminar() {
        when(notaRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarNotaPropia(1L, OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Nota no encontrada");
        verify(notaRepository, never()).deleteById(any());
    }
}
