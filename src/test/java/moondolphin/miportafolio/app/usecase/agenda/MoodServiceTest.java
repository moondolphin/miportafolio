package moondolphin.miportafolio.app.usecase.agenda;

import moondolphin.miportafolio.domain.model.agenda.EstadoAnimo;
import moondolphin.miportafolio.domain.model.agenda.MoodEntry;
import moondolphin.miportafolio.domain.port.out.agenda.MoodRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoodServiceTest {

    @Mock
    private MoodRepositoryPort moodRepository;

    private MoodService service;

    private static final Long USER_ID = 1L;
    private static final Long OTRO_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new MoodService(moodRepository);
    }

    // --- obtenerMoodsDelUsuario ---

    @Test
    void obtenerMoodsDelUsuario_soloRetornaMoodsPropios() {
        List<MoodEntry> entries = List.of(new MoodEntry(), new MoodEntry());
        when(moodRepository.findAllByCreatedBy(USER_ID)).thenReturn(entries);

        List<MoodEntry> resultado = service.obtenerMoodsDelUsuario(USER_ID);

        assertThat(resultado).hasSize(2);
        verify(moodRepository).findAllByCreatedBy(USER_ID);
        verify(moodRepository, never()).findAllByCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerMoodsDelUsuario_conListaVacia_retornaListaVacia() {
        when(moodRepository.findAllByCreatedBy(USER_ID)).thenReturn(List.of());

        assertThat(service.obtenerMoodsDelUsuario(USER_ID)).isEmpty();
    }

    // --- registrarMoodParaUsuario ---

    @Test
    void registrarMoodParaUsuario_asignaCreatedByYTimestampYGuarda() {
        MoodEntry entry = new MoodEntry();
        entry.setFecha(LocalDate.now());
        entry.setEstadoAnimo(EstadoAnimo.MUY_BIEN);
        MoodEntry guardado = new MoodEntry();
        guardado.setId(1L);
        when(moodRepository.save(entry)).thenReturn(guardado);

        MoodEntry resultado = service.registrarMoodParaUsuario(entry, USER_ID);

        assertThat(entry.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(moodRepository).save(entry);
    }

    @Test
    void registrarMoodParaUsuario_conDiferentesEstados_guardaElEstadoRecibido() {
        MoodEntry entry = new MoodEntry();
        entry.setEstadoAnimo(EstadoAnimo.MUY_MAL);
        MoodEntry guardado = new MoodEntry();
        guardado.setEstadoAnimo(EstadoAnimo.MUY_MAL);
        guardado.setId(2L);
        when(moodRepository.save(entry)).thenReturn(guardado);

        MoodEntry resultado = service.registrarMoodParaUsuario(entry, USER_ID);

        assertThat(resultado.getEstadoAnimo()).isEqualTo(EstadoAnimo.MUY_MAL);
    }

    // --- obtenerEstadisticasMoodDelUsuario ---

    @Test
    void obtenerEstadisticasMoodDelUsuario_retornaMapaFiltradoPorUsuario() {
        Map<EstadoAnimo, Long> stats = Map.of(
                EstadoAnimo.MUY_BIEN, 5L,
                EstadoAnimo.BIEN,     3L,
                EstadoAnimo.MAL,      2L
        );
        when(moodRepository.countByEstadoAnimoAndCreatedBy(USER_ID)).thenReturn(stats);

        Map<EstadoAnimo, Long> resultado = service.obtenerEstadisticasMoodDelUsuario(USER_ID);

        assertThat(resultado).containsEntry(EstadoAnimo.MUY_BIEN, 5L);
        assertThat(resultado).containsEntry(EstadoAnimo.BIEN, 3L);
        assertThat(resultado).containsEntry(EstadoAnimo.MAL, 2L);
        verify(moodRepository).countByEstadoAnimoAndCreatedBy(USER_ID);
        verify(moodRepository, never()).countByEstadoAnimoAndCreatedBy(OTRO_USER_ID);
    }

    @Test
    void obtenerEstadisticasMoodDelUsuario_conMapaVacio_retornaMapaVacio() {
        when(moodRepository.countByEstadoAnimoAndCreatedBy(USER_ID)).thenReturn(Map.of());

        Map<EstadoAnimo, Long> resultado = service.obtenerEstadisticasMoodDelUsuario(USER_ID);

        assertThat(resultado).isEmpty();
    }
}
