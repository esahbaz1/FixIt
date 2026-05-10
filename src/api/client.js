export const API_BASE = "http://localhost:8080";

export async function apiCall(path, options = {}, token = null) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: "Greška na serveru" }));
    throw new Error(err.message || err.poruka || `HTTP ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}
