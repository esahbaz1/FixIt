import { useState } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import AuthLayout from "../components/AuthLayout";
import Spinner from "../components/Spinner";

export default function LoginPage({ onLogin, switchToRegister }) {
  const [email, setEmail] = useState("");
  const [lozinka, setLozinka] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true); setError("");
    try { onLogin(await apiCall("/api/auth/prijava", { method: "POST", body: JSON.stringify({ email, lozinka }) })); }
    catch (err) { setError(err.message); }
    finally { setLoading(false); }
  }

  return (
    <AuthLayout>
      <div style={{ marginBottom: 36 }}>
        <h2 style={{ fontSize: 24, fontWeight: 600, letterSpacing: "-0.02em", color: T.text, marginBottom: 6 }}>Dobrodošli</h2>
        <p style={{ color: T.textSub, fontSize: 13 }}>Prijavite se na vaš nalog</p>
      </div>

      {error && (
        <div style={{
          background: T.redDim, border: `1px solid ${T.redBorder}`,
          color: T.red, padding: "10px 14px", borderRadius: 8, marginBottom: 20, fontSize: 13,
        }}>{error}</div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 14 }}>
          <label className="label">Email</label>
          <input type="email" required value={email} onChange={e => setEmail(e.target.value)} placeholder="vase@email.com" className="input-field"/>
        </div>
        <div style={{ marginBottom: 24 }}>
          <label className="label">Lozinka</label>
          <input type="password" required value={lozinka} onChange={e => setLozinka(e.target.value)} placeholder="••••••••" className="input-field"/>
        </div>
        <button type="submit" disabled={loading} className="btn-prim" style={{ width: "100%", padding: "10px" }}>
          {loading ? <><Spinner size={16} color="#fff"/> Prijava...</> : "Prijavi se"}
        </button>
      </form>

      <p style={{ marginTop: 24, textAlign: "center", fontSize: 13, color: T.textSub }}>
        Nemate nalog?{" "}
        <button onClick={switchToRegister} style={{ background: "none", border: "none", color: T.blue, fontWeight: 500, fontSize: 13, cursor: "pointer", padding: 0 }}>
          Registrujte se
        </button>
      </p>
    </AuthLayout>
  );
}
