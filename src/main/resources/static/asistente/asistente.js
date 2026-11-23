const form = document.querySelector("#chat");
const input = document.querySelector("#message");
const conversation = document.querySelector("#conversation");
const review = document.querySelector("#review");
const thinking = document.querySelector("#thinking");
const sendButton = document.querySelector("#send");
const closeButton = document.querySelector("#close-session");
const downloadButton = document.querySelector("#download-receipt");
const emptyTemplate = document.querySelector("#empty-conversation");
let currentReceipt = null;
let busy = false;
const retryButton = document.querySelector("#retry");
let retryAction = null;
let retryEntry = null;

function clearRetry() {
    retryAction = null;
    retryEntry = null;
    retryButton.hidden = true;
}

function offerRetry(action, entry) {
    retryAction = action;
    retryEntry = entry;
    retryButton.hidden = false;
}

function reportClientError(operation) {
    // Best effort: a disconnected browser cannot deliver a server log.
    call("client-error", { operation }).catch(() => {});
}

retryButton.addEventListener("click", () => {
    if (busy || !retryAction) return;
    const action = retryAction;
    const entry = retryEntry;
    retryAction = null;
    retryEntry = null;
    action(entry);
});

function showEmptyConversation() {
    conversation.replaceChildren(emptyTemplate.content.cloneNode(true));
}

function appendMessage(label, text, retrieved, isError = false, tools = []) {
    conversation.querySelector(".empty-conversation")?.remove();
    const entry = document.createElement("article");
    const author = document.createElement("strong");
    const content = document.createElement("p");
    author.textContent = label;
    content.textContent = text;
    entry.append(author, content);
    entry.classList.toggle("error", isError);
    entry.classList.toggle("user-message", label === "Vos");
    if (retrieved || tools.length) {
        const sources = document.createElement("details");
        sources.className = "message-sources";
        const title = document.createElement("summary");
        title.textContent = "Fuentes y consultas";
        sources.append(title);
        if (retrieved) {
            const source = document.createElement("small");
            source.textContent = "Fragmento recuperado: " + retrieved.id + " · v" + retrieved.version;
            sources.append(source);
        }
        for (const tool of tools) {
            const source = document.createElement("small");
            source.textContent = tool.herramienta + " · " + tool.consultadaEn
                + (tool.exito ? " · consulta completada" : " · operación no completada");
            sources.append(source);
        }
        entry.append(sources);
    }
    conversation.append(entry);
    conversation.scrollTop = conversation.scrollHeight;
    return entry;
}

async function call(path, body) {
    const options = { credentials: "same-origin", cache: "no-store" };
    if (body !== undefined) {
        options.method = "POST";
        options.headers = { "Content-Type": "application/json" };
        options.body = JSON.stringify(body);
    }
    const response = await fetch("../api/v1/public/assistant/" + path, options);
    const text = await response.text();
    let result;
    try { result = text ? JSON.parse(text) : null; }
    catch (_) { throw new Error("El servidor devolvió una respuesta inválida. Podés reintentar."); }
    if (!response.ok) {
        const error = new Error(result?.error || "El servidor no pudo procesar la consulta.");
        error.booking = result?.booking;
        throw error;
    }
    return result;
}

function showAttempt(attempt) {
    // A prepared summary or an uncertain send is not proof of registration.
    const registered = attempt?.stage === "registrada" && attempt.solicitudId && attempt.comprobante && attempt.details;
    currentReceipt = registered ? attempt : null;
    review.hidden = !registered;
    downloadButton.disabled = !registered;
    const fields = {
        "receipt-slot": registered ? attempt.horario : "",
        "receipt-name": registered ? attempt.details.nombre : "",
        "receipt-email": registered ? attempt.details.correo : "",
        "receipt-reason": registered ? attempt.details.motivo : "",
        "receipt-id": registered ? attempt.solicitudId : "",
        "receipt-token": registered ? attempt.comprobante : "",
        "booking-status": registered ? attempt.message : ""
    };
    for (const [id, value] of Object.entries(fields)) document.getElementById(id).textContent = value || "";
    const state = document.querySelector("#receipt-state");
    state.textContent = registered ? ({ pendiente: "Pendiente de recepción", confirmada: "Confirmada", cancelada: "Cancelada" }[attempt.estado] || attempt.estado) : "";
    state.dataset.state = registered ? attempt.estado : "";
    if (!registered) document.querySelector(".receipt-code").open = false;
}

function setBusy(value, assistantThinking = false) {
    busy = value;
    sendButton.disabled = value;
    closeButton.disabled = value;
    retryButton.disabled = value;
    retryButton.textContent = value && retryAction === null ? "Reintentando…" : "Reintentar ↻";
    thinking.hidden = !(value && assistantThinking);
    conversation.setAttribute("aria-busy", String(value));
}

async function sendChat(message, requestId = crypto.randomUUID(), retry = false, oldEntry = null) {
    if (!message || busy) return;
    if (!retry) {
        clearRetry();
        appendMessage("Vos", message);
        input.value = "";
    }
    setBusy(true, true);
    try {
        const result = await call("chat", { message, requestId, retry });
        oldEntry?.remove();
        clearRetry();
        const tools = result.herramientas || [];
        const entry = appendMessage("Asistente", result.answer, result.retrieved, false, tools);
        showAttempt(result.booking);
        if (tools.some(tool => !tool.exito) || result.booking?.stage === "indeterminado") {
            offerRetry(previous => sendChat(message, requestId, true, previous), entry);
        }
    } catch (error) {
        if (error.booking) showAttempt(error.booking);
        oldEntry?.remove();
        const entry = appendMessage("Error", error.message || "No se pudo obtener una respuesta del servidor.", null, true);
        offerRetry(previous => sendChat(message, requestId, true, previous), entry);
        reportClientError("chat");
    } finally {
        setBusy(false);
        input.focus({ preventScroll: true });
    }
}

form.addEventListener("submit", event => {
    event.preventDefault();
    sendChat(input.value.trim());
});

downloadButton.addEventListener("click", () => {
    if (!currentReceipt) return;
    const receipt = currentReceipt;
    const text = [
        "CONSULTORIO LOS TILOS · DEMO ACADÉMICA",
        "Comprobante de solicitud registrada", "",
        "Solicitud: " + receipt.solicitudId,
        "Estado: " + receipt.estado,
        "Horario solicitado (Buenos Aires): " + receipt.horario,
        "Nombre: " + receipt.details.nombre,
        "Correo: " + receipt.details.correo,
        "Motivo general: " + receipt.details.motivo, "",
        receipt.message, "",
        "Comprobante: " + receipt.comprobante, "",
        "Guardá este archivo en un lugar privado."
    ].join("\r\n");
    const url = URL.createObjectURL(new Blob(["\uFEFF", text], { type: "text/plain;charset=utf-8" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = "comprobante-los-tilos-" + String(receipt.solicitudId).replace(/[^a-zA-Z0-9-]/g, "") + ".txt";
    link.hidden = true;
    document.body.append(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
});

async function closeSession(oldEntry = null) {
    if (busy) return;
    setBusy(true);
    try {
        await call("booking/close", {});
        clearRetry();
        showAttempt(null);
        showEmptyConversation();
        input.value = "";
    } catch (error) {
        oldEntry?.remove();
        const entry = appendMessage("Error", error.message || "No se pudo finalizar la sesión.", null, true);
        offerRetry(closeSession, entry);
        reportClientError("close");
    } finally { setBusy(false); }
}
closeButton.addEventListener("click", () => {
    if (busy) return;
    if (window.confirm("¿Finalizar la sesión? Se borrarán los datos de esta página. Descargá el comprobante antes de continuar si ya registraste una solicitud.")) closeSession();
});

async function restoreSession(oldEntry = null) {
    if (busy) return;
    setBusy(true);
    try {
        const attempt = await call("booking/current");
        const context = await call("booking/conversation");
        oldEntry?.remove();
        clearRetry();
        showAttempt(attempt);
        showEmptyConversation();
        for (const turn of context?.history || []) appendMessage(turn.role === "user" ? "Vos" : "Asistente", turn.content);
    } catch (error) {
        oldEntry?.remove();
        const entry = appendMessage("Error", "No pude recuperar la sesión anterior. Podés reintentar.", null, true);
        offerRetry(restoreSession, entry);
        reportClientError("restore");
    } finally { setBusy(false); }
}
showEmptyConversation();
showAttempt(null);
restoreSession();
