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

    @BeforeEach
    void setUp() {
        service = new CollageService(collageRepository);
    }

    // --- obtenerTodos ---

    @Test
    void obtenerTodos_retornaTodosLosCollages() {
        // Arrange
        List<CollageEntry> entries = List.of(new CollageEntry(), new CollageEntry());
        when(collageRepository.findAll()).thenReturn(entries);

        // Act
        List<CollageEntry> resultado = service.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(collageRepository).findAll();
    }

    @Test
    void obtenerTodos_conListaVacia_retornaListaVacia() {
        // Arrange
        when(collageRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodos()).isEmpty();
    }

    // --- obtenerPorId ---

    @Test
    void obtenerPorId_conIdExistente_retornaOptionalConCollage() {
        // Arrange
        CollageEntry entry = new CollageEntry();
        entry.setId(1L);
        when(collageRepository.findById(1L)).thenReturn(Optional.of(entry));

        // Act
        Optional<CollageEntry> resultado = service.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_conIdInexistente_retornaOptionalVacio() {
        // Arrange
        when(collageRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<CollageEntry> resultado = service.obtenerPorId(99L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- crear ---

    @Test
    void crear_asignaTimestampsYGuarda() {
        // Arrange
        CollageEntry entry = new CollageEntry();
        entry.setTitulo("Collage enero");
        CollageEntry guardado = new CollageEntry();
        guardado.setId(1L);
        when(collageRepository.save(entry)).thenReturn(guardado);

        // Act
        CollageEntry resultado = service.crear(entry);

        // Assert
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(collageRepository).save(entry);
    }

    // --- actualizar ---

    @Test
    void actualizar_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        // Arrange
        CollageEntry existente = new CollageEntry();
        existente.setId(1L);
        CollageEntry nuevo = new CollageEntry();
        nuevo.setTitulo("Actualizado");
        CollageEntry guardado = new CollageEntry();
        guardado.setId(1L);
        when(collageRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(collageRepository.save(nuevo)).thenReturn(guardado);

        // Act
        CollageEntry resultado = service.actualizar(1L, nuevo);

        // Assert
        assertThat(nuevo.getId()).isEqualTo(1L);
        assertThat(nuevo.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizar_conIdInexistente_lanzaExcepcionSinGuardar() {
        // Arrange
        when(collageRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.actualizar(99L, new CollageEntry()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Collage no encontrado");
        verify(collageRepository, never()).save(any());
    }

    // --- eliminar ---

    @Test
    void eliminar_delegaAlRepositorio() {
        // Act
        service.eliminar(1L);

        // Assert
        verify(collageRepository).deleteById(1L);
    }
}
