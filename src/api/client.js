export const API_BASE = "http://localhost:8080";

// ─── Token Storage ─────────────────────────────────────────────────────────
// Access token čuvan u memoriji (ne localStorage) radi XSS zaštite.
let _accessToken = null;
let _refreshToken = null;
let _refreshPromise = null;
let _korisnikUloga = null;
let _korisnikId = null;

export function setTokens(access, refresh) {
  _accessToken = access;
  if (refresh !== undefined) _refreshToken = refresh;
}

export function setKorisnikKontekst(uloga, id) {
  _korisnikUloga = uloga;
  _korisnikId = id;
}

export function clearTokens() {
  _accessToken = null;
  _refreshToken = null;
  _korisnikUloga = null;
  _korisnikId = null;
}

export function getAccessToken() { return _accessToken; }

// ─── Auto-refresh mehanizam ─────────────────────────────────────────────────
async function doRefresh() {
  if (!_refreshToken) throw new Error("No refresh token");
  const res = await fetch(`${API_BASE}/api/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: _refreshToken }),
  });
  if (!res.ok) throw new Error("Refresh failed");
  const data = await res.json();
  _accessToken = data.token;
  if (data.refreshToken) _refreshToken = data.refreshToken;
  return data.token;
}

async function refreshAccessToken() {
  if (!_refreshPromise) {
    _refreshPromise = doRefresh().finally(() => { _refreshPromise = null; });
  }
  return _refreshPromise;
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
  if (res.status === 401 && !explicitToken && _refreshToken) {
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
    const err = await res.json().catch(() => ({ message: `HTTP ${res.status}` }));
    throw new Error(err.message || err.poruka || err.error || `HTTP ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

// ─── Logout sa invalidacijom na serveru ────────────────────────────────────
export async function apiLogout() {
  if (_accessToken && _refreshToken) {
    await fetch(`${API_BASE}/api/auth/odjava`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${_accessToken}`,
      },
      body: JSON.stringify({ refreshToken: _refreshToken, accessToken: _accessToken }),
    }).catch(() => {});
  }
  clearTokens();
}
