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
    const result = text ? JSON.parse(text) : null;
    if (!response.ok) throw new Error(result?.error || "El servidor no pudo procesar la consulta.");
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
    thinking.hidden = !(value && assistantThinking);
    conversation.setAttribute("aria-busy", String(value));
}

async function sendChat(message) {
    if (!message || busy) return;
    appendMessage("Vos", message);
    input.value = "";
    setBusy(true, true);
    try {
        const result = await call("chat", { message });
        appendMessage("Asistente", result.answer, result.retrieved, false, result.herramientas || []);
        showAttempt(result.booking);
    } catch (error) {
        appendMessage("Error", error.message || "No se pudo obtener una respuesta del servidor.", null, true);
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

closeButton.addEventListener("click", async () => {
    if (busy) return;
    if (!window.confirm("¿Finalizar la sesión? Se borrarán los datos de esta página. Descargá el comprobante antes de continuar si ya registraste una solicitud.")) return;
    setBusy(true);
    try {
        await call("booking/close", {});
        showAttempt(null);
        showEmptyConversation();
        input.value = "";
    } catch (error) {
        appendMessage("Error", error.message || "No se pudo finalizar la sesión.", null, true);
    } finally {
        setBusy(false);
    }
});

showEmptyConversation();
showAttempt(null);
setBusy(true);
call("booking/current").then(async attempt => {
    showAttempt(attempt);
    const context = await call("booking/conversation");
    for (const turn of context?.history || []) appendMessage(turn.role === "user" ? "Vos" : "Asistente", turn.content);
})
    .catch(() => appendMessage("Aviso", "No pude recuperar la sesión anterior. Volvé a consultar disponibilidad."))
    .finally(() => setBusy(false));
