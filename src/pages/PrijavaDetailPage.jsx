import { useState } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import Spinner from "../components/Spinner";
import Toast from "../components/Toast";
import { StatusChip, PrioChip } from "../components/Chips";
import Icon from "../components/Icon";

export default function PrijavaDetailPage({ prijava, onBack, onUpdated }) {
  const { token } = useAuth();
  const [noviStatus, setNoviStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);
  const statuses = ["Novo","Dodijeljeno","U radu","Rijeseno","Zatvoreno"];

  async function handleStatus() {
    if (!noviStatus) return;
    setLoading(true);
    try {
      await apiCall(`/api/prijave/${prijava.id}/status?noviStatus=${encodeURIComponent(noviStatus)}&korisnikId=1`, { method: "PATCH" }, token);
      setToast({ msg: "Status uspješno promijenjen.", type: "success" });
      setTimeout(onUpdated, 1200);
    } catch (err) { setToast({ msg: err.message, type: "error" }); }
    finally { setLoading(false); }
  }

  const fmt = d => d ? new Date(d).toLocaleDateString("bs") : "—";
  const fmtDt = d => d ? new Date(d).toLocaleString("bs") : "—";

  return (
    <div style={{ animation: "fadeIn 0.25s ease" }}>
      {toast && <Toast message={toast.msg} type={toast.type} onDone={() => setToast(null)}/>}

      <button className="btn-ghost" onClick={onBack} style={{ marginBottom: 28, fontSize: 12, padding: "5px 12px" }}>
        <Icon.ChevLeft/> Nazad
      </button>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 300px", gap: 16 }}>
        {/* Main */}
        <div className="card" style={{ padding: 32 }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 28 }}>
            <div>
              <h2 style={{ fontSize: 20, fontWeight: 600, color: T.text, letterSpacing: "-0.02em", marginBottom: 8 }}>{prijava.naslov}</h2>
              {prijava.adresa && (
                <div style={{ display: "flex", alignItems: "center", gap: 5, color: T.textSub, fontSize: 12 }}>
                  <Icon.Pin/> {prijava.adresa}
                </div>
              )}
            </div>
            <StatusChip status={prijava.statusNaziv}/>
          </div>

          {/* Opis */}
          <div style={{
            background: T.bgRaised, border: `1px solid ${T.line}`,
            borderRadius: 8, padding: "16px 18px", marginBottom: 24,
          }}>
            <div style={{ fontSize: 11, fontWeight: 500, color: T.textMuted, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: 10 }}>Opis</div>
            <p style={{ color: T.textSub, fontSize: 13, lineHeight: 1.75, margin: 0 }}>{prijava.opis}</p>
          </div>

          {/* Meta grid */}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 10 }}>
            {[
              { label: "Kategorija",       val: prijava.nazivKategorije || "—" },
              { label: "Prioritet",        val: <PrioChip priority={prijava.prioritet}/> },
              { label: "Korisnik ID",      val: `#${prijava.korisnikId}` },
              { label: "Datum prijave",    val: fmtDt(prijava.datumPodnosenja) },
              { label: "Rok",              val: fmt(prijava.datumRoka) || "Nije postavljen" },
              { label: "Završeno",         val: fmt(prijava.datumZavrsetka) },
            ].map(({ label, val }) => (
              <div key={label} style={{
                background: T.bgRaised, border: `1px solid ${T.line}`,
                borderRadius: 8, padding: "12px 14px",
              }}>
                <div style={{ fontSize: 10, fontWeight: 500, color: T.textMuted, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: 6 }}>{label}</div>
                <div style={{ fontSize: 13, color: T.text }}>{val}</div>
              </div>
            ))}
          </div>

          {prijava.latitude && prijava.longitude && (
            <div style={{
              marginTop: 12, padding: "10px 14px",
              background: T.blueDim, border: `1px solid ${T.blueBorder}`,
              borderRadius: 8, display: "flex", alignItems: "center", gap: 10,
            }}>
              <Icon.Pin/>
              <span style={{ fontSize: 12, color: T.textSub }}>GPS: </span>
              <code style={{ fontSize: 12, color: T.blue, fontFamily: "'Geist Mono', monospace" }}>
                {prijava.latitude.toFixed(6)}, {prijava.longitude.toFixed(6)}
              </code>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <div className="card" style={{ padding: 22 }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 16 }}>Promijeni status</div>
            <div style={{ marginBottom: 12 }}>
              <label className="label">Novi status</label>
              <select value={noviStatus} onChange={e => setNoviStatus(e.target.value)} className="input-field">
                <option value="">Odaberi...</option>
                {statuses.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <button onClick={handleStatus} disabled={!noviStatus || loading} className="btn-prim" style={{ width: "100%" }}>
              {loading ? <><Spinner size={14} color="#fff"/> Ažuriranje...</> : "Ažuriraj status"}
            </button>
          </div>

          <div className="card" style={{ padding: 22 }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 14 }}>Detalji prijave</div>
            {[
              { label: "ID", val: `#${prijava.id}` },
              { label: "Arhivirana", val: prijava.arhiviran ? "Da" : "Ne", color: prijava.arhiviran ? T.amber : T.textSub },
            ].map(({ label, val, color }) => (
              <div key={label} style={{
                display: "flex", justifyContent: "space-between", alignItems: "center",
                padding: "9px 0", borderBottom: `1px solid ${T.line}`,
              }}>
                <span style={{ fontSize: 12, color: T.textMuted }}>{label}</span>
                <span style={{ fontSize: 12, fontWeight: 500, color: color || T.text, fontFamily: label === "ID" ? "'Geist Mono', monospace" : "inherit" }}>{val}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
