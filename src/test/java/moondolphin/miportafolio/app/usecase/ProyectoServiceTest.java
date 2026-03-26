package moondolphin.miportafolio.app.usecase;

import moondolphin.miportafolio.domain.model.Proyecto;
import moondolphin.miportafolio.domain.port.out.ProyectoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepositoryPort repositoryPort;

    private ProyectoService service;

    @BeforeEach
    void setUp() {
        service = new ProyectoService(repositoryPort);
    }

    // --- obtenerTodos ---

    @Test
    void obtenerTodos_retornaListaDelRepositorio() {
        // Arrange
        List<Proyecto> proyectos = List.of(
                new Proyecto(1L, "Portafolio", "Mi portafolio", "img.png"),
                new Proyecto(2L, "Agenda", "Agenda personal", "agenda.png")
        );
        when(repositoryPort.findAll()).thenReturn(proyectos);

        // Act
        List<Proyecto> resultado = service.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Portafolio");
        verify(repositoryPort).findAll();
    }

    @Test
    void obtenerTodos_conListaVacia_retornaListaVacia() {
        // Arrange
        when(repositoryPort.findAll()).thenReturn(List.of());

        // Act
        List<Proyecto> resultado = service.obtenerTodos();

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- buscarPorNombre ---

    @Test
    void buscarPorNombre_conCoincidencia_retornaProyectos() {
        // Arrange
        List<Proyecto> proyectos = List.of(new Proyecto(1L, "Portafolio", "desc", "img.png"));
        when(repositoryPort.findByNombre("Portafolio")).thenReturn(proyectos);

        // Act
        List<Proyecto> resultado = service.buscarPorNombre("Portafolio");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Portafolio");
        verify(repositoryPort).findByNombre("Portafolio");
    }

    @Test
    void buscarPorNombre_sinCoincidencia_retornaListaVacia() {
        // Arrange
        when(repositoryPort.findByNombre("Inexistente")).thenReturn(List.of());

        // Act
        List<Proyecto> resultado = service.buscarPorNombre("Inexistente");

        // Assert
        assertThat(resultado).isEmpty();
    }

    // --- buscarPorTerminoEnDescripcion ---

    @Test
    void buscarPorTerminoEnDescripcion_conCoincidencia_retornaProyectos() {
        // Arrange
        List<Proyecto> proyectos = List.of(new Proyecto(1L, "Agenda", "Java Spring", "img.png"));
        when(repositoryPort.buscarPorTerminoEnDescripcion("Java")).thenReturn(proyectos);

        // Act
        List<Proyecto> resultado = service.buscarPorTerminoEnDescripcion("Java");

        // Assert
        assertThat(resultado).hasSize(1);
        verify(repositoryPort).buscarPorTerminoEnDescripcion("Java");
    }

    @Test
    void buscarPorTerminoEnDescripcion_sinCoincidencia_retornaListaVacia() {
        // Arrange
        when(repositoryPort.buscarPorTerminoEnDescripcion("xyz")).thenReturn(List.of());

        // Act
        List<Proyecto> resultado = service.buscarPorTerminoEnDescripcion("xyz");

        // Assert
        assertThat(resultado).isEmpty();
    }
}
