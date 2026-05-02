package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.CollageEntry;
import moondolphin.miportafolio.domain.port.out.agenda.CollageRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollageServiceTest {

    @Mock
    private CollageRepositoryPort collageRepository;

    private CollageService service;

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new CollageService(collageRepository);
    }

    // --- obtenerCollageDelUsuario ---

    @Test
    void obtenerCollageDelUsuario_soloRetornaEntradasPropias() {
        List<CollageEntry> entries = List.of(new CollageEntry(), new CollageEntry());
        when(collageRepository.findAllByCreatedBy(USER_ID)).thenReturn(entries);

        List<CollageEntry> resultado = service.obtenerCollageDelUsuario(USER_ID);

        assertThat(resultado).hasSize(2);
        verify(collageRepository).findAllByCreatedBy(USER_ID);
        verify(collageRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerCollageDelUsuario_conListaVacia_retornaListaVacia() {
        when(collageRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.obtenerCollageDelUsuario(USER_ID)).isEmpty();
    }

    // --- obtenerCollagePropioPorId ---

    @Test
    void obtenerCollagePropioPorId_conIdExistente_retornaOptionalConCollage() {
        CollageEntry entry = new CollageEntry();
        entry.setId(1L);
        when(collageRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(entry));

        Optional<CollageEntry> resultado = service.obtenerCollagePropioPorId(1L, USER_ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerCollagePropioPorId_conIdInexistente_retornaOptionalVacio() {
        when(collageRepository.findByIdAndCreatedBy(99L, USER_ID)).thenReturn(Optional.empty());

        Optional<CollageEntry> resultado = service.obtenerCollagePropioPorId(99L, USER_ID);

        assertThat(resultado).isEmpty();
    }

    // --- crearCollageParaUsuario ---

    @Test
    void crearCollageParaUsuario_asignaCreatedByTimestampsYGuarda() {
        CollageEntry entry = new CollageEntry();
        entry.setTitulo("Collage enero");
        CollageEntry guardado = new CollageEntry();
        guardado.setId(1L);
        when(collageRepository.save(entry)).thenReturn(guardado);

        CollageEntry resultado = service.crearCollageParaUsuario(entry, USER_ID);

        assertThat(entry.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(collageRepository).save(entry);
    }

    // --- actualizarCollagePropio ---

    @Test
    void actualizarCollagePropio_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        CollageEntry existente = new CollageEntry();
        existente.setId(1L);
        CollageEntry nuevo = new CollageEntry();
        nuevo.setTitulo("Actualizado");
        CollageEntry guardado = new CollageEntry();
        guardado.setId(1L);
        when(collageRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));
        when(collageRepository.save(nuevo)).thenReturn(guardado);

        CollageEntry resultado = service.actualizarCollagePropio(1L, nuevo, USER_ID);

        assertThat(nuevo.getId()).isEqualTo(1L);
        assertThat(nuevo.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(nuevo.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizarCollagePropio_conRecursoAjeno_lanzaNotFoundExceptionSinGuardar() {
        when(collageRepository.findByIdAndCreatedBy(99L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarCollagePropio(99L, new CollageEntry(), OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Collage no encontrado");
        verify(collageRepository, never()).save(any());
    }

    // --- eliminarCollagePropio ---

    @Test
    void eliminarCollagePropio_conIdPropio_delegaAlRepositorio() {
        CollageEntry existente = new CollageEntry();
        existente.setId(1L);
        when(collageRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));

        service.eliminarCollagePropio(1L, USER_ID);

        verify(collageRepository).deleteById(1L);
    }

    @Test
    void eliminarCollagePropio_conRecursoAjeno_lanzaNotFoundExceptionSinEliminar() {
        when(collageRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarCollagePropio(1L, OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Collage no encontrado");
        verify(collageRepository, never()).deleteById(any());
    }
}
