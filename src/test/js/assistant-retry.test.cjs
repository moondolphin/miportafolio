const test = require("node:test");
const assert = require("node:assert/strict");
const vm = require("node:vm");
const fs = require("node:fs");
const path = require("node:path");

class Element {
    constructor() { this.children = []; this.listeners = {}; this.dataset = {}; this.hidden = false; this.disabled = false; this.value = ""; this.textContent = ""; this.classList = { toggle() {} }; }
    append(...items) { for (const item of items) { item.parent = this; this.children.push(item); } }
    replaceChildren(...items) { this.children = []; this.append(...items); }
    remove() { if (this.parent) this.parent.children = this.parent.children.filter(x => x !== this); }
    querySelector() { return null; }
    setAttribute() {}
    focus() {}
    addEventListener(event, callback) { this.listeners[event] = callback; }
    click() { if (!this.disabled) this.listeners.click?.({}); }
}
const tick = () => new Promise(resolve => setImmediate(resolve));
async function page(responses) {
    const elements = new Map();
    const get = id => { if (!elements.has(id)) elements.set(id, new Element()); return elements.get(id); };
    get("#empty-conversation").content = { cloneNode: () => new Element() };
    const requests = [];
    const fetch = async (url, options) => {
        if (!url.endsWith("/chat")) return { ok: true, text: async () => "null" };
        requests.push(JSON.parse(options.body));
        const response = responses.shift();
        if (typeof response === "function") return response();
        return { ok: response.ok !== false, text: async () => JSON.stringify(response.body) };
    };
    const context = vm.createContext({ document: { querySelector: get, getElementById: id => get("#" + id), createElement: () => new Element() },
        fetch, crypto: require("node:crypto").webcrypto, console, setTimeout, Blob, URL });
    vm.runInContext(fs.readFileSync(path.join(__dirname, "../../main/resources/static/asistente/asistente.js"), "utf8"), context);
    await tick();
    return { get, requests, send: message => { get("#message").value = message; get("#chat").listeners.submit({ preventDefault() {} }); } };
}
const good = { body: { answer: "Respuesta recuperada", herramientas: [], booking: null } };

test("OpenAI error shows retry; double click executes once and keeps one user message", async () => {
    let finish;
    const pending = () => new Promise(resolve => { finish = () => resolve({ ok: true, text: async () => JSON.stringify(good.body) }); });
    const p = await page([{ ok: false, body: { error: "OpenAI no disponible" } }, pending]);
    p.send("Hola"); await tick();
    assert.equal(p.get("#retry").hidden, false);
    p.get("#retry").click(); p.get("#retry").click();
    assert.equal(p.requests.length, 2);
    assert.equal(p.get("#retry").disabled, true);
    assert.equal(p.get("#retry").hidden, false);
    assert.equal(p.get("#retry").textContent, "Reintentando…");
    assert.equal(p.get("#thinking").hidden, false);
    finish(); await tick();
    assert.equal(p.requests[0].requestId, p.requests[1].requestId);
    assert.equal(p.requests[1].message, "Hola");
    assert.equal(p.requests[1].retry, true);
    assert.equal(p.get("#retry").hidden, true);
    const articles = p.get("#conversation").children;
    assert.equal(articles.filter(x => x.children[0]?.textContent === "Vos").length, 1);
    assert.equal(articles.some(x => x.children[1]?.textContent === "Respuesta recuperada"), true);
});

test("n8n tool failure on HTTP 200 and network failure both keep retry available", async () => {
    const p = await page([{ body: { answer: "No pude consultar n8n", herramientas: [{ herramienta: "disponibilidad", exito: false }] } },
        () => Promise.reject(new TypeError("Network error")), good]);
    p.send("¿Hay horarios?"); await tick();
    assert.equal(p.get("#retry").hidden, false);
    p.get("#retry").click(); await tick();
    assert.equal(p.get("#retry").hidden, false);
    assert.equal(p.get("#retry").disabled, false);
    p.get("#retry").click(); await tick();
    assert.equal(p.requests.length, 3);
    assert.equal(new Set(p.requests.map(r => r.requestId)).size, 1);
    assert.equal(p.get("#retry").hidden, true);
});
