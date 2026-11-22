package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingConversationTest {
    private final ObjectMapper json = new ObjectMapper();
    private final OpenAiClient openAi = mock(OpenAiClient.class);
    private final InstitutionalAssistant rag = mock(InstitutionalAssistant.class);
    private final AvailabilityClient availability = mock(AvailabilityClient.class);
    private final RegistrationClient registration = mock(RegistrationClient.class);
    private final BookingService booking = new BookingService(registration);
    private final BookingConversation conversation = new BookingConversation(booking, availability, openAi, rag,
            Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneId.of("America/Argentina/Buenos_Aires")));
    private final MockHttpSession session = new MockHttpSession();
    private final org.springframework.test.web.servlet.MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new AssistantController(conversation), new BookingController(booking, conversation)).build();

    private JsonNode plan(String operation, Boolean active, String name, String email, String reason,
                          String id, String date, String time, boolean consent, String query) {
        var value = json.createObjectNode();
        var tools = value.putArray("herramientas");
        if (!"ninguna".equals(operation) && !"cancelar_preparacion".equals(operation)) {
            tools.addObject().put("nombre", operation).putNull("solicitud_id");
        }
        value.put("cancelar_preparacion", "cancelar_preparacion".equals(operation));
        if (active != null) value.put("intencion_turno", active);
        value.put("confirmacion_explicita", consent);
        value.put("reintento_explicito", "reintentar".equals(operation));
        value.putObject("datos").put("nombre", name).put("correo", email).put("motivo", reason);
        value.putObject("seleccion").put("id", id).put("fecha", date).put("hora", time).put("ambigua", false);
        value.putArray("vaciar");
        value.put("consulta_institucional", query);
        return value;
    }

    private JsonNode say(String text, JsonNode plan) throws Exception {
        when(rag.prompt()).thenReturn("V4 exacto");
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(plan);
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> rendered(invocation.getArgument(1, String.class)));
        return send(text);
    }

    private com.fasterxml.jackson.databind.node.ObjectNode rendered(String input) throws Exception {
        var output = json.createObjectNode();
        output.put("respuesta", "Respuesta redactada por el modelo");
        output.putArray("herramientas");
        output.putArray("acciones_anunciadas");
        output.put("usa_datos_dinamicos", false);
        var sources = output.putArray("fuentes_dinamicas");
        for (var call : json.readTree(input).path("herramientas_turno")) sources.add(call.path("id").asText());
        return output;
    }

    private JsonNode send(String text) throws Exception {
        var result = mvc.perform(post("/api/v1/public/assistant/chat").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("message", text))))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store")).andReturn();
        return json.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private void slots() {
        when(availability.availableSlots()).thenReturn(List.of(
                new AvailabilityClient.Slot("2", "13/11/2026 17:00 a 13/11/2026 17:30",
                        "2026-11-13T17:00:00-03:00", "2026-11-13T17:30:00-03:00"),
                new AvailabilityClient.Slot("3", "13/11/2026 17:30 a 13/11/2026 18:00",
                        "2026-11-13T17:30:00-03:00", "2026-11-13T18:00:00-03:00")));
    }

    private JsonNode summary() throws Exception {
        slots();
        say("Me duele la muela", plan("ninguna", false, null, null, "dolor de muela", null, null, null, false, null));
        say("Quiero un turno", plan("disponibilidad", true, null, null, null, null, null, null, false, null));
        say("Soy Matías, matute_r@mimail.com", plan("ninguna", null, "Matías", "matute_r@mimail.com", null, null, null, null, false, null));
        return say("Mañana a las 17 me sirve", plan("ninguna", null, null, null, null, null, "2026-11-13", "17:00", false, null));
    }

    @Test
    void naturalSequenceAccumulatesMemoryAndResolvesTomorrowBeforeAskingForConsent() throws Exception {
        var result = summary();
        assertThat(result.path("booking").path("stage").asText()).isEqualTo("resumen");
        assertThat(result.path("booking").path("details").path("nombre").asText()).isEqualTo("Matías");
        assertThat(result.path("booking").path("details").path("motivo").asText()).isEqualTo("dolor de muela");
        assertThat(result.path("booking").path("details").path("horarioId").asText()).isEqualTo("2");
        verifyNoInteractions(registration);
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada",
                "681a1f4d-680a-4d1d-96ed-fb418e313329", "pendiente"));
        result = say("Sí, está bien así, enviá", plan("registrar", null, null, null, null, null, null, null, true, null));
        assertThat(result.path("booking").path("estado").asText()).isEqualTo("pendiente");
        verify(registration).register(anyMap());
        var next = say("Quiero otro turno", plan("disponibilidad", true, null, null, null, null, null, null, false, null));
        assertThat(next.path("conversation").path("details").path("horarioId").asText()).isEmpty();
        assertThat(next.path("booking").isNull()).isTrue();
        verify(registration).register(anyMap());
        verify(rag, never()).retrieve(anyString());
    }

    @Test
    void acceptsAnyOrderAndKeepsContextThroughAnInstitutionalQuestion() throws Exception {
        slots();
        say("correo matute_r@mimail.com y quiero un turno", plan("disponibilidad", true, null, "matute_r@mimail.com", null, null, null, null, false, null));
        say("El primero, me llamo Matías y es por dolor", plan("ninguna", null, "Matías", null, "dolor de muela", "2", null, null, false, null));
        when(rag.retrieve("horarios y canales de atención")).thenReturn(new InstitutionalAssistant.Retrieval(
                "INST-01: Lunes a viernes", new InstitutionalAssistant.Source("INST-01", "1.1")));
        var response = say("¿En qué horarios atienden?", plan("ninguna", null, null, null, null, null, null, null, false, "horarios y canales de atención"));
        assertThat(response.path("conversation").path("details").path("correo").asText()).isEqualTo("matute_r@mimail.com");
        assertThat(response.path("retrieved").path("id").asText()).isEqualTo("INST-01");
        ArgumentCaptor<String> contexts = ArgumentCaptor.forClass(String.class);
        verify(openAi, times(3)).writeDialogue(eq("V4 exacto"), contexts.capture());
        assertThat(contexts.getValue()).contains("historial", "El primero", "INST-01", "dolor de muela");
    }

    @Test
    void aCorrectionAndConsentInOneMessageRequiresAFreshReview() throws Exception {
        String old = summary().path("booking").path("attemptId").asText();
        var changed = say("Cambiá el correo a nueva@example.com y confirmá",
                plan("registrar", null, null, "nueva@example.com", null, null, null, null, true, null));
        assertThat(changed.path("booking").path("attemptId").asText()).isNotEqualTo(old);
        verifyNoInteractions(registration);
        mvc.perform(post("/api/v1/public/assistant/booking/confirm").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("attemptId", old))))
                .andExpect(status().isConflict());
    }

    @Test
    void refusesUnmatchedAndAmbiguousTimesWithoutDiscardingKnownContacts() throws Exception {
        summary();
        var response = say("Mañana a las 19", plan("ninguna", null, null, null, null, null, "2026-11-13", "19:00", false, null));
        assertThat(response.path("conversation").path("details").path("horarioId").asText()).isEmpty();
        assertThat(response.path("conversation").path("details").path("nombre").asText()).isEqualTo("Matías");
        var ambiguous = plan("ninguna", null, null, null, null, null, null, null, false, null);
        ((com.fasterxml.jackson.databind.node.ObjectNode) ambiguous.path("seleccion")).put("ambigua", true);
        response = say("Ese horario", ambiguous);
        assertThat(response.path("booking").isNull()).isTrue();
        ArgumentCaptor<String> context = ArgumentCaptor.forClass(String.class);
        verify(openAi, atLeastOnce()).writeDialogue(eq("V4 exacto"), context.capture());
        assertThat(context.getValue()).contains("aclaracion_horario", "ambigua");
        verifyNoInteractions(registration);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void uncertainRetryKeepsTheOriginalPayloadWhileAllowingDraftEdits() throws Exception {
        summary();
        when(registration.register(anyMap())).thenReturn(RegistrationClient.Result.unknown(),
                new RegistrationClient.Result("ya_registrada", "681a1f4d-680a-4d1d-96ed-fb418e313329", "pendiente"));
        say("Confirmo", plan("registrar", null, null, null, null, null, null, null, true, null));
        say("Mi correo nuevo es otro@example.com", plan("ninguna", null, null, "otro@example.com", null, null, null, null, false, null));
        var recovered = say("Verificá el envío anterior", plan("reintentar", null, null, null, null, null, null, null, false, null));
        ArgumentCaptor<Map<String, String>> payloads = ArgumentCaptor.forClass((Class) Map.class);
        verify(registration, times(2)).register(payloads.capture());
        assertThat(payloads.getAllValues().get(0)).isEqualTo(payloads.getAllValues().get(1));
        assertThat(recovered.path("conversation").path("details").path("correo").asText()).isEqualTo("otro@example.com");
        assertThat(recovered.path("booking").path("details").path("correo").asText()).isEqualTo("matute_r@mimail.com");
    }

    private RegistrationClient.LookupResult stored(String state) {
        return new RegistrationClient.LookupResult("encontrada", new RegistrationClient.StoredRequest(
                "681a1f4d-680a-4d1d-96ed-fb418e313329", "2", "Matías", "matute_r@mimail.com",
                "dolor de muela", state, "2026-11-13T17:00:00-03:00", "2026-11-13T17:30:00-03:00"));
    }

    @Test
    void repeatedAvailabilityQueriesUseNewCallsAndChangingResponses() throws Exception {
        slots();
        var first = availability.availableSlots();
        var replacement = List.of(new AvailabilityClient.Slot("9", "16/11/2026 10:00 a 16/11/2026 10:30",
                "2026-11-16T10:00:00-03:00", "2026-11-16T10:30:00-03:00"));
        clearInvocations(availability);
        when(availability.availableSlots()).thenReturn(first, replacement);
        var a = say("Opciones para esta semana", plan("disponibilidad", true, null, null, null, null, null, null, false, null));
        var b = say("Actualizá la consulta para otro día", plan("disponibilidad", null, null, null, null, null, null, null, false, null));
        verify(availability, times(2)).availableSlots();
        assertThat(b.path("horarios").get(0).path("id").asText()).isEqualTo("9");
        assertThat(a.path("herramientas").get(0).path("id").asText())
                .isNotEqualTo(b.path("herramientas").get(0).path("id").asText());
    }

    @Test
    void rereadsStatusAndStoredDataAfterRegistrationAndDraftCorrections() throws Exception {
        summary();
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada",
                "681a1f4d-680a-4d1d-96ed-fb418e313329", "pendiente"));
        say("Autorizo el envío", plan("registrar", null, null, null, null, null, null, null, true, null));
        when(registration.inspect(anyMap())).thenReturn(stored("pendiente"), stored("confirmada"), stored("confirmada"));
        var first = say("Verificá el estado de la solicitud", plan("consultar_solicitud", null, null, null, null, null, null, null, false, null));
        var second = say("Consultala de nuevo", plan("consultar_solicitud", null, null, null, null, null, null, null, false, null));
        assertThat(first.path("booking").path("estado").asText()).isEqualTo("pendiente");
        assertThat(second.path("booking").path("estado").asText()).isEqualTo("confirmada");
        var third = say("Mi borrador lleva otro correo; verificá qué se guardó",
                plan("consultar_solicitud", null, null, "otro@example.com", null, null, null, null, false, null));
        verify(registration, times(3)).inspect(anyMap());
        assertThat(third.path("conversation").path("details").path("correo").asText()).isEqualTo("otro@example.com");
        ArgumentCaptor<String> context = ArgumentCaptor.forClass(String.class);
        verify(openAi, atLeastOnce()).writeDialogue(eq("V4 exacto"), context.capture());
        var actual = json.readTree(context.getValue());
        assertThat(actual.path("herramientas_turno").get(0).path("resultado").path("solicitud").path("correo").asText())
                .isEqualTo("matute_r@mimail.com");
        assertThat(context.getValue()).doesNotContain("comprobante_hash", "clave_idempotencia");
    }

    @Test
    void aToolDiscoveredWhileWritingRunsBeforeTheResponseIsDelivered() throws Exception {
        summary();
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada",
                "681a1f4d-680a-4d1d-96ed-fb418e313329", "pendiente"));
        say("Autorizo el envío", plan("registrar", null, null, null, null, null, null, null, true, null));
        when(registration.inspect(anyMap())).thenReturn(stored("confirmada"));
        when(openAi.planDialogue(eq("V4 exacto"), anyString()))
                .thenReturn(plan("ninguna", null, null, null, null, null, null, null, false, null));
        final int[] round = {0};
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var answer = rendered(invocation.getArgument(1, String.class));
            if (round[0]++ == 0) {
                answer.put("respuesta", "Borrador que anuncia una verificación.");
                answer.withArray("acciones_anunciadas").add("consultar_solicitud");
            } else answer.put("respuesta", "Resultado redactado después de verificar.");
            return answer;
        });
        var result = send("¿Podés comprobar los datos guardados?");
        assertThat(result.path("answer").asText()).isEqualTo("Resultado redactado después de verificar.");
        assertThat(result.path("herramientas").size()).isEqualTo(1);
        verify(registration).inspect(anyMap());
    }

    @Test
    void supportsSeveralToolsInOneTurnAndDoesNotExposeAnotherSessionsRequest() throws Exception {
        summary();
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada",
                "681a1f4d-680a-4d1d-96ed-fb418e313329", "pendiente"));
        say("Autorizo el envío", plan("registrar", null, null, null, null, null, null, null, true, null));
        when(registration.inspect(anyMap())).thenReturn(stored("pendiente"));
        var combined = plan("disponibilidad", null, null, null, null, null, null, null, false, null);
        ((com.fasterxml.jackson.databind.node.ObjectNode) combined).withArray("herramientas")
                .addObject().put("nombre", "consultar_solicitud").putNull("solicitud_id");
        var result = say("Consultá estado y nuevas opciones", combined);
        assertThat(result.path("herramientas").size()).isEqualTo(2);
        assertThat(result.path("herramientas").get(1).path("herramienta").asText()).isEqualTo("consultar_solicitud");
        clearInvocations(registration);
        var foreign = plan("consultar_solicitud", null, null, null, null, null, null, null, false, null);
        ((com.fasterxml.jackson.databind.node.ObjectNode) foreign.path("herramientas").get(0))
                .put("solicitud_id", "11111111-1111-4111-8111-111111111111");
        result = say("Consultá este otro identificador", foreign);
        assertThat(result.path("herramientas").get(0).path("exito").asBoolean()).isFalse();
        verifyNoInteractions(registration);
    }

    @Test
    void isolatesSessionsAndCancelsPreparationWithoutACannedDialogue() throws Exception {
        summary();
        var cancelled = say("Dejémoslo para después", plan("cancelar_preparacion", false, null, null, null, null, null, null, false, null));
        assertThat(cancelled.path("conversation").path("active").asBoolean()).isFalse();
        assertThat(cancelled.path("answer").asText()).isEqualTo("Respuesta redactada por el modelo");
        assertThat(conversation.current(new MockHttpSession())).isNull();
        assertThat(booking.current(session)).isNull();
        assertThat(conversation.current(session).history()).hasSize(10);
    }

    @Test
    void prematureEffectsFromPlanAndWriterAreNotInvokedOrShownAsFailures() throws Exception {
        when(rag.prompt()).thenReturn("V4 exacto");
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(
                plan("registrar", true, null, null, null, null, null, null, false, null));
        final int[] count = {0};
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var output = rendered(invocation.getArgument(1, String.class));
            if (count[0]++ == 0) {
                output.withArray("herramientas").addObject().put("nombre", "registrar");
                output.withArray("acciones_anunciadas").add("registrar");
            }
            return output;
        });
        var response = send("Quisiera solicitar un turno");
        assertThat(response.path("herramientas")).isEmpty();
        verifyNoInteractions(registration);
        ArgumentCaptor<String> context = ArgumentCaptor.forClass(String.class);
        verify(openAi, times(2)).writeDialogue(eq("V4 exacto"), context.capture());
        assertThat(context.getValue()).contains("operacion_no_habilitada", "\"ejecutada\":false");
    }

    @Test
    void selectionIsNotConsentAndAReviewCreatedInTheSameTurnDoesNotAuthorizeRegistration() throws Exception {
        summary();
        var choice = say("Ese horario me sirve", plan("registrar", null, null, null, null, "2", null, null, false, null));
        assertThat(choice.path("herramientas")).isEmpty();
        verifyNoInteractions(registration);
        var changed = say("Cambiá al otro y enviá", plan("registrar", null, null, null, null, "3", null, null, true, null));
        assertThat(changed.path("booking").path("details").path("horarioId").asText()).isEqualTo("3");
        assertThat(changed.path("herramientas")).isEmpty();
        verifyNoInteractions(registration);
    }

    @Test
    void repeatedEffectsInPlanAndWriterExecuteOnceEvenAfterTimeout() throws Exception {
        summary();
        when(registration.register(anyMap())).thenReturn(RegistrationClient.Result.unknown());
        var plan = plan("registrar", null, null, null, null, null, null, null, true, null);
        ((com.fasterxml.jackson.databind.node.ObjectNode) plan).withArray("herramientas")
                .addObject().put("nombre", "registrar");
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(plan);
        final int[] rounds = {0};
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var output = rendered(invocation.getArgument(1, String.class));
            if (rounds[0]++ == 0) output.withArray("herramientas").addObject().put("nombre", "reintentar");
            return output;
        });
        var response = send("Confirmo el resumen, enviá la solicitud");
        assertThat(response.path("booking").path("stage").asText()).isEqualTo("indeterminado");
        assertThat(response.path("herramientas").size()).isEqualTo(1);
        assertThat(response.path("herramientas").get(0).path("tipo").asText()).isEqualTo("efecto");
        verify(registration, times(1)).register(anyMap());
    }

    @Test
    void resolvedAttemptsAreReadAgainRatherThanWrittenAgain() throws Exception {
        summary();
        when(registration.register(anyMap())).thenReturn(new RegistrationClient.Result("registrada",
                "681a1f4d-680a-4d1d-96ed-fb418e313329", "pendiente"));
        say("Confirmo el envío", plan("registrar", null, null, null, null, null, null, null, true, null));
        var response = say("Volvé a verificarlo", plan("registrar", null, null, null, null, null, null, null, true, null));
        assertThat(response.path("herramientas")).isEmpty();
        when(registration.inspect(anyMap())).thenReturn(stored("pendiente"));
        response = say("Consultá el estado actual", plan("consultar_solicitud", null, null, null, null, null, null, null, false, null));
        assertThat(response.path("herramientas").get(0).path("tipo").asText()).isEqualTo("lectura");
        verify(registration, times(1)).register(anyMap());
        verify(registration).inspect(anyMap());
    }

    @Test
    void institutionalConflictsAreExcludedBeforeTheyReachTheModelAsOffers() throws Exception {
        when(availability.availableSlots()).thenReturn(List.of(
            new AvailabilityClient.Slot("20", "Sábado", "2026-11-14T10:00:00-03:00", "2026-11-14T10:30:00-03:00"),
            new AvailabilityClient.Slot("21", "Viernes", "2026-11-13T10:00:00-03:00", "2026-11-13T10:30:00-03:00")));
        var result = say("Qué opciones hay", plan("disponibilidad", true, null, null, null, null, null, null, false, null));
        assertThat(result.path("horarios").size()).isEqualTo(1);
        assertThat(result.path("horarios").get(0).path("id").asText()).isEqualTo("21");
        verifyNoInteractions(registration);
    }

    @Test
    void anUnknownAttemptDoesNotAuthorizeAnUnrequestedRetry() throws Exception {
        summary();
        when(registration.register(anyMap())).thenReturn(RegistrationClient.Result.unknown());
        say("Confirmo el envio", plan("registrar", null, null, null, null, null, null, null, true, null));
        var retry = plan("reintentar", null, null, null, null, null, null, null, false, null);
        ((com.fasterxml.jackson.databind.node.ObjectNode) retry).put("reintento_explicito", false);
        var response = say("Que paso con mi solicitud", retry);
        assertThat(response.path("herramientas")).isEmpty();
        verify(registration, times(1)).register(anyMap());
    }

    @Test
    void institutionalFactsDiscoveredWhileWritingAreRetrievedBeforeDelivery() throws Exception {
        when(rag.prompt()).thenReturn("V4 exacto");
        when(rag.retrieve("política institucional de reprogramaciones")).thenReturn(new InstitutionalAssistant.Retrieval(
                "INST-01: horarios. INST-06: comunicar con recepción", new InstitutionalAssistant.Source("INST-01", "1.1"),
                List.of(new InstitutionalAssistant.Source("INST-01", "1.1"), new InstitutionalAssistant.Source("INST-06", "1.1"))));
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(
                plan("ninguna", null, null, null, null, null, null, null, false, null));
        final int[] rounds = {0};
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var output = rendered(invocation.getArgument(1, String.class));
            if (rounds[0]++ == 0) output.put("consulta_institucional", "política institucional de reprogramaciones");
            else {
                output.put("afirma_politica_institucional", true);
                output.withArray("fuentes_institucionales").add(rounds[0] == 2 ? "INST-99" : "INST-06");
            }
            return output;
        });
        var response = send("Qué procedimiento administrativo corresponde para cambiar una cita?");
        assertThat(response.path("retrieved").path("id").asText()).isEqualTo("INST-06");
        verify(rag).retrieve("política institucional de reprogramaciones");
        verifyNoInteractions(registration);
    }

    @Test
    void anUnresolvedProviderLoopReturnsOnlySafeRouteDiagnosticsAndNoEffects() throws Exception {
        when(rag.prompt()).thenReturn("V4 exacto");
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(
                plan("ninguna", null, null, null, null, null, null, null, false, null));
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var output = rendered(invocation.getArgument(1, String.class));
            output.put("usa_datos_dinamicos", true);
            return output;
        });
        mvc.perform(post("/api/v1/public/assistant/chat").session(session)
                .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"Comprobar sin inventar datos\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.diagnostico.rutas[0]").value("DYNAMIC_SOURCE_INVALID"));
        verifyNoInteractions(registration);
    }

    @Test
    void aMissingInstitutionalSourceTriggersActualRetrievalInsteadOfRepeatingTheRejectedDraft() throws Exception {
        slots();
        say("Hola", plan("ninguna", null, null, null, null, null, null, null, false, null));
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(
                plan("disponibilidad", true, null, null, "dolor de muela", null, null, null, false, null));
        when(rag.topics()).thenReturn(List.of("Solicitud de turnos"));
        when(openAi.groundingQuery(eq("V4 exacto"), anyString(), anyString(), anyList())).thenReturn("Solicitud de turnos");
        when(rag.retrieve("Solicitud de turnos")).thenReturn(new InstitutionalAssistant.Retrieval(
                "INST-04: datos y conformidad para solicitar turnos", new InstitutionalAssistant.Source("INST-04", "1.1")));
        final int[] rounds = {0};
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var output = rendered(invocation.getArgument(1, String.class));
            output.put("afirma_politica_institucional", true);
            if (rounds[0]++ > 0) output.withArray("fuentes_institucionales").add("INST-04");
            return output;
        });
        var result = send("sí, me duele la muela, quiero sacar un turno");
        assertThat(result.path("retrieved").path("id").asText()).isEqualTo("INST-04");
        assertThat(result.path("conversation").path("details").path("motivo").asText()).isEqualTo("dolor de muela");
        verify(rag).retrieve("requisitos administrativos para solicitar un turno");
        verify(openAi).groundingQuery(eq("V4 exacto"), anyString(), anyString(), eq(List.of("Solicitud de turnos")));
        verify(availability).availableSlots();
        verifyNoInteractions(registration);
    }

    @Test
    void groundingDoesNotBlockAToolRequestedInTheSameProvisionalDraft() throws Exception {
        slots();
        when(rag.prompt()).thenReturn("V4 exacto");
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(
                plan("ninguna", true, null, null, null, null, null, null, false, null));
        when(rag.retrieve("información administrativa para solicitudes")).thenReturn(new InstitutionalAssistant.Retrieval(
                "INST-04: requisitos", new InstitutionalAssistant.Source("INST-04", "1.1")));
        final int[] rounds = {0};
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var output = rendered(invocation.getArgument(1, String.class));
            output.put("afirma_politica_institucional", true);
            if (rounds[0]++ == 0) {
                output.put("consulta_institucional", "información administrativa para solicitudes");
                output.withArray("herramientas").addObject().put("nombre", "disponibilidad");
            } else output.withArray("fuentes_institucionales").add("INST-04");
            return output;
        });
        var result = send("Necesito consultar la agenda y cómo se solicita atención");
        assertThat(result.path("herramientas").size()).isEqualTo(1);
        verify(availability).availableSlots();
        verify(rag).retrieve("información administrativa para solicitudes");
        verifyNoInteractions(registration);
    }

    @Test
    void aRepairQueryContainingPersonalDataNeverReachesEmbeddings() throws Exception {
        when(rag.prompt()).thenReturn("V4 exacto");
        when(openAi.planDialogue(eq("V4 exacto"), anyString())).thenReturn(
                plan("ninguna", null, null, null, null, null, null, null, false, null));
        when(rag.topics()).thenReturn(List.of("Solicitud de turnos"));
        when(openAi.groundingQuery(eq("V4 exacto"), anyString(), anyString(), anyList())).thenReturn("persona@example.com dolor de muela");
        when(openAi.writeDialogue(eq("V4 exacto"), anyString())).thenAnswer(invocation -> {
            var output = rendered(invocation.getArgument(1, String.class));
            output.put("afirma_politica_institucional", true);
            return output;
        });
        mvc.perform(post("/api/v1/public/assistant/chat").session(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Consulta administrativa\"}"))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.error").value(
                        "No pude recuperar una fuente institucional segura para esta respuesta. Volvé a intentar."));
        verify(rag, never()).retrieve(anyString());
        verifyNoInteractions(registration);
    }
}
