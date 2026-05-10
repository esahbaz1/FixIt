import { useState } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import AuthLayout from "../components/AuthLayout";
import Spinner from "../components/Spinner";

export default function RegisterPage({ onSuccess, switchToLogin }) {
  const [form, setForm] = useState({ ime: "", prezime: "", email: "", lozinka: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true); setError("");
    try { await apiCall("/api/auth/registracija", { method: "POST", body: JSON.stringify(form) }); onSuccess(); }
    catch (err) { setError(err.message); }
    finally { setLoading(false); }
  }

  return (
    <AuthLayout>
      <div style={{ marginBottom: 36 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, letterSpacing: "-0.02em", color: T.text, marginBottom: 6 }}>Kreirajte nalog</h2>
        <p style={{ color: T.textSub, fontSize: 13 }}>Brza registracija bez komplikacija</p>
      </div>

      {error && (
        <div style={{
          background: T.redDim, border: `1px solid ${T.redBorder}`,
          color: T.red, padding: "10px 14px", borderRadius: 8, marginBottom: 20, fontSize: 13,
        }}>{error}</div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 14 }}>
          <div>
            <label className="label">Ime</label>
            <input type="text" required placeholder="Adnan" value={form.ime} onChange={e => setForm({...form, ime: e.target.value})} className="input-field"/>
          </div>
          <div>
            <label className="label">Prezime</label>
            <input type="text" required placeholder="Hodžić" value={form.prezime} onChange={e => setForm({...form, prezime: e.target.value})} className="input-field"/>
          </div>
        </div>
        <div style={{ marginBottom: 14 }}>
          <label className="label">Email</label>
          <input type="email" required placeholder="adnan@email.com" value={form.email} onChange={e => setForm({...form, email: e.target.value})} className="input-field"/>
        </div>
        <div style={{ marginBottom: 24 }}>
          <label className="label">Lozinka</label>
          <input type="password" required placeholder="Min. 8 karaktera" value={form.lozinka} onChange={e => setForm({...form, lozinka: e.target.value})} className="input-field"/>
        </div>
        <button type="submit" disabled={loading} className="btn-prim" style={{ width: "100%", padding: "10px" }}>
          {loading ? <><Spinner size={16} color="#fff"/> Kreiranje...</> : "Kreiraj nalog"}
        </button>
      </form>

      <p style={{ marginTop: 24, textAlign: "center", fontSize: 13, color: T.textSub }}>
        Već imate nalog?{" "}
        <button onClick={switchToLogin} style={{ background: "none", border: "none", color: T.blue, fontWeight: 500, fontSize: 13, cursor: "pointer", padding: 0 }}>
          Prijavite se
        </button>
      </p>
    </AuthLayout>
  );
}
