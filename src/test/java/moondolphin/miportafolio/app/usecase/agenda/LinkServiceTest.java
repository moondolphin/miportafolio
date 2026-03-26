package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import moondolphin.miportafolio.domain.port.out.agenda.LinkRepositoryPort;
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
class LinkServiceTest {

    @Mock
    private LinkRepositoryPort linkRepository;

    private LinkService service;

    @BeforeEach
    void setUp() {
        service = new LinkService(linkRepository);
    }

    // --- obtenerTodos ---

    @Test
    void obtenerTodos_retornaTodosLosLinks() {
        // Arrange
        List<LinkItem> links = List.of(new LinkItem(), new LinkItem());
        when(linkRepository.findAll()).thenReturn(links);

        // Act
        List<LinkItem> resultado = service.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(linkRepository).findAll();
    }

    @Test
    void obtenerTodos_conListaVacia_retornaListaVacia() {
        // Arrange
        when(linkRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodos()).isEmpty();
    }

    // --- obtenerPorId ---

    @Test
    void obtenerPorId_conIdExistente_retornaOptionalConLink() {
        // Arrange
        LinkItem link = new LinkItem();
        link.setId(1L);
        when(linkRepository.findById(1L)).thenReturn(Optional.of(link));

        // Act
        Optional<LinkItem> resultado = service.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerPorId_conIdInexistente_retornaOptionalVacio() {
        // Arrange
        when(linkRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<LinkItem> resultado = service.obtenerPorId(99L);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- crear ---

    @Test
    void crear_asignaTimestampsYGuarda() {
        // Arrange
        LinkItem link = new LinkItem();
        link.setTitulo("GitHub");
        LinkItem guardado = new LinkItem();
        guardado.setId(1L);
        when(linkRepository.save(eq(link), anyList())).thenReturn(guardado);

        // Act
        LinkItem resultado = service.crear(link, List.of());

        // Assert
        assertThat(link.getCreatedAt()).isNotNull();
        assertThat(link.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizar ---

    @Test
    void actualizar_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        // Arrange
        LinkItem existente = new LinkItem();
        existente.setId(1L);
        LinkItem nuevo = new LinkItem();
        nuevo.setTitulo("Actualizado");
        LinkItem guardado = new LinkItem();
        guardado.setId(1L);
        when(linkRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(linkRepository.save(eq(nuevo), anyList())).thenReturn(guardado);

        // Act
        LinkItem resultado = service.actualizar(1L, nuevo, List.of());

        // Assert
        assertThat(nuevo.getId()).isEqualTo(1L);
        assertThat(nuevo.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizar_conIdInexistente_lanzaExcepcionSinGuardar() {
        // Arrange
        when(linkRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.actualizar(99L, new LinkItem(), List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Link no encontrado");
        verify(linkRepository, never()).save(any(), any());
    }

    // --- eliminar ---

    @Test
    void eliminar_delegaAlRepositorio() {
        // Act
        service.eliminar(1L);

        // Assert
        verify(linkRepository).deleteById(1L);
    }
}
