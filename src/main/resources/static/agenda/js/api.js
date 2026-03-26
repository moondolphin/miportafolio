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
        window.location.href = '/agenda/login.html';
        return false;
    }
    return true;
}

function requireAdmin() {
    if (!requireAuth()) return false;
    if (getRole() !== 'ADMIN') {
        window.location.href = '/agenda/pendiente.html';
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

// ── Fetch wrappers ─────────────────────────────────────────

function headers() {
    const token = getToken();
    const h = { 'Content-Type': 'application/json' };
    if (token) h['Authorization'] = 'Bearer ' + token;
    return h;
}

async function handleResponse(response) {
    if (response.status === 401 || response.status === 403) {
        removeToken();
        window.location.href = '/agenda/login.html';
        throw new Error('No autorizado');
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
    if (response.status === 401 || response.status === 403) {
        removeToken();
        window.location.href = '/agenda/login.html';
        throw new Error('No autorizado');
    }
    return null;
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
    return new Date().toISOString().split('T')[0];
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
