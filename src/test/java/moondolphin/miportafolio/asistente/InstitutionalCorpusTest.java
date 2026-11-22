package moondolphin.miportafolio.asistente;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class InstitutionalCorpusTest {
    @Test
    void acceptsAdditionalSectionsAndStableNonConsecutiveIds() {
        String corpus = """
                Versión 1.2.
                ## INST-01 — Horarios
                De lunes a viernes.
                ## INST-03 — Servicios
                Controles y limpiezas.
                ## INST-08 - Nueva política
                Contenido agregado.
                """;
        List<?> fragments = ReflectionTestUtils.invokeMethod(InstitutionalAssistant.class, "parse", corpus);
        assertThat(fragments).hasSize(3);
    }

    @Test
    void acceptsTheOriginalSixAndAnExpandedCorpus() {
        StringBuilder corpus = new StringBuilder("Versión 1.0.\n");
        for (int i = 1; i <= 8; i++) {
            corpus.append("## INST-%02d — Sección%nTexto institucional.%n".formatted(i));
            if (i >= 6) {
                List<?> fragments = ReflectionTestUtils.invokeMethod(InstitutionalAssistant.class, "parse", corpus.toString());
                assertThat(fragments).hasSize(i);
            }
        }
    }

    @Test
    void rejectsDuplicateIds() {
        assertThatThrownBy(() -> parse("""
                Versión 1.1.
                ## INST-01 — Horarios
                Texto.
                ## INST-01 — Otro título
                Otro texto.
                """)).hasMessageContaining("INST-01");
    }

    @Test
    void rejectsEmptyContentAndMalformedHeadings() {
        assertThatThrownBy(() -> parse("Versión 1.1.\n## INST-01 — Vacía\n"))
                .hasMessageContaining("INST-01");
        assertThatThrownBy(() -> parse("Versión 1.1.\n## Sección sin ID\nTexto.\n"))
                .hasMessageContaining("Encabezado");
    }

    @Test
    void requiresAVersionAndAtLeastOneSection() {
        assertThatThrownBy(() -> parse("## INST-01 — Horarios\nTexto."))
                .hasMessageContaining("versión");
        assertThatThrownBy(() -> parse("Versión 1.1.\n"))
                .hasMessageContaining("al menos una sección");
    }

    private void parse(String markdown) {
        ReflectionTestUtils.invokeMethod(InstitutionalAssistant.class, "parse", markdown);
    }
}
