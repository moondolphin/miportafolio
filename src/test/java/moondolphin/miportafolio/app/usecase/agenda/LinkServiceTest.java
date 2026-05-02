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

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new LinkService(linkRepository);
    }

    // --- obtenerLinksDelUsuario ---

    @Test
    void obtenerLinksDelUsuario_soloRetornaLinksPropios() {
        List<LinkItem> links = List.of(new LinkItem(), new LinkItem());
        when(linkRepository.findAllByCreatedBy(USER_ID)).thenReturn(links);

        List<LinkItem> resultado = service.obtenerLinksDelUsuario(USER_ID);

        assertThat(resultado).hasSize(2);
        verify(linkRepository).findAllByCreatedBy(USER_ID);
        verify(linkRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerLinksDelUsuario_conListaVacia_retornaListaVacia() {
        when(linkRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.obtenerLinksDelUsuario(USER_ID)).isEmpty();
    }

    // --- obtenerLinkPropioPorId ---

    @Test
    void obtenerLinkPropioPorId_conIdExistente_retornaOptionalConLink() {
        LinkItem link = new LinkItem();
        link.setId(1L);
        when(linkRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(link));

        Optional<LinkItem> resultado = service.obtenerLinkPropioPorId(1L, USER_ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerLinkPropioPorId_conIdInexistente_retornaOptionalVacio() {
        when(linkRepository.findByIdAndCreatedBy(99L, USER_ID)).thenReturn(Optional.empty());

        Optional<LinkItem> resultado = service.obtenerLinkPropioPorId(99L, USER_ID);

        assertThat(resultado).isEmpty();
    }

    // --- crearLinkParaUsuario ---

    @Test
    void crearLinkParaUsuario_asignaCreatedByTimestampsYGuarda() {
        LinkItem link = new LinkItem();
        link.setTitulo("GitHub");
        LinkItem guardado = new LinkItem();
        guardado.setId(1L);
        when(linkRepository.save(eq(link), anyList())).thenReturn(guardado);

        LinkItem resultado = service.crearLinkParaUsuario(link, List.of(), USER_ID);

        assertThat(link.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(link.getCreatedAt()).isNotNull();
        assertThat(link.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    // --- actualizarLinkPropio ---

    @Test
    void actualizarLinkPropio_conIdExistente_asignaIdYUpdatedAtYGuarda() {
        LinkItem existente = new LinkItem();
        existente.setId(1L);
        LinkItem nuevo = new LinkItem();
        nuevo.setTitulo("Actualizado");
        LinkItem guardado = new LinkItem();
        guardado.setId(1L);
        when(linkRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));
        when(linkRepository.save(eq(nuevo), anyList())).thenReturn(guardado);

        LinkItem resultado = service.actualizarLinkPropio(1L, nuevo, List.of(), USER_ID);

        assertThat(nuevo.getId()).isEqualTo(1L);
        assertThat(nuevo.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(nuevo.getUpdatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void actualizarLinkPropio_conRecursoAjeno_lanzaNotFoundExceptionSinGuardar() {
        when(linkRepository.findByIdAndCreatedBy(99L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarLinkPropio(99L, new LinkItem(), List.of(), OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Link no encontrado");
        verify(linkRepository, never()).save(any(), any());
    }

    // --- eliminarLinkPropio ---

    @Test
    void eliminarLinkPropio_conIdPropio_delegaAlRepositorio() {
        LinkItem existente = new LinkItem();
        existente.setId(1L);
        when(linkRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));

        service.eliminarLinkPropio(1L, USER_ID);

        verify(linkRepository).deleteById(1L);
    }

    @Test
    void eliminarLinkPropio_conRecursoAjeno_lanzaNotFoundExceptionSinEliminar() {
        when(linkRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarLinkPropio(1L, OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Link no encontrado");
        verify(linkRepository, never()).deleteById(any());
    }
}
