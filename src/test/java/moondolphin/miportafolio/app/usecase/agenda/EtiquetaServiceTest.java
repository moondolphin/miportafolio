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

    @BeforeEach
    void setUp() {
        service = new EtiquetaService(etiquetaRepository);
    }

    // --- obtenerTodas ---

    @Test
    void obtenerTodas_retornaTodasLasEtiquetas() {
        // Arrange
        List<Etiqueta> etiquetas = List.of(
                new Etiqueta(1L, "trabajo", "#ff0000", LocalDateTime.now()),
                new Etiqueta(2L, "personal", "#00ff00", LocalDateTime.now())
        );
        when(etiquetaRepository.findAll()).thenReturn(etiquetas);

        // Act
        List<Etiqueta> resultado = service.obtenerTodas();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(etiquetaRepository).findAll();
    }

    @Test
    void obtenerTodas_conListaVacia_retornaListaVacia() {
        // Arrange
        when(etiquetaRepository.findAll()).thenReturn(List.of());

        // Act + Assert
        assertThat(service.obtenerTodas()).isEmpty();
    }

    // --- crear ---

    @Test
    void crear_asignaCreatedAtYGuarda() {
        // Arrange
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("urgente");
        etiqueta.setColor("#ff0000");
        Etiqueta guardada = new Etiqueta(1L, "urgente", "#ff0000", LocalDateTime.now());
        when(etiquetaRepository.save(etiqueta)).thenReturn(guardada);

        // Act
        Etiqueta resultado = service.crear(etiqueta);

        // Assert
        assertThat(etiqueta.getCreatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(etiquetaRepository).save(etiqueta);
    }

    // --- actualizar ---

    @Test
    void actualizar_conIdExistente_asignaIdYGuarda() {
        // Arrange
        Etiqueta existente = new Etiqueta(1L, "trabajo", "#ff0000", LocalDateTime.now());
        Etiqueta nueva = new Etiqueta();
        nueva.setNombre("trabajo-actualizado");
        Etiqueta guardada = new Etiqueta(1L, "trabajo-actualizado", "#ff0000", LocalDateTime.now());
        when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(etiquetaRepository.save(nueva)).thenReturn(guardada);

        // Act
        Etiqueta resultado = service.actualizar(1L, nueva);

        // Assert
        assertThat(nueva.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("trabajo-actualizado");
    }

    @Test
    void actualizar_conIdInexistente_lanzaExcepcionSinGuardar() {
        // Arrange
        when(etiquetaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.actualizar(99L, new Etiqueta()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Etiqueta no encontrada");
        verify(etiquetaRepository, never()).save(any());
    }

    // --- eliminar ---

    @Test
    void eliminar_delegaAlRepositorio() {
        // Act
        service.eliminar(1L);

        // Assert
        verify(etiquetaRepository).deleteById(1L);
    }
}
