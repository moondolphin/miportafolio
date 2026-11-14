package moondolphin.miportafolio.asistente;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssistantControllerTest {
    private static final String QUESTION = "¿Cómo reprogramo un turno confirmado?";

    @Test
    void ranksFragmentsAndSendsV4AndContextSeparately() throws Exception {
        var openAi = mock(OpenAiClient.class);
        when(openAi.embed(anyList())).thenAnswer(invocation -> {
            List<String> texts = invocation.getArgument(0);
            return texts.stream().map(text ->
                    text.equals(QUESTION) || text.contains("Cancelaciones y reprogramaciones")
                            ? new double[] {1, 0} : new double[] {0, 1})
                    .toArray(double[][]::new);
        });
        when(openAi.answer(anyString(), anyString(), eq(QUESTION))).thenReturn("Escribí a recepción.");
        var institutional = new InstitutionalAssistant(openAi);
        for (int attempt = 0; attempt < 2; attempt++) {
            var result = institutional.answer(QUESTION);
            assertThat(result.answer()).isEqualTo("Escribí a recepción.");
            assertThat(result.retrieved().id()).matches("INST-\\d{2,}");
        }
        verify(openAi).embed(argThat(texts -> texts.size() > 1));
        var prompt = ArgumentCaptor.forClass(String.class);
        var context = ArgumentCaptor.forClass(String.class);
        verify(openAi, times(2)).answer(prompt.capture(), context.capture(), eq(QUESTION));
        assertThat(prompt.getValue()).startsWith("Rol").contains("Manejo conversacional", "Cierre");
        assertThat(context.getValue()).contains("Cancelaciones y reprogramaciones", "24 horas", "recepcion@lostilos.example");
    }

    @Test
    void showsConfigurationErrorWhenTheServerKeyIsMissing() throws Exception {
        var institutional = new InstitutionalAssistant(new OpenAiClient(""));
        assertThatThrownBy(() -> institutional.answer(QUESTION))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("OPENAI_API_KEY");
    }
}
