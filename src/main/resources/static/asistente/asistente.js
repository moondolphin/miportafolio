const form = document.querySelector("#chat");
const input = document.querySelector("#message");
const conversation = document.querySelector("#conversation");
const bookingForm = document.querySelector("#booking");
const slotSelect = document.querySelector("#slot");
const review = document.querySelector("#review");
const confirmButton = document.querySelector("#confirm");
let currentAttempt = null;
let busy = false;

function appendMessage(label, text, retrieved, isError = false, tools = []) {
    const entry = document.createElement("article");
    const author = document.createElement("strong");
    author.textContent = label;
    entry.append(author, document.createTextNode("\n" + text));
    entry.classList.toggle("error", isError);
    if (retrieved) {
        const source = document.createElement("small");
        source.textContent = "Fragmento recuperado: " + retrieved.id + " · v" + retrieved.version;
        entry.append(source);
    }
    for (const tool of tools) {
        const source = document.createElement("small");
        source.textContent = tool.herramienta + " · " + tool.consultadaEn
            + (tool.exito ? " · consulta completada" : " · operación no completada");
        entry.append(source);
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
    if (!response.ok) {
        throw new Error(result?.error || "El servidor no pudo procesar la consulta.");
    }
    return result;
}

function showAttempt(attempt) {
    currentAttempt = attempt;
    review.hidden = !attempt;
    if (!attempt) {
        document.querySelector("#summary").textContent = "";
        document.querySelector("#booking-status").textContent = "";
        document.querySelector("#receipt").textContent = "";
        confirmButton.hidden = true;
        return;
    }

    const data = attempt.details;
    document.querySelector("#name").value = data.nombre;
    document.querySelector("#email").value = data.correo;
    document.querySelector("#reason").value = data.motivo;
    slotSelect.value = data.horarioId;
    document.querySelector("#summary").textContent =
        attempt.horario + "\n" + data.nombre + "\n" + data.correo + "\nMotivo: " + data.motivo;
    document.querySelector("#booking-status").textContent = attempt.message;
    document.querySelector("#receipt").textContent = attempt.comprobante
        ? "Solicitud: " + attempt.solicitudId + "\nEstado: " + attempt.estado
          + "\nComprobante: " + attempt.comprobante
        : "";
    confirmButton.hidden = !["resumen", "indeterminado"].includes(attempt.stage);
    confirmButton.textContent = attempt.stage === "indeterminado"
        ? "Verificar o reintentar" : "Confirmar y enviar solicitud";
    bookingForm.hidden = slotSelect.options.length === 0
        || (attempt.stage !== "resumen" && attempt.stage !== "rechazada");
    review.scrollIntoView({ block: "nearest", behavior: "smooth" });
}

function showConversation(context) {
    if (!context) return;
    slotSelect.replaceChildren();
    for (const slot of context.horarios) {
        const option = document.createElement("option");
        option.value = slot.id;
        option.textContent = slot.label;
        slotSelect.append(option);
    }
    slotSelect.value = context.details.horarioId;
    document.querySelector("#name").value = context.details.nombre;
    document.querySelector("#email").value = context.details.correo;
    document.querySelector("#reason").value = context.details.motivo;
    bookingForm.hidden = !context.active || context.horarios.length === 0
        || context.stage === "indeterminado";
}

function setBusy(value) {
    busy = value;
    document.querySelectorAll("button").forEach(button => { button.disabled = value; });
}

async function sendChat(message) {
    if (!message || busy) return;
    appendMessage("Vos", message);
    input.value = "";
    setBusy(true);
    try {
        const result = await call("chat", { message });
        appendMessage("Asistente", result.answer, result.retrieved, false, result.herramientas || []);
        if (Array.isArray(result.horarios)) {
            slotSelect.replaceChildren();
            for (const slot of result.horarios) {
                const option = document.createElement("option");
                option.value = slot.id;
                option.textContent = slot.label;
                slotSelect.append(option);
            }
            bookingForm.hidden = result.horarios.length === 0
                || currentAttempt?.stage === "indeterminado";
        }
        if (result.conversation) {
            showAttempt(result.booking);
            showConversation(result.conversation);
        }
    } catch (error) {
        appendMessage("Error", error.message || "No se pudo obtener una respuesta del servidor.", null, true);
    } finally {
        setBusy(false);
    }
}

form.addEventListener("submit", async event => {
    event.preventDefault();
    await sendChat(input.value.trim());
});

bookingForm.addEventListener("submit", async event => {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    try {
        const attempt = await call("booking/prepare", {
            horarioId: slotSelect.value,
            nombre: document.querySelector("#name").value,
            correo: document.querySelector("#email").value,
            motivo: document.querySelector("#reason").value
        });
        showAttempt(attempt);
    } catch (error) {
        appendMessage("Error", error.message, null, true);
    } finally {
        setBusy(false);
    }
});

confirmButton.addEventListener("click", async () => {
    if (!currentAttempt || busy) return;
    await sendChat(currentAttempt.stage === "indeterminado"
        ? "Verificá o reintentá el envío anterior con la misma clave."
        : "Confirmo que envíes la solicitud con los datos del resumen.");
});

document.querySelector("#close-session").addEventListener("click", async () => {
    if (busy) return;
    if (!window.confirm("¿Finalizar la sesión? Se borrarán los datos y el comprobante de esta página. Guardá el comprobante antes de continuar.")) return;
    setBusy(true);
    try {
        await call("booking/close", {});
        showAttempt(null);
        bookingForm.reset();
        bookingForm.hidden = true;
        conversation.replaceChildren();
        appendMessage("Asistente", "Sesión finalizada. Podés iniciar una consulta nueva.");
    } catch (error) {
        appendMessage("Error", error.message, null, true);
    } finally {
        setBusy(false);
    }
});

setBusy(true);
call("booking/current").then(async attempt => {
    showAttempt(attempt);
    const context = await call("booking/conversation");
    showConversation(context);
    for (const turn of context?.history || []) {
        appendMessage(turn.role === "user" ? "Vos" : "Asistente", turn.content);
    }
})
    .catch(() => appendMessage("Aviso", "No pude recuperar la sesión anterior. Volvé a consultar disponibilidad."))
    .finally(() => setBusy(false));
