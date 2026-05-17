import { useState } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { KATEGORIJE, PRIO_CFG } from "../api/constants";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";

export default function NovaPrijavaPage({ onSuccess }) {
  const { token, user } = useAuth();
  const [form, setForm] = useState({ naslov: "", opis: "", latitude: "", longitude: "", adresa: "", kategorijaId: "1", prioritet: "SREDNJI" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true); setError("");
    try {
      await apiCall("/api/prijave", {
        method: "POST",
        body: JSON.stringify({
          naslov: form.naslov, opis: form.opis,
          latitude: parseFloat(form.latitude) || 43.8563,
          longitude: parseFloat(form.longitude) || 18.4131,
          adresa: form.adresa,
          kategorijaId: parseInt(form.kategorijaId),
          korisnikId: user?.id || 1,
          prioritet: form.prioritet,
        }),
      }, token);
      setDone(true);
      setTimeout(onSuccess, 1600);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  }

  if (done) return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: "80px 0", animation: "fadeUp 0.4s ease" }}>
      <div style={{
        width: 56, height: 56, borderRadius: "50%",
        background: T.greenDim, border: `1px solid ${T.greenBorder}`,
        display: "flex", alignItems: "center", justifyContent: "center",
        marginBottom: 20,
      }}>
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <polyline points="3,10 8,15 17,5" stroke={T.green} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </div>
      <h3 style={{ fontSize: 18, fontWeight: 600, color: T.text, marginBottom: 8, letterSpacing: "-0.01em" }}>Prijava kreirana</h3>
      <p style={{ color: T.textSub, fontSize: 13 }}>Preusmjeravamo vas...</p>
    </div>
  );

  const prioOptions = [
    { v: "NIZAK",   label: "Nizak",   color: T.green  },
    { v: "SREDNJI", label: "Srednji", color: T.amber  },
    { v: "VISOK",   label: "Visok",   color: T.orange },
    { v: "HITNO",   label: "Hitno",   color: T.red    },
  ];

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader title="Nova prijava" sub="Prijavite komunalni problem"/>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 280px", gap: 16 }}>
        <div className="card" style={{ padding: 32 }}>
          {error && (
            <div style={{
              background: T.redDim, border: `1px solid ${T.redBorder}`,
              color: T.red, padding: "10px 14px", borderRadius: 8, marginBottom: 20, fontSize: 13,
            }}>{error}</div>
          )}

          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: 18 }}>
              <label className="label">Naslov *</label>
              <input type="text" required value={form.naslov} onChange={e => setForm({...form, naslov: e.target.value})} placeholder="npr. Oštećenje asfalta — Titova ulica" className="input-field"/>
            </div>

            <div style={{ marginBottom: 18 }}>
              <label className="label">Opis *</label>
              <textarea required value={form.opis} onChange={e => setForm({...form, opis: e.target.value})} placeholder="Opišite problem — veličina, utjecaj, lokacija..." rows={4} className="input-field" style={{ resize: "vertical", minHeight: 100 }}/>
            </div>

            {/* Category */}
            <div style={{ marginBottom: 18 }}>
              <label className="label">Kategorija *</label>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 6 }}>
                {KATEGORIJE.map(k => (
                  <button key={k.id} type="button" onClick={() => setForm({...form, kategorijaId: String(k.id)})} style={{
                    padding: "9px 6px", borderRadius: 8, cursor: "pointer",
                    background: form.kategorijaId === String(k.id) ? T.blueDim : T.bgRaised,
                    border: `1px solid ${form.kategorijaId === String(k.id) ? T.blueBorder : T.line}`,
                    color: form.kategorijaId === String(k.id) ? T.blue : T.textSub,
                    fontSize: 11, fontWeight: 500, transition: "all 0.12s", textAlign: "center",
                  }}>
                    {k.naziv}
                  </button>
                ))}
              </div>
            </div>

            {/* Priority */}
            <div style={{ marginBottom: 18 }}>
              <label className="label">Prioritet</label>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 6 }}>
                {prioOptions.map(p => {
                  const active = form.prioritet === p.v;
                  const cfg = PRIO_CFG[p.v];
                  return (
                    <button key={p.v} type="button" onClick={() => setForm({...form, prioritet: p.v})} style={{
                      padding: "9px 6px", borderRadius: 8, cursor: "pointer",
                      background: active ? cfg.dim : T.bgRaised,
                      border: `1px solid ${active ? p.color + "50" : T.line}`,
                      color: active ? p.color : T.textSub,
                      fontSize: 11, fontWeight: 500, transition: "all 0.12s", textAlign: "center",
                    }}>
                      {p.label}
                    </button>
                  );
                })}
              </div>
            </div>

            <div style={{ marginBottom: 16 }}>
              <label className="label">Adresa</label>
              <input type="text" value={form.adresa} onChange={e => setForm({...form, adresa: e.target.value})} placeholder="Titova ulica 15, Sarajevo" className="input-field"/>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, marginBottom: 24 }}>
              <div>
                <label className="label">Latitude</label>
                <input type="number" step="any" value={form.latitude} onChange={e => setForm({...form, latitude: e.target.value})} placeholder="43.8563" className="input-field"/>
              </div>
              <div>
                <label className="label">Longitude</label>
                <input type="number" step="any" value={form.longitude} onChange={e => setForm({...form, longitude: e.target.value})} placeholder="18.4131" className="input-field"/>
              </div>
            </div>

            <button type="submit" disabled={loading} className="btn-prim" style={{ width: "100%", padding: "11px" }}>
              {loading ? <><Spinner size={16} color="#fff"/> Kreiranje...</> : "Pošalji prijavu"}
            </button>
          </form>
        </div>

        {/* Tips */}
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <div className="card" style={{ padding: 20 }}>
            <div style={{ fontSize: 12, fontWeight: 500, color: T.textSub, marginBottom: 14 }}>Smjernice</div>
            {[
              "Konkretan naslov koji opisuje problem",
              "Detaljan opis veličine i utjecaja",
              "Tačna adresa ubrzava terene",
              "GPS koordinate za precizno lociranje",
              "Realan prioritet — hitno samo ako je opasnost",
            ].map((tip, i) => (
              <div key={i} style={{ display: "flex", gap: 10, marginBottom: 10, alignItems: "flex-start" }}>
                <div style={{
                  width: 16, height: 16, borderRadius: 3,
                  background: T.bgActive, border: `1px solid ${T.line}`,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  flexShrink: 0, marginTop: 1,
                }}>
                  <svg width="7" height="7" viewBox="0 0 7 7" fill="none">
                    <polyline points="1,3.5 3,5.5 6,1.5" stroke={T.textMuted} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </div>
                <span style={{ fontSize: 12, color: T.textSub, lineHeight: 1.5 }}>{tip}</span>
              </div>
            ))}
          </div>

          <div className="card" style={{ padding: 20, borderColor: T.redBorder }}>
            <div style={{ fontSize: 12, fontWeight: 500, color: T.red, marginBottom: 10 }}>Hitne situacije</div>
            <p style={{ fontSize: 12, color: T.textSub, lineHeight: 1.6, margin: 0 }}>
              Za situacije koje direktno ugrožavaju ljude ili imovinu, kontaktirajte komunalne službe direktno uz odabir prioriteta "Hitno".
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
