package moondolphin.miportafolio.asistente;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
class BookingConversation {
    private static final Logger LOG = LoggerFactory.getLogger(BookingConversation.class);
    private static final String SESSION_KEY = BookingConversation.class.getName();
    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    private final BookingService booking;
    private final AvailabilityClient availability;
    private final OpenAiClient openAi;
    private final InstitutionalAssistant institutional;
    private final Clock clock;
    private final ObjectMapper json = new ObjectMapper();

    @Autowired
    BookingConversation(BookingService booking, AvailabilityClient availability,
                        OpenAiClient openAi, InstitutionalAssistant institutional) {
        this(booking, availability, openAi, institutional, Clock.system(ZONE));
    }

    BookingConversation(BookingService booking, AvailabilityClient availability,
                        OpenAiClient openAi, InstitutionalAssistant institutional, Clock clock) {
        this.booking = booking;
        this.availability = availability;
        this.openAi = openAi;
        this.institutional = institutional;
        this.clock = clock;
    }

    static void sync(HttpSession session, BookingService.View view) {
        State state = state(session);
        if ("resumen".equals(view.stage()) || state.details.equals(view.details())) {
            state.details = view.details();
            state.active = !"registrada".equals(view.stage());
        }
        state.operation = view.stage();
    }

    void reviewShown(HttpSession session, BookingService.View view) {
        synchronized (session) {
            if ("resumen".equals(view.stage())) state(session).reviewedAttempt = view.attemptId();
        }
    }

    BookingService.View currentAttempt(HttpSession session) {
        return booking.current(session);
    }

    Context current(HttpSession session) {
        if (session == null) return null;
        synchronized (session) {
            State state = (State) session.getAttribute(SESSION_KEY);
            return state == null ? null : state.context();
        }
    }

    InstitutionalAssistant.ChatReply reply(HttpSession session, String message) {
        return reply(session, message, UUID.randomUUID().toString(), false);
    }

    InstitutionalAssistant.ChatReply reply(HttpSession session, String message, String requestId, boolean retry) {
        synchronized (session) {
            State state = state(session);
            if (retry && state.turn != null && !requestId.equals(state.requestId)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                        "El intento ya no pertenece al turno actual. Conservá tus datos y retomá la conversación.");
            }
            boolean resuming = requestId.equals(state.requestId);
            if (resuming && !message.equals(state.turn.message)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "El reintento debe conservar el mensaje original.");
            }
            if (!resuming) {
                state.requestId = requestId;
                state.turn = new TurnWork(message, booking.current(session), state.reviewedAttempt);
            }
            var turn = state.turn;
            if (turn.completed != null) return turn.completed;
            LOG.info("asistente id={} evento={} ", turn.trace, resuming ? "reintento" : "inicio");
            if (resuming) {
                if (turn.retrieved == null) turn.groundingRepairAttempted = false;
                if (turn.historyWritten) {
                    state.history.subList(state.history.size() - 2, state.history.size()).clear();
                    turn.historyWritten = false;
                }
                recoverFailures(session, state, turn);
            }
            if (!turn.initialized) {
                JsonNode plan = openAi.planDialogue(institutional.prompt(), input(session, state, turn, null));
                turn.plan = plan;
                turn.confirmed = plan.path("confirmacion_explicita").asBoolean(false);
                var updated = update(state.details, plan, turn.validation);
                if (!updated.equals(state.details) || !turn.validation.isEmpty()) {
                    invalidateReview(session, state);
                    state.details = updated;
                    if (state.authorizedIncomplete != null && !preservesKnownData(state.authorizedIncomplete, updated)) {
                        state.authorizedIncomplete = null;
                    }
                }
                if (!state.active && plan.path("intencion_turno").asBoolean(false)
                        && turn.previous != null && "registrada".equals(turn.previous.stage())) {
                    booking.clearCompleted(session);
                    state.authorizedIncomplete = null;
                    invalidateReview(session, state);
                    state.details = new BookingService.Details("", state.details.nombre(),
                            state.details.correo(), state.details.motivo());
                }
                if (plan.path("intencion_turno").isBoolean()) state.active = plan.path("intencion_turno").asBoolean();
                if (plan.path("cancelar_preparacion").asBoolean()) {
                    invalidateReview(session, state);
                    state.active = false;
                    state.details = new BookingService.Details("", state.details.nombre(), state.details.correo(), state.details.motivo());
                    state.authorizedIncomplete = null;
                    turn.notes.put("preparacion_cancelada", true);
                }
                turn.initialized = true;
                if (state.authorizedIncomplete != null) prepareReview(session, state, turn);
                runActions(session, state, turn, plan.path("herramientas"));
                recoverUncertainRegistration(session, state, turn);
                resolveSelection(session, state, turn);
            }

            InstitutionalAssistant.Retrieval retrieved = turn.retrieved;
            String query = turn.plan.path("consulta_institucional").asText("").strip();
            if (retrieved == null && !query.isEmpty() && institutionalQuery(query, state.details)) {
                retrieved = institutional.retrieve(query);
                turn.institutionalResults.put(queryKey(query), retrieved);
                turn.retrieved = retrieved;
            }

            for (int round = 0; round < 4; round++) {
                turn.diagnosticRound = round + 1;
                LOG.info("asistente id={} ronda={} evento=inicio herramientas_ejecutadas={}",
                        turn.trace, turn.diagnosticRound, turn.calls.size());
                var pending = prepareReview(session, state, turn);
                JsonNode draft = openAi.writeDialogue(institutional.prompt(), input(session, state, turn, retrieved));
                boolean contextChanged = false;
                String institutionalNeed = draft.path("consulta_institucional").asText("").strip();
                if (!institutionalNeed.isEmpty()) {
                    turn.routes.add("RAG_REQUESTED");
                    String key = queryKey(institutionalNeed);
                    if (turn.institutionalResults.containsKey(key)) {
                        LOG.info("asistente id={} ronda={} decision=RAG_REUTILIZADO", turn.trace, turn.diagnosticRound);
                        turn.notes.put("contexto_institucional_ya_recuperado", true);
                    } else if (institutionalQuery(institutionalNeed, state.details)) {
                        var additional = institutional.retrieve(institutionalNeed);
                        turn.institutionalResults.put(key, additional);
                        retrieved = mergeRetrieval(retrieved, additional);
                        turn.retrieved = retrieved;
                        contextChanged = true;
                    } else {
                        LOG.info("asistente id={} ronda={} decision=RAG_CONSULTA_RECHAZADA", turn.trace, turn.diagnosticRound);
                        turn.notes.put("consulta_institucional_invalida", "La recuperación requiere una consulta general sin datos personales.");
                    }
                }
                var requested = json.createArrayNode();
                for (var action : draft.path("herramientas")) requested.add(action);
                for (var announced : draft.path("acciones_anunciadas")) {
                    if (turn.calls.stream().noneMatch(call -> call.herramienta().equals(announced.asText()))) {
                        requested.addObject().put("nombre", announced.asText()).putNull("solicitud_id");
                    }
                }
                if (!requested.isEmpty()) {
                    turn.routes.add("TOOLS_REQUESTED");
                    contextChanged |= runActions(session, state, turn, requested);
                    recoverUncertainRegistration(session, state, turn);
                    resolveSelection(session, state, turn);
                }
                // A provisional draft may request both kinds of context. Fulfil requests before
                // validating the final text, so grounding cannot starve an external read.
                if (contextChanged) {
                    logRound(turn, state, draft, retrieved, "NUEVO_CONTEXTO");
                    continue;
                }
                if (draft.path("afirma_politica_institucional").asBoolean(false) && !validInstitutionalSources(draft, retrieved)) {
                    turn.routes.add("INSTITUTIONAL_SOURCE_INVALID");
                    logRound(turn, state, draft, retrieved, "INSTITUTIONAL_SOURCE_INVALID");
                    if (retrieved == null && !turn.groundingRepairAttempted) {
                        turn.groundingRepairAttempted = true;
                        String clean = openAi.groundingQuery(institutional.prompt(), input(session, state, turn, null),
                                draft.path("respuesta").asText(""), institutional.topics());
                        if (clean == null || clean.isBlank() || !institutional.topics().contains(clean)) {
                            LOG.warn("asistente id={} ronda={} decision=RAG_REPARACION_RECHAZADA", turn.trace, turn.diagnosticRound);
                            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                    "No pude recuperar una fuente institucional segura para esta respuesta. Volvé a intentar.");
                        }
                        retrieved = institutional.retrieve(clean);
                        turn.retrieved = retrieved;
                        turn.institutionalResults.put(queryKey(clean), retrieved);
                        turn.notes.put("respaldo_institucional_recuperado", true);
                    } else {
                        turn.notes.put("fuente_institucional_faltante", "Citar los IDs institucionales disponibles o pedir contexto adicional; no repetir el borrador sin respaldo.");
                    }
                    continue;
                }
                if (!validSources(draft, turn.calls)) {
                    turn.routes.add("DYNAMIC_SOURCE_INVALID");
                    logRound(turn, state, draft, retrieved, "DYNAMIC_SOURCE_INVALID");
                    turn.notes.put("fuentes_invalidas", "Usar solo IDs de herramientas de este turno; consultar la fuente si hace falta.");
                    continue;
                }
                String answer = draft.path("respuesta").asText("");
                if (answer.isBlank()) throw OpenAiClient.unavailable();
                var actual = booking.current(session);
                boolean registrationRequested = turn.effectExecuted || turn.confirmed
                        || turn.notes.containsKey("operacion_no_habilitada");
                if (registrationRequested && !hasReceipt(actual)) {
                    LOG.warn("asistente id={} evento=registro_sin_comprobante estado={}",
                            turn.trace, actual == null ? "sin_intento" : actual.stage());
                    answer = registrationPendingMessage(actual, state, turn);
                }
                rememberTurn(state, "user", message);
                rememberTurn(state, "assistant", answer);
                turn.historyWritten = true;
                if (pending != null && "resumen".equals(pending.stage())) state.reviewedAttempt = pending.attemptId();
                logRound(turn, state, draft, retrieved, "RESPUESTA_ENTREGADA");
                var response = new InstitutionalAssistant.ChatReply(answer, usedInstitutionalSource(draft, retrieved),
                        state.slots, booking.current(session), state.context(), turn.calls.stream().map(Call::evidence).toList());
                if (turn.calls.stream().allMatch(Call::exito)) turn.completed = response;
                return response;
            }
            LOG.warn("asistente id={} evento=limite_rondas rondas={} rutas={} herramientas_ejecutadas={}",
                    turn.trace, turn.diagnosticRound, turn.routes, turn.calls.size());
            throw new ToolCycleException(turn.routes);
        }
    }

    private static String registrationPendingMessage(BookingService.View attempt, State state, TurnWork turn) {
        if (attempt != null && "resumen".equals(attempt.stage())) {
            var details = attempt.details();
            return "Solicitud preparada: " + details.nombre() + ", " + details.correo()
                    + ". Motivo informado: " + details.motivo() + ". Horario: " + attempt.horario()
                    + ". " + attempt.message() + " ¿Confirmás que envíe la solicitud?";
        }
        if (attempt != null && !"registrada".equals(attempt.stage())) return attempt.message();
        var missing = new ArrayList<String>(turn.validation);
        if (!turn.selectionIssue.isBlank()) missing.add(turn.selectionIssue);
        if (state.details.nombre().isBlank()) missing.add("Falta tu nombre.");
        if (state.details.correo().isBlank()) missing.add("Falta un correo electrónico válido.");
        if (state.details.motivo().isBlank()) missing.add("Falta el motivo informado de la consulta.");
        if (state.details.horarioId().isBlank()) missing.add("Falta elegir un horario válido de los ofrecidos.");
        return "No se registró la solicitud. " + (missing.isEmpty()
                ? "Todavía no hay un resumen vigente con comprobante de envío. Necesito verificar la preparación."
                : String.join(" ", missing));
    }

    // Filling missing fields preserves consent; changing an already supplied field does not.
    private static boolean preservesKnownData(BookingService.Details known, BookingService.Details updated) {
        return (known.nombre().isBlank() || known.nombre().equals(updated.nombre()))
                && (known.correo().isBlank() || known.correo().equals(updated.correo()))
                && (known.motivo().isBlank() || known.motivo().equals(updated.motivo()))
                && (known.horarioId().isBlank() || known.horarioId().equals(updated.horarioId()));
    }

    private static boolean hasReceipt(BookingService.View attempt) {
        return attempt != null && "registrada".equals(attempt.stage())
                && attempt.solicitudId() != null && !attempt.solicitudId().isBlank()
                && attempt.comprobante() != null && !attempt.comprobante().isBlank();
    }

    private void recoverUncertainRegistration(HttpSession session, State state, TurnWork turn) {
        var attempt = booking.current(session);
        if (!turn.effectExecuted || turn.registrationRecoveryAttempted || attempt == null
                || !"indeterminado".equals(attempt.stage())) return;
        turn.registrationRecoveryAttempted = true;
        LOG.info("asistente id={} evento=verificar_registro_antes_de_reenviar", turn.trace);
        // Only an already authorized send reaches this path. BookingService verifies
        // existence and reuses the original payload/idempotency key before any resend.
        recoverFailures(session, state, turn);
    }

    // Recovery executes only operations that actually failed in this authorized turn.
    // Successful effects remain recorded even if subsequent OpenAI generation fails.
    private void recoverFailures(HttpSession session, State state, TurnWork turn) {
        for (Call failed : List.copyOf(turn.calls)) {
            if (failed.exito()) continue;
            if (AssistantTool.named(failed.herramienta()).hasEffect()) {
                var attempt = booking.current(session);
                if (attempt == null) throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                        "Se perdió el intento de registro. No se reenvió.");
                var saved = booking.confirm(session, attempt.attemptId());
                if (!"registrada".equals(saved.stage())) {
                    throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, saved.message());
                }
                turn.calls.set(turn.calls.indexOf(failed), new Call(UUID.randomUUID().toString(),
                        failed.herramienta(), now(), true, publicAttempt(saved)));
            } else {
                var key = turn.readResults.entrySet().stream().filter(e -> e.getValue().id().equals(failed.id()))
                        .map(Map.Entry::getKey).findFirst().orElseThrow();
                turn.calls.remove(failed);
                turn.readResults.remove(key);
                var actions = json.createArrayNode();
                actions.addObject().put("nombre", key.tool()).put("solicitud_id", key.target());
                runActions(session, state, turn, actions);
            }
        }
    }

    private static void logRound(TurnWork turn, State state, JsonNode draft,
                                 InstitutionalAssistant.Retrieval retrieved, String route) {
        LOG.info("asistente id={} ronda={} ruta={} herramientas_pedidas={} herramientas_ejecutadas={} afirma_institucional={} fuentes_rag_disponibles={} citas_institucionales={} afirma_dinamicos={} citas_dinamicas={} horario_seleccionado={} requiere_aclaracion={}",
                turn.trace, turn.diagnosticRound, route, draft.path("herramientas").size(), turn.calls.size(),
                draft.path("afirma_politica_institucional").asBoolean(false),
                retrieved == null ? 0 : retrieved.sources().size(), draft.path("fuentes_institucionales").size(),
                draft.path("usa_datos_dinamicos").asBoolean(false), draft.path("fuentes_dinamicas").size(),
                !state.details.horarioId().isBlank(), !turn.selectionIssue.isBlank());
    }

    private boolean runActions(HttpSession session, State state, TurnWork turn, JsonNode actions) {
        boolean changed = false;
        for (JsonNode action : actions) {
            String tool = action.path("nombre").asText();
            String target = action.path("solicitud_id").asText("");
            var definition = AssistantTool.named(tool);
            if (definition == null) {
                turn.notes.put("herramienta_no_disponible", tool);
                continue;
            }
            ReadKey key = readKey(session, tool, target, turn.externalRevision);
            if (!definition.hasEffect() && turn.readResults.containsKey(key)) {
                var previous = turn.readResults.get(key);
                LOG.info("asistente id={} ronda={} herramienta={} decision=LECTURA_REUTILIZADA resultado_id={} revision={}",
                        turn.trace, turn.diagnosticRound, definition.name(), previous.id(), turn.externalRevision);
                turn.notes.put("lectura_ya_resuelta_en_turno", Map.of("herramienta", tool,
                        "resultado_id", previous.id(), "exito", previous.exito()));
                continue;
            }
            if (definition.hasEffect()) {
                resolveSelection(session, state, turn);
                // Eligibility is checked before creating evidence or invoking the tool.
                String denial = effectDenial(session, state, turn, tool, target);
                if (!denial.isEmpty()) {
                    if ("registrar".equals(tool) && turn.confirmed && !complete(state.details)) {
                        state.authorizedIncomplete = state.details;
                        state.active = true;
                    }
                    LOG.info("asistente id={} ronda={} herramienta={} decision=EFECTO_NO_HABILITADO",
                            turn.trace, turn.diagnosticRound, definition.name());
                    turn.notes.put("operacion_no_habilitada", Map.of("herramienta", tool, "motivo", denial,
                            "ejecutada", false));
                    continue;
                }
                turn.effectExecuted = true;
            }
            String id = UUID.randomUUID().toString();
            Map<String, Object> result;
            boolean success = true;
            try {
                switch (tool) {
                    case "disponibilidad" -> {
                        var received = availability.availableSlots();
                        state.slots = received.stream().filter(InstitutionalCalendar.current()::permits).toList();
                        turn.notes.put("cupos_excluidos_por_politica", received.size() - state.slots.size());
                        state.slotsObservedAt = now();
                        booking.remember(session, state.slots);
                        if (state.slots.stream().noneMatch(slot -> slot.id().equals(state.details.horarioId()))) {
                            invalidateReview(session, state);
                            state.details = new BookingService.Details("", state.details.nombre(),
                                    state.details.correo(), state.details.motivo());
                        }
                        result = Map.of("horarios", state.slots);
                        resolveSelection(session, state, turn);
                    }
                    case "consultar_solicitud" -> {
                        var lookup = booking.inspect(session, target);
                        result = json.convertValue(lookup, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    }
                    case "registrar", "reintentar" -> {
                        resolveSelection(session, state, turn);
                        var pending = booking.current(session);
                        var saved = booking.confirm(session, pending.attemptId());
                        result = publicAttempt(saved);
                        success = "registrada".equals(saved.stage());
                        if (success) state.authorizedIncomplete = null;
                    }
                    default -> throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                            "La herramienta solicitada no está disponible.");
                }
            } catch (ResponseStatusException exception) {
                success = false;
                result = Map.of("error", exception.getReason() == null ? "La operación no pudo completarse." : exception.getReason(),
                        "codigo", exception.getStatusCode().value());
            } catch (RuntimeException exception) {
                success = false;
                LOG.error("asistente id={} herramienta={} evento=error_inesperado tipo={}",
                        turn.trace, tool, exception.getClass().getSimpleName());
                result = Map.of("error", "No pude completar la operación. Podés reintentar.", "codigo", 500);
            }
            if (!success) LOG.warn("asistente id={} herramienta={} evento=error_recuperable", turn.trace, tool);
            var call = new Call(id, tool, now(), success, result);
            turn.calls.add(call);
            LOG.info("asistente id={} ronda={} herramienta={} decision=EJECUTADA resultado_id={} exito={}",
                    turn.trace, turn.diagnosticRound, definition.name(), id, success);
            if (definition.hasEffect()) turn.externalRevision++;
            else turn.readResults.put(key, call);
            changed = true;
        }
        return changed;
    }

    private ReadKey readKey(HttpSession session, String tool, String target, int revision) {
        if ("disponibilidad".equals(tool)) target = ""; // GET returns the complete catalogue; it has no date arguments.
        else if ("consultar_solicitud".equals(tool) && target.isBlank()) {
            var attempt = booking.current(session);
            if (attempt != null && !"resumen".equals(attempt.stage())) {
                target = attempt.solicitudId() == null ? attempt.attemptId() : attempt.solicitudId();
            } else {
                var known = booking.knownRequests(session);
                if (!known.isEmpty()) target = known.get(known.size() - 1).get("solicitud_id");
            }
        }
        return new ReadKey(tool, target, revision);
    }

    private List<Map<String, Object>> availableRequests(HttpSession session, State state, TurnWork turn) {
        var choices = new ArrayList<Map<String, Object>>();
        var defaultTarget = java.util.Collections.<String>singletonList(null);
        if (!turn.readResults.containsKey(readKey(session, "disponibilidad", "", turn.externalRevision))) {
            choices.add(Map.of("nombre", "disponibilidad", "solicitudes", defaultTarget));
        }
        var targets = new ArrayList<String>();
        if (!turn.readResults.containsKey(readKey(session, "consultar_solicitud", "", turn.externalRevision))) targets.add(null);
        for (var request : booking.knownRequests(session)) {
            String id = request.get("solicitud_id");
            if (!turn.readResults.containsKey(readKey(session, "consultar_solicitud", id, turn.externalRevision))) targets.add(id);
        }
        if (!targets.isEmpty()) choices.add(Map.of("nombre", "consultar_solicitud", "solicitudes", targets));
        for (String effect : List.of("registrar", "reintentar")) {
            if (effectDenial(session, state, turn, effect, "").isEmpty()) {
                choices.add(Map.of("nombre", effect, "solicitudes", defaultTarget));
            }
        }
        return choices;
    }

    private static String queryKey(String query) {
        return query.strip().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    private static InstitutionalAssistant.Retrieval mergeRetrieval(InstitutionalAssistant.Retrieval current,
                                                                  InstitutionalAssistant.Retrieval additional) {
        if (current == null) return additional;
        var sources = new LinkedHashMap<String, InstitutionalAssistant.Source>();
        for (var source : current.sources()) sources.put(source.id(), source);
        for (var source : additional.sources()) sources.put(source.id(), source);
        return new InstitutionalAssistant.Retrieval(current.context() + "\n\n---\n\n" + additional.context(),
                current.source(), List.copyOf(sources.values()));
    }

    private String effectDenial(HttpSession session, State state, TurnWork turn, String tool, String target) {
        if (turn.effectExecuted) return "Ya se ejecutó una operación con efectos en este turno; no repetirla.";
        var pending = booking.current(session);
        if (pending == null) return "No existe un intento preparado.";
        if (!target.isEmpty() && !target.equals(pending.solicitudId())) return "El identificador no corresponde al intento autorizado.";
        if ("reintentar".equals(tool)) {
            return "indeterminado".equals(pending.stage()) && turn.plan.path("reintento_explicito").asBoolean(false)
                    ? "" : "El reintento exige petición explícita y un envío anterior indeterminado; para verificar usá lectura.";
        }
        boolean eligible = "resumen".equals(pending.stage()) && complete(state.details)
                && state.slots.stream().anyMatch(slot -> slot.id().equals(state.details.horarioId())
                        && InstitutionalCalendar.current().permits(slot))
                && ((state.authorizedIncomplete != null
                        && preservesKnownData(state.authorizedIncomplete, state.details))
                    || (turn.confirmed && turn.previous != null
                        && "resumen".equals(turn.previous.stage())
                        && turn.previous.attemptId().equals(turn.reviewedAtStart)
                        && turn.previous.attemptId().equals(state.reviewedAttempt)
                        && turn.previous.details().equals(state.details)
                        && pending.attemptId().equals(turn.previous.attemptId())))
                && turn.validation.isEmpty() && turn.selectionIssue.isEmpty();
        return eligible ? "" : "Faltan datos, selección institucional válida, resumen previo o conformidad explícita vigente. No se ejecutó el registro.";
    }

    private void resolveSelection(HttpSession session, State state, TurnWork turn) {
        var selection = SlotSelection.resolve(turn.plan.path("seleccion"), turn.message, state.slots,
                ZonedDateTime.now(clock).withZoneSameInstant(ZONE).toLocalDate());
        if (!selection.requested()) return;
        turn.selectionRequested = true;
        if (!selection.id().equals(state.details.horarioId())) {
            invalidateReview(session, state);
            state.details = new BookingService.Details(selection.id(), state.details.nombre(),
                    state.details.correo(), state.details.motivo());
            if (state.authorizedIncomplete != null && !preservesKnownData(state.authorizedIncomplete, state.details)) {
                state.authorizedIncomplete = null;
            }
        }
        turn.selectionIssue = selection.issue();
    }

    private BookingService.View prepareReview(HttpSession session, State state, TurnWork turn) {
        var pending = booking.current(session);
        if (state.active && complete(state.details) && turn.validation.isEmpty() && turn.selectionIssue.isEmpty()
                && (pending == null || "resumen".equals(pending.stage()))) {
            return booking.prepare(session, state.details);
        }
        return pending;
    }

    private void invalidateReview(HttpSession session, State state) {
        booking.discardDraft(session);
        state.reviewedAttempt = null;
    }

    private String input(HttpSession session, State state, TurnWork turn, InstitutionalAssistant.Retrieval retrieved) {
        var input = new LinkedHashMap<String, Object>();
        input.put("ahora_buenos_aires", now());
        input.put("politica_institucional_calendario", InstitutionalCalendar.current().context());
        input.put("clases_herramienta", Map.of("lectura", List.of("disponibilidad", "consultar_solicitud"),
                "efecto", List.of("registrar", "reintentar")));
        input.put("efecto_ejecutado_en_turno", turn.effectExecuted);
        if (turn.plan != null) {
            input.put("solicitudes_habilitadas_en_turno", availableRequests(session, state, turn));
            input.put("registro_habilitado", effectDenial(session, state, turn, "registrar", "").isEmpty());
            input.put("reintento_habilitado", effectDenial(session, state, turn, "reintentar", "").isEmpty());
        }
        input.put("memoria", Map.of("intencion_activa", state.active, "datos", state.details,
                "horarios_ofrecidos", state.slots, "horarios_observados_en", state.slotsObservedAt,
                "vigencia", "Los datos externos de memoria son antecedentes; no verifican el estado actual.",
                "solicitudes_conocidas", booking.knownRequests(session)));
        input.put("historial", state.history);
        input.put("mensaje_actual", turn.message);
        input.put("autorizacion_registro_conservada", state.authorizedIncomplete != null);
        var attempt = booking.current(session);
        input.put("operacion_antecedente", publicAttempt(attempt));
        input.put("confirmacion_pendiente", attempt != null && "resumen".equals(attempt.stage())
                && attempt.attemptId().equals(state.reviewedAttempt));
        input.put("herramientas_turno", turn.calls);
        var notes = new LinkedHashMap<String, Object>(turn.notes);
        if (!turn.validation.isEmpty()) notes.put("aclaraciones_datos", turn.validation);
        if (!turn.selectionIssue.isEmpty()) notes.put("aclaracion_horario", turn.selectionIssue);
        if (turn.selectionRequested) notes.put("seleccion_validada", Map.of("horario_id", state.details.horarioId(),
                "coincidencia_exacta", turn.selectionIssue.isEmpty() && !state.details.horarioId().isEmpty()));
        input.put("hechos_del_backend", notes);
        if (retrieved != null) {
            input.put("contexto_institucional", retrieved.context());
            input.put("fuentes_institucionales_disponibles", retrieved.sources());
        }
        try {
            return json.writeValueAsString(input);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo construir el contexto del asistente.", exception);
        }
    }

    private static boolean validInstitutionalSources(JsonNode draft, InstitutionalAssistant.Retrieval retrieved) {
        if (retrieved == null || draft.path("fuentes_institucionales").isEmpty()) return false;
        for (var id : draft.path("fuentes_institucionales")) {
            if (retrieved.sources().stream().noneMatch(source -> source.id().equals(id.asText()))) return false;
        }
        return true;
    }

    private static InstitutionalAssistant.Source usedInstitutionalSource(JsonNode draft, InstitutionalAssistant.Retrieval retrieved) {
        if (retrieved == null) return null;
        for (var id : draft.path("fuentes_institucionales")) {
            for (var source : retrieved.sources()) if (source.id().equals(id.asText())) return source;
        }
        return retrieved.source();
    }

    private static boolean validSources(JsonNode draft, List<Call> calls) {
        var sources = draft.path("fuentes_dinamicas");
        if (draft.path("usa_datos_dinamicos").asBoolean() && sources.isEmpty()) return false;
        for (JsonNode source : sources) {
            if (calls.stream().noneMatch(call -> call.id().equals(source.asText()))) return false;
        }
        return true;
    }

    private String now() {
        return ZonedDateTime.now(clock).withZoneSameInstant(ZONE).toOffsetDateTime().toString();
    }

    private static Map<String, Object> publicAttempt(BookingService.View view) {
        if (view == null) return Map.of();
        return Map.of("resultado", view.stage(), "datos_del_intento", view.details(),
                "horario", view.horario(), "estado", view.estado() == null ? "" : view.estado(),
                "solicitud_id", view.solicitudId() == null ? "" : view.solicitudId(),
                "mensaje_resultado", view.message(), "comprobante_disponible", hasReceipt(view));
    }

    private static BookingService.Details update(BookingService.Details previous, JsonNode plan,
                                                 List<String> validation) {
        String name = field(plan, "nombre", previous.nombre());
        String email = field(plan, "correo", previous.correo());
        String reason = field(plan, "motivo", previous.motivo());
        if (name.length() > 120) {
            validation.add("El nombre admite hasta 120 caracteres.");
            name = previous.nombre();
        }
        if (!email.isEmpty() && (email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+[.][^\\s@]+$"))) {
            validation.add("El correo aportado es inválido.");
            email = previous.correo();
        }
        if (reason.length() > 500) {
            validation.add("El motivo admite hasta 500 caracteres.");
            reason = previous.motivo();
        }
        String slot = previous.horarioId();
        for (var cleared : plan.path("vaciar")) {
            if ("horario_id".equals(cleared.asText())) slot = "";
        }
        return new BookingService.Details(slot, name, email, reason);
    }

    private static String field(JsonNode plan, String key, String previous) {
        var value = plan.path("datos").path(key);
        if (value.isTextual() && !value.asText().isBlank()) return value.asText().strip();
        for (var cleared : plan.path("vaciar")) {
            if (key.equals(cleared.asText())) return "";
        }
        return previous;
    }

    private static boolean institutionalQuery(String query, BookingService.Details details) {
        String text = query.toLowerCase(java.util.Locale.ROOT);
        return query.length() <= 200 && !text.contains("@")
                && (details.nombre().isBlank() || !java.util.regex.Pattern.compile("\\b" + java.util.regex.Pattern.quote(details.nombre().toLowerCase(java.util.Locale.ROOT)) + "\\b").matcher(text).find())
                && !text.matches(".*\\b(dolor|duele|fiebre|sangrado|hinchazon|hinchazón)\\b.*");
    }

    private static boolean complete(BookingService.Details details) {
        return !details.nombre().isBlank() && !details.correo().isBlank()
                && !details.motivo().isBlank() && !details.horarioId().isBlank();
    }

    private static void rememberTurn(State state, String role, String content) {
        state.history.add(new Turn(role, content));
        while (state.history.size() > 40) state.history.remove(0);
    }

    private static State state(HttpSession session) {
        session.setMaxInactiveInterval(30 * 60);
        State state = (State) session.getAttribute(SESSION_KEY);
        if (state == null) {
            state = new State();
            session.setAttribute(SESSION_KEY, state);
        }
        return state;
    }

    record Turn(String role, String content) {}
    record Context(boolean active, String stage, BookingService.Details details,
                   List<AvailabilityClient.Slot> horarios, List<Turn> history) {}
    record Evidence(String id, String herramienta, String tipo, String consultadaEn, boolean exito) {}
    record Call(String id, String herramienta, String consultadaEn, boolean exito, Map<String, Object> resultado) {
        Evidence evidence() { return new Evidence(id, herramienta, AssistantTool.named(herramienta).hasEffect() ? "efecto" : "lectura", consultadaEn, exito); }
    }

    private record ReadKey(String tool, String target, int revision) {}

    private static class TurnWork {
        private final String trace = UUID.randomUUID().toString();
        private int diagnosticRound;
        private boolean initialized;
        private boolean historyWritten;
        private InstitutionalAssistant.Retrieval retrieved;
        private InstitutionalAssistant.ChatReply completed;
        private final String message;
        private final BookingService.View previous;
        private final String reviewedAtStart;
        private boolean confirmed;
        private boolean effectExecuted;
        private boolean registrationRecoveryAttempted;
        private boolean selectionRequested;
        private boolean groundingRepairAttempted;
        private int externalRevision;
        private final Map<ReadKey, Call> readResults = new LinkedHashMap<>();
        private final Map<String, InstitutionalAssistant.Retrieval> institutionalResults = new LinkedHashMap<>();
        private JsonNode plan;
        private String selectionIssue = "";
        private final List<String> validation = new ArrayList<>();
        private final List<Call> calls = new ArrayList<>();
        private final List<String> routes = new ArrayList<>();
        private final Map<String, Object> notes = new LinkedHashMap<>();

        TurnWork(String message, BookingService.View previous, String reviewedAtStart) {
            this.message = message;
            this.previous = previous;
            this.reviewedAtStart = reviewedAtStart;
        }
    }

    private static class State {
        private String requestId;
        private TurnWork turn;
        private boolean active;
        private String operation = "conversando";
        private String reviewedAttempt;
        private String slotsObservedAt = "";
        private BookingService.Details authorizedIncomplete;
        private BookingService.Details details = new BookingService.Details("", "", "", "");
        private List<AvailabilityClient.Slot> slots = List.of();
        private final List<Turn> history = new ArrayList<>();

        Context context() { return new Context(active, operation, details, slots, List.copyOf(history)); }
    }
}
