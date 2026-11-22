package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OpenAiProtocolTest {
    @Test void schemaAllowsOnlyActualSourceIdsAndNoSourcesWhenNoneWereRetrieved() throws Exception {
        var json = new ObjectMapper();
        var builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new OpenAiClient("test-only");
        ReflectionTestUtils.setField(client, "client", builder.build());
        var output = Map.of("status", "completed", "output", List.of(Map.of("type", "message", "content",
                List.of(Map.of("type", "output_text", "text", "{\"respuesta\":\"Texto\"}")))));
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andExpect(jsonPath("$.text.format.schema.properties.fuentes_dinamicas.items.enum[0]").value("call-valid"))
                .andExpect(jsonPath("$.text.format.schema.properties.fuentes_institucionales.items.enum[0]").value("INST-06"))
                .andRespond(withSuccess(json.writeValueAsString(output), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andExpect(jsonPath("$.text.format.schema.properties.fuentes_dinamicas.maxItems").value(0))
                .andExpect(jsonPath("$.text.format.schema.properties.fuentes_institucionales.maxItems").value(0))
                .andRespond(withSuccess(json.writeValueAsString(output), MediaType.APPLICATION_JSON));
        var context = json.writeValueAsString(Map.of("herramientas_turno", List.of(Map.of("id", "call-valid")),
                "fuentes_institucionales_disponibles", List.of(Map.of("id", "INST-06"))));
        assertThat(client.writeDialogue("V4 literal", context).path("respuesta").asText()).isEqualTo("Texto");
        client.writeDialogue("V4 literal", "{}");
        server.verify();
    }
}
