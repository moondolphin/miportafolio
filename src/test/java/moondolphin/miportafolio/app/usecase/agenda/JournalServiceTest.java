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

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new JournalService(journalRepository);
    }

    // --- obtenerJournalDelUsuario ---

    @Test
    void obtenerJournalDelUsuario_soloRetornaEntradasPropias() {
        List<JournalEntry> entradas = List.of(new JournalEntry(), new JournalEntry());
        when(journalRepository.findAllByCreatedBy(USER_ID)).thenReturn(entradas);

        List<JournalEntry> resultado = service.obtenerJournalDelUsuario(USER_ID);

        assertThat(resultado).hasSize(2);
        verify(journalRepository).findAllByCreatedBy(USER_ID);
        verify(journalRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerJournalDelUsuario_conListaVacia_retornaListaVacia() {
        when(journalRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.obtenerJournalDelUsuario(USER_ID)).isEmpty();
    }

    // --- obtenerEntradaJournalPropiaPorId ---

    @Test
    void obtenerEntradaJournalPropiaPorId_conIdExistente_retornaOptionalConEntrada() {
        JournalEntry entry = new JournalEntry();
        entry.setId(1L);
        when(journalRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(entry));

        Optional<JournalEntry> resultado = service.obtenerEntradaJournalPropiaPorId(1L, USER_ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerEntradaJournalPropiaPorId_conIdInexistente_retornaOptionalVacio() {
        when(journalRepository.findByIdAndCreatedBy(99L, USER_ID)).thenReturn(Optional.empty());

        Optional<JournalEntry> resultado = service.obtenerEntradaJournalPropiaPorId(99L, USER_ID);

        assertThat(resultado).isEmpty();
    }

    // --- crearEntradaJournalParaUsuario ---

    @Test
    void crearEntradaJournalParaUsuario_asignaCreatedByTimestampsYGuarda() {
        JournalEntry entry = new JournalEntry();
        entry.setTitulo("Día feliz");
        JournalEntry guardada = new JournalEntry();
        guardada.setId(1L);
        when(journalRepository.save(eq(entry), anyList())).thenReturn(guardada);

        JournalEntry resultado = service.crearEntradaJournalParaUsuario(entry, List.of(), USER_ID);

        assertThat(entry.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizarEntradaJournalPropia ---

    @Test
    void actualizarEntradaJournalPropia_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        JournalEntry existente = new JournalEntry();
        existente.setId(1L);
        JournalEntry nueva = new JournalEntry();
        nueva.setTitulo("Actualizada");
        JournalEntry guardada = new JournalEntry();
        guardada.setId(1L);
        when(journalRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));
        when(journalRepository.save(eq(nueva), anyList())).thenReturn(guardada);

        JournalEntry resultado = service.actualizarEntradaJournalPropia(1L, nueva, List.of(), USER_ID);

        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(nueva.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(nueva.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizarEntradaJournalPropia_conRecursoAjeno_lanzaNotFoundExceptionSinGuardar() {
        when(journalRepository.findByIdAndCreatedBy(99L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarEntradaJournalPropia(99L, new JournalEntry(), List.of(), OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Entrada de journal no encontrada");
        verify(journalRepository, never()).save(any(), any());
    }

    // --- eliminarEntradaJournalPropia ---

    @Test
    void eliminarEntradaJournalPropia_conIdPropio_delegaAlRepositorio() {
        JournalEntry existente = new JournalEntry();
        existente.setId(1L);
        when(journalRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));

        service.eliminarEntradaJournalPropia(1L, USER_ID);

        verify(journalRepository).deleteById(1L);
    }

    @Test
    void eliminarEntradaJournalPropia_conRecursoAjeno_lanzaNotFoundExceptionSinEliminar() {
        when(journalRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarEntradaJournalPropia(1L, OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Entrada de journal no encontrada");
        verify(journalRepository, never()).deleteById(any());
    }
}
