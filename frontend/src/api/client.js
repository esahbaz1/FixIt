export const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

// ─── Token Storage ─────────────────────────────────────────────────────────
// Access token čuvan u memoriji (ne localStorage) radi XSS zaštite.
// Refresh token čuva se u sessionStorage kako bi preživio refresh stranice (UX-01 / CQ-04).
let _accessToken = null;
let _refreshPromise = null;
let _korisnikUloga = null;
let _korisnikId = null;

const REFRESH_TOKEN_KEY = "fixit_refresh_token";
const USER_KEY = "fixit_user";

export function setTokens(access, refresh) {
  _accessToken = access;
  if (refresh !== undefined) {
    sessionStorage.setItem(REFRESH_TOKEN_KEY, refresh);
  }
}

export function setKorisnikKontekst(uloga, id) {
  _korisnikUloga = uloga;
  _korisnikId = id;
}

export function clearTokens() {
  _accessToken = null;
  _korisnikUloga = null;
  _korisnikId = null;
  sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
}

export function getAccessToken() { return _accessToken; }

// CQ-04 / UX-01: Pomoćne funkcije za perzistenciju korisničkih podataka u sessionStorage
export function saveUserToStorage(userData) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(userData));
}

export function loadUserFromStorage() {
  try {
    const raw = sessionStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function loadRefreshTokenFromStorage() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY);
}

// ─── Auto-refresh mehanizam ─────────────────────────────────────────────────
async function doRefresh() {
  const refreshToken = sessionStorage.getItem(REFRESH_TOKEN_KEY);
  if (!refreshToken) throw new Error("No refresh token");
  const res = await fetch(`${API_BASE}/api/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!res.ok) throw new Error("Refresh failed");
  const data = await res.json();
  _accessToken = data.token;
  if (data.refreshToken) {
    sessionStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
  }
  return data.token;
}

async function refreshAccessToken() {
  if (!_refreshPromise) {
    _refreshPromise = doRefresh().finally(() => { _refreshPromise = null; });
  }
  return _refreshPromise;
}

// ─── HTTP status → korisnička poruka greške (UX-02) ────────────────────────
export function friendlyError(status, fallbackMessage, context) {
  if (status === 500 || status === 503) {
    return "Sistem je trenutno nedostupan. Pokušajte ponovo za nekoliko minuta.";
  }
  if (status === 401) {
    // Kontekst "login" → prikaži poruku o pogrešnoj lozinci umjesto generičke
    if (context === "login") {
      return "Pogrešna email adresa ili lozinka. Provjerite podatke i pokušajte ponovo.";
    }
    return "Niste autorizovani. Prijavite se ponovo.";
  }
  if (status === 403) {
    return "Nemate dozvolu za ovu akciju.";
  }
  if (status === 404) {
    return "Pogrešan unos podataka.";
  }
  return fallbackMessage || `Greška (HTTP ${status})`;
}

// ─── Glavni API klijent ────────────────────────────────────────────────────
export async function apiCall(path, options = {}, explicitToken = null) {
  const token = explicitToken ?? _accessToken;
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  if (_korisnikUloga) headers["X-Korisnik-Uloga"] = _korisnikUloga;
  if (_korisnikId)    headers["X-Korisnik-Id"]    = String(_korisnikId);

  let res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  // 401 → pokušaj refresh + retry
  if (res.status === 401 && !explicitToken && sessionStorage.getItem(REFRESH_TOKEN_KEY)) {
    try {
      const newToken = await refreshAccessToken();
      headers["Authorization"] = `Bearer ${newToken}`;
      res = await fetch(`${API_BASE}${path}`, { ...options, headers });
    } catch {
      clearTokens();
      window.dispatchEvent(new Event("auth:logout"));
      throw new Error("Sesija je istekla. Prijavite se ponovo.");
    }
  }

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    const rawMsg = body.message || body.poruka || body.error || `HTTP ${res.status}`;
    // UX-02: prikazujemo korisnički prihvatljive poruke umjesto tehničkih
    // Za login endpoint (prijava), 401 znači pogrešna lozinka/email
    const context = path.includes("/auth/prijava") ? "login" : undefined;
    throw new Error(friendlyError(res.status, rawMsg, context));
  }
  if (res.status === 204) return null;
  return res.json();
}

// ─── Logout sa invalidacijom na serveru ────────────────────────────────────
export async function apiLogout() {
  const refreshToken = sessionStorage.getItem(REFRESH_TOKEN_KEY);
  if (_accessToken && refreshToken) {
    await fetch(`${API_BASE}/api/auth/odjava`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${_accessToken}`,
      },
      body: JSON.stringify({ refreshToken, accessToken: _accessToken }),
    }).catch(() => {});
  }
  clearTokens();
}
