/* =========================================================
   api.js — utilidades compartidas de Agenda Mislun
   ========================================================= */

const TOKEN_KEY = 'agenda_token';
const BASE_URL  = '/api/v1';

// ── Token helpers ──────────────────────────────────────────

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
}

function parseJwtPayload(token) {
    try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(atob(base64));
    } catch {
        return null;
    }
}

function isTokenValido() {
    const token = getToken();
    if (!token) return false;
    const payload = parseJwtPayload(token);
    if (!payload) return false;
    return payload.exp * 1000 > Date.now();
}

function getRole() {
    const payload = parseJwtPayload(getToken());
    return payload ? payload.role : null;
}

function getUserId() {
    const payload = parseJwtPayload(getToken());
    return payload ? payload.userId : null;
}

function getUsername() {
    const payload = parseJwtPayload(getToken());
    return payload ? payload.sub : null;
}

// ── Auth guards ────────────────────────────────────────────

function requireAuth() {
    if (!isTokenValido()) {
        mostrarPopupYRedirigir(
            'Sesión requerida',
            'Necesitás iniciar sesión para acceder a esta página.',
            '/agenda/login.html',
            true
        );
        return false;
    }
    return true;
}

function requireAdmin() {
    if (!requireAuth()) return false;
    if (getRole() !== 'ADMIN') {
        mostrarPopupYRedirigir(
            'Acceso restringido',
            'Esta sección es exclusiva del administrador.',
            '/agenda/dashboard.html',
            false
        );
        return false;
    }
    return true;
}

// Si ya está logueado, redirigir al dashboard (para usar en login.html)
function redirectIfAuthenticated() {
    if (isTokenValido()) {
        window.location.href = '/agenda/dashboard.html';
    }
}

// ── Logout ─────────────────────────────────────────────────

function logout() {
    removeToken();
    window.location.href = '/agenda/login.html';
}

// ── Popup modal bloqueante ─────────────────────────────────
// Muestra un popup con un botón "Aceptar". Hasta que el usuario no
// confirme, no se ejecuta el redirect. Si borrarToken=true, además
// limpia la sesión antes de redirigir (caso 401: sesión inválida).

function mostrarPopupYRedirigir(titulo, mensaje, urlDestino, borrarToken) {
    if (document.getElementById('agenda-popup-overlay')) return; // ya hay uno

    const overlay = document.createElement('div');
    overlay.id = 'agenda-popup-overlay';
    overlay.style.cssText = `
        position: fixed; inset: 0; background: rgba(0,0,0,0.5);
        display: flex; align-items: center; justify-content: center;
        z-index: 99999; font-family: 'Inter', sans-serif;
    `;

    const popup = document.createElement('div');
    popup.style.cssText = `
        background: #fff; border-radius: 16px; padding: 28px;
        max-width: 380px; width: 90%; box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        text-align: center; border: 1px solid #e9d5ff;
    `;

    const tituloEl = document.createElement('h2');
    tituloEl.textContent = titulo;
    tituloEl.style.cssText = `
        font-family: 'Cormorant Garamond', serif; color: #3b1f6a;
        font-size: 22px; margin: 0 0 12px 0;
    `;

    const mensajeEl = document.createElement('p');
    mensajeEl.textContent = mensaje;
    mensajeEl.style.cssText = `color: #5a4a7a; font-size: 14px; margin: 0 0 20px 0; line-height: 1.5;`;

    const botonEl = document.createElement('button');
    botonEl.textContent = 'Aceptar';
    botonEl.style.cssText = `
        background: #7c3aed; color: white; border: none;
        padding: 10px 28px; border-radius: 10px; font-size: 14px;
        font-weight: 500; cursor: pointer;
    `;
    botonEl.onclick = () => {
        if (borrarToken) removeToken();
        document.body.removeChild(overlay);
        if (urlDestino) window.location.href = urlDestino;
    };

    popup.appendChild(tituloEl);
    popup.appendChild(mensajeEl);
    popup.appendChild(botonEl);
    overlay.appendChild(popup);
    document.body.appendChild(overlay);
    setTimeout(() => botonEl.focus(), 50);
}

// ── Fetch wrappers ─────────────────────────────────────────

function headers() {
    const token = getToken();
    const h = { 'Content-Type': 'application/json' };
    if (token) h['Authorization'] = 'Bearer ' + token;
    return h;
}

async function handleResponse(response) {
    if (response.status === 401) {
        // Sesión inválida o expirada → borrar token y mandar al login
        mostrarPopupYRedirigir(
            'Sesión expirada',
            'Tu sesión expiró. Por favor iniciá sesión nuevamente.',
            '/agenda/login.html',
            true
        );
        throw new Error('Sesión expirada');
    }
    if (response.status === 403) {
        // Permiso insuficiente → mantener sesión, avisar al usuario
        mostrarPopupYRedirigir(
            'Sin permisos',
            'No tenés permiso para realizar esta acción.',
            null,
            false
        );
        throw new Error('Sin permisos');
    }
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `Error ${response.status}`);
    }
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }
    return null;
}

async function apiGet(path) {
    const response = await fetch(BASE_URL + path, {
        method: 'GET',
        headers: headers()
    });
    return handleResponse(response);
}

async function apiPost(path, body) {
    const response = await fetch(BASE_URL + path, {
        method: 'POST',
        headers: headers(),
        body: JSON.stringify(body)
    });
    return handleResponse(response);
}

async function apiPut(path, body) {
    const response = await fetch(BASE_URL + path, {
        method: 'PUT',
        headers: headers(),
        body: JSON.stringify(body)
    });
    return handleResponse(response);
}

async function apiDelete(path) {
    const response = await fetch(BASE_URL + path, {
        method: 'DELETE',
        headers: headers()
    });
    return handleResponse(response);
}

// ── Utilidades de UI ───────────────────────────────────────

function mostrarError(elementId, mensaje) {
    const el = document.getElementById(elementId);
    if (el) { el.textContent = mensaje; el.classList.remove('hidden'); }
}

function ocultarError(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.classList.add('hidden');
}

function formatearFecha(fechaStr) {
    if (!fechaStr) return '—';
    const [y, m, d] = fechaStr.split('-');
    return `${d}/${m}/${y}`;
}

function fechaHoy() {
    const d = new Date();
    return d.getFullYear() + '-'
        + String(d.getMonth() + 1).padStart(2, '0') + '-'
        + String(d.getDate()).padStart(2, '0');
}

const PRIORIDAD_BADGE = {
    ALTA:   'bg-red-100 text-red-700',
    MEDIA:  'bg-yellow-100 text-yellow-700',
    BAJA:   'bg-green-100 text-green-700'
};

const ESTADO_BADGE = {
    PENDIENTE:    'bg-purple-100 text-purple-700',
    EN_PROGRESO:  'bg-blue-100 text-blue-700',
    COMPLETADA:   'bg-green-100 text-green-700',
    CANCELADA:    'bg-gray-100 text-gray-500'
};

const ANIMO_EMOJI = {
    MUY_BIEN: '✨',
    BIEN:     '🌸',
    NEUTRO:   '🌙',
    MAL:      '🌧',
    MUY_MAL:  '🌑'
};
