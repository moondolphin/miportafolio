package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.exception.NotFoundException;
import moondolphin.miportafolio.domain.model.agenda.Etiqueta;
import moondolphin.miportafolio.domain.port.out.agenda.EtiquetaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtiquetaServiceTest {

    @Mock
    private EtiquetaRepositoryPort etiquetaRepository;

    private EtiquetaService service;

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new EtiquetaService(etiquetaRepository);
    }

    // --- obtenerEtiquetasDelUsuario ---

    @Test
    void obtenerEtiquetasDelUsuario_soloRetornaEtiquetasPropias() {
        List<Etiqueta> etiquetas = List.of(
                new Etiqueta(1L, "trabajo", "#ff0000", USER_ID, LocalDateTime.now()),
                new Etiqueta(2L, "personal", "#00ff00", USER_ID, LocalDateTime.now())
        );
        when(etiquetaRepository.findAllByCreatedBy(USER_ID)).thenReturn(etiquetas);

        List<Etiqueta> resultado = service.obtenerEtiquetasDelUsuario(USER_ID);

        assertThat(resultado).hasSize(2);
        verify(etiquetaRepository).findAllByCreatedBy(USER_ID);
        verify(etiquetaRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerEtiquetasDelUsuario_conListaVacia_retornaListaVacia() {
        when(etiquetaRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.obtenerEtiquetasDelUsuario(USER_ID)).isEmpty();
    }

    // --- crearEtiquetaParaUsuario ---

    @Test
    void crearEtiquetaParaUsuario_asignaCreatedByTimestampYGuarda() {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("urgente");
        etiqueta.setColor("#ff0000");
        Etiqueta guardada = new Etiqueta(1L, "urgente", "#ff0000", USER_ID, LocalDateTime.now());
        when(etiquetaRepository.save(etiqueta)).thenReturn(guardada);

        Etiqueta resultado = service.crearEtiquetaParaUsuario(etiqueta, USER_ID);

        assertThat(etiqueta.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(etiqueta.getCreatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(etiquetaRepository).save(etiqueta);
    }

    // --- actualizarEtiquetaPropia ---

    @Test
    void actualizarEtiquetaPropia_conIdExistente_asignaIdYGuarda() {
        Etiqueta existente = new Etiqueta(1L, "trabajo", "#ff0000", USER_ID, LocalDateTime.now());
        Etiqueta nueva = new Etiqueta();
        nueva.setNombre("trabajo-actualizado");
        Etiqueta guardada = new Etiqueta(1L, "trabajo-actualizado", "#ff0000", USER_ID, LocalDateTime.now());
        when(etiquetaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));
        when(etiquetaRepository.save(nueva)).thenReturn(guardada);

        Etiqueta resultado = service.actualizarEtiquetaPropia(1L, nueva, USER_ID);

        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(nueva.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(resultado.getNombre()).isEqualTo("trabajo-actualizado");
    }

    @Test
    void actualizarEtiquetaPropia_conRecursoAjeno_lanzaNotFoundExceptionSinGuardar() {
        when(etiquetaRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarEtiquetaPropia(1L, new Etiqueta(), OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Etiqueta no encontrada");
        verify(etiquetaRepository, never()).save(any());
    }

    // --- eliminarEtiquetaPropia ---

    @Test
    void eliminarEtiquetaPropia_conIdPropio_delegaAlRepositorio() {
        Etiqueta existente = new Etiqueta(1L, "trabajo", "#ff0000", USER_ID, LocalDateTime.now());
        when(etiquetaRepository.findByIdAndCreatedBy(1L, USER_ID)).thenReturn(Optional.of(existente));

        service.eliminarEtiquetaPropia(1L, USER_ID);

        verify(etiquetaRepository).deleteById(1L);
    }

    @Test
    void eliminarEtiquetaPropia_conRecursoAjeno_lanzaNotFoundExceptionSinEliminar() {
        when(etiquetaRepository.findByIdAndCreatedBy(1L, OTRO_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarEtiquetaPropia(1L, OTRO_USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Etiqueta no encontrada");
        verify(etiquetaRepository, never()).deleteById(any());
    }
}
