import { useState } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import AuthLayout from "../components/AuthLayout";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

export default function LoginPage({ onLogin, switchToRegister }) {
  const [email, setEmail] = useState("");
  const [lozinka, setLozinka] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPass, setShowPass] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true); setError("");
    try {
      // Šalje POST /api/auth/prijava, prima { token, refreshToken, id, email, uloga, ime, prezime }
      const data = await apiCall("/api/auth/prijava", {
        method: "POST",
        body: JSON.stringify({ email, lozinka }),
      });
      onLogin(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <div style={{ marginBottom: 36 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 16 }}>
          <div style={{
            width: 36, height: 36, borderRadius: 10,
            background: "linear-gradient(135deg, #2ECC71, #1B6B45)",
            display: "flex", alignItems: "center", justifyContent: "center",
          }}>
            <Icon.Shield/>
          </div>
          <span style={{ fontSize: 18, fontWeight: 700, color: T.text, letterSpacing: "-0.02em" }}>FixIt</span>
        </div>
        <h2 style={{ fontSize: 22, fontWeight: 600, letterSpacing: "-0.02em", color: T.text, marginBottom: 6 }}>
          Dobrodošli nazad
        </h2>
        <p style={{ color: T.textSub, fontSize: 13 }}>Prijavite se na vaš nalog</p>
      </div>

      {error && (
        <div style={{
          background: T.redDim, border: `1px solid ${T.redBorder}`,
          color: T.red, padding: "10px 14px", borderRadius: 8, marginBottom: 20, fontSize: 13,
          display: "flex", alignItems: "center", gap: 8,
        }}>
          <Icon.Alert/> {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 14 }}>
          <label className="label">Email adresa</label>
          <input
            type="email" required value={email}
            onChange={e => setEmail(e.target.value)}
            placeholder="vase@email.com"
            className="input-field"
            autoComplete="email"
          />
        </div>
        <div style={{ marginBottom: 24 }}>
          <label className="label">Lozinka</label>
          <div style={{ position: "relative" }}>
            <input
              type={showPass ? "text" : "password"}
              required value={lozinka}
              onChange={e => setLozinka(e.target.value)}
              placeholder="••••••••"
              className="input-field"
              autoComplete="current-password"
              style={{ paddingRight: 40 }}
            />
            <button type="button" onClick={() => setShowPass(p => !p)} style={{
              position: "absolute", right: 10, top: "50%", transform: "translateY(-50%)",
              background: "none", border: "none", cursor: "pointer", color: T.textMuted, padding: 4,
            }}>
              <Icon.Key/>
            </button>
          </div>
        </div>
        <button type="submit" disabled={loading} className="btn-prim" style={{ width: "100%", padding: "11px" }}>
          {loading ? <><Spinner size={16} color="#fff"/> Provjera...</> : "Prijavi se"}
        </button>
      </form>

      {/* Sigurnosna napomena */}
      <div style={{
        marginTop: 20, padding: "10px 14px",
        background: "rgba(46,204,113,0.08)", border: "1px solid rgba(46,204,113,0.2)",
        borderRadius: 8, display: "flex", alignItems: "flex-start", gap: 8,
      }}>
        <span style={{ color: "#2ECC71", flexShrink: 0, marginTop: 1 }}><Icon.Shield/></span>
        <span style={{ fontSize: 11, color: T.textSub, lineHeight: 1.5 }}>
          Veza je zaštićena JWT tokenom. Vaši podaci su bezbjedni.
        </span>
      </div>

      <p style={{ marginTop: 20, textAlign: "center", fontSize: 13, color: T.textSub }}>
        Nemate nalog?{" "}
        <button onClick={switchToRegister} style={{ background: "none", border: "none", color: "#2ECC71", fontWeight: 500, fontSize: 13, cursor: "pointer", padding: 0 }}>
          Registrujte se
        </button>
      </p>
    </AuthLayout>
  );
}
