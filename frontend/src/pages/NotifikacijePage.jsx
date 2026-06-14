import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

// ─── Konfiguracija tipova notifikacija ───────────────────────────────────────

const TIP_CFG = {
  NOVA_PRIJAVA:    { ikona: "📋", color: "#2ECC71",  label: "Nova prijava"              },
  STATUS_PROMJENA: { ikona: "🔄", color: "#3498DB",  label: "Promjena statusa"          },
  NOVI_KOMENTAR:   { ikona: "💬", color: "#9B59B6",  label: "Novi komentar"             },
  DODIJELJENO:     { ikona: "📌", color: "#F39C12",  label: "Dodijeljena prijava"       },
  DODJELA_SLUZBI:  { ikona: "🏢", color: "#F39C12",  label: "Dodijeljena službi"        },
  DODJELA_RADNIKU: { ikona: "👷", color: "#E67E22",  label: "Dodijeljena vam prijava"   },
  UPOZORENJE:      { ikona: "⚠️", color: "#E74C3C",  label: "Upozorenje"                },
  RIJESENO:        { ikona: "✅", color: "#2ECC71",  label: "Riješeno"                  },
};

const DEFAULT_CFG = { ikona: "🔔", color: T.textSub, label: "Obavijest" };

// ─── Pojedinačna kartica notifikacije ────────────────────────────────────────

function NotifCard({ n, onMarkRead }) {
  const navigate = useNavigate();
  const cfg = TIP_CFG[n.tip] || DEFAULT_CFG;

  const handleKlik = () => {
    if (!n.procitano) onMarkRead(n.id);
    if (n.prijavaId) navigate(`/prijave/${n.prijavaId}`);
  };

  const vrijemeFormatted = n.datumKreiranja
    ? new Date(n.datumKreiranja).toLocaleString("bs-BA", {
        day: "2-digit", month: "2-digit", year: "numeric",
        hour: "2-digit", minute: "2-digit",
      })
    : "—";

  return (
    <div
      onClick={n.prijavaId ? handleKlik : undefined}
      style={{
        display: "flex", alignItems: "flex-start", gap: 14,
        padding: "18px 24px",
        background: n.procitano ? "transparent" : `${cfg.color}08`,
        borderBottom: `1px solid ${T.line}`,
        cursor: n.prijavaId ? "pointer" : "default",
        transition: "background 0.2s",
      }}
      onMouseEnter={e => { if (n.prijavaId) e.currentTarget.style.background = `${cfg.color}14`; }}
      onMouseLeave={e => { e.currentTarget.style.background = n.procitano ? "transparent" : `${cfg.color}08`; }}
    >
      {/* Ikona */}
      <div style={{
        width: 40, height: 40, borderRadius: "50%", flexShrink: 0,
        background: `${cfg.color}18`, border: `1px solid ${cfg.color}30`,
        display: "flex", alignItems: "center", justifyContent: "center",
        fontSize: 18,
      }}>
        {cfg.ikona}
      </div>

      {/* Sadržaj */}
      <div style={{ flex: 1, minWidth: 0 }}>

        {/* Red: tip + datum */}
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 4 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{
              fontSize: 10, fontWeight: 700, letterSpacing: "0.07em",
              textTransform: "uppercase", color: cfg.color,
            }}>
              {cfg.label}
            </span>
            {!n.procitano && (
              <span style={{
                display: "inline-block", width: 7, height: 7, borderRadius: "50%",
                background: cfg.color, flexShrink: 0,
              }} title="Nepročitano" />
            )}
          </div>
          <span style={{ fontSize: 11, color: T.textMuted, flexShrink: 0, marginLeft: 12 }}>
            {vrijemeFormatted}
          </span>
        </div>

        {/* Naslov */}
        <div style={{
          fontSize: 14, fontWeight: 600, color: n.procitano ? T.textSub : T.text,
          marginBottom: 5, lineHeight: 1.4,
        }}>
          {n.naslov}
        </div>

        {/* Tekst poruke */}
        {n.tekst && (
          <p style={{
            fontSize: 13, color: T.textSub, lineHeight: 1.55,
            margin: "0 0 6px 0",
          }}>
            {n.tekst}
          </p>
        )}

        {/* Donji red: ID prijave + link */}
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginTop: 2 }}>
          {n.prijavaId && (
            <span style={{ fontSize: 11, color: T.textMuted }}>
              Prijava #{n.prijavaId}
            </span>
          )}
          {n.prijavaId && !n.procitano && (
            <span style={{ fontSize: 11, color: cfg.color, fontWeight: 600 }}>
              Klikni za pregled →
            </span>
          )}
          {n.datumCitanja && (
            <span style={{ fontSize: 11, color: T.textMuted, marginLeft: "auto" }}>
              Pročitano: {new Date(n.datumCitanja).toLocaleString("bs-BA", {
                day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit",
              })}
            </span>
          )}
        </div>
      </div>

      {/* Dugme "Označi pročitano" */}
      {!n.procitano && (
        <button
          onClick={(e) => { e.stopPropagation(); onMarkRead(n.id); }}
          className="btn-ghost"
          style={{ padding: "5px 10px", fontSize: 11, flexShrink: 0, alignSelf: "center" }}
          title="Označi kao pročitano"
        >
          <Icon.Check />
        </button>
      )}
    </div>
  );
}

// ─── Stranica ─────────────────────────────────────────────────────────────────

export default function NotifikacijePage() {
  const { user } = useAuth();
  const [notif,      setNotif]      = useState([]);
  const [loading,    setLoading]    = useState(true);
  const [filter,     setFilter]     = useState("sve");
  const [tipFilter,  setTipFilter]  = useState("sve");
  const [markingAll, setMarkingAll] = useState(false);

  const TIPOVI_FILTER = [
    { key: "sve",            label: "Sve" },
    { key: "STATUS_PROMJENA",label: "Status" },
    { key: "DODJELA_RADNIKU",label: "Dodjele" },
    { key: "RIJESENO",       label: "Riješeno" },
    { key: "UPOZORENJE",     label: "Upozorenja" },
  ];

  useEffect(() => {
    if (!user?.id) return;
    setLoading(true);
    const endpoint = filter === "neprocitane"
      ? `/api/notifikacije/korisnik/${user.id}/neprocitane`
      : `/api/notifikacije/korisnik/${user.id}`;
    apiCall(endpoint)
      .then(data => setNotif(Array.isArray(data) ? data : []))
      .catch(() => setNotif([]))
      .finally(() => setLoading(false));
  }, [user?.id, filter]);

  async function markRead(id) {
    try {
      await apiCall(`/api/notifikacije/${id}/procitano`, { method: "PATCH" });
      setNotif(prev => prev.map(n => n.id === id
        ? { ...n, procitano: true, datumCitanja: new Date().toISOString() }
        : n));
    } catch {}
  }

  async function markAllRead() {
    setMarkingAll(true);
    try {
      const neproc = prikazane.filter(n => !n.procitano);
      await Promise.all(neproc.map(n =>
        apiCall(`/api/notifikacije/${n.id}/procitano`, { method: "PATCH" }).catch(() => {})
      ));
      const ids = new Set(neproc.map(n => n.id));
      setNotif(prev => prev.map(n =>
        ids.has(n.id) ? { ...n, procitano: true, datumCitanja: new Date().toISOString() } : n
      ));
    } finally {
      setMarkingAll(false);
    }
  }

  // Primijeni filter tipa
  const prikazane = notif.filter(n => tipFilter === "sve" || n.tip === tipFilter);
  const neprocitane = notif.filter(n => !n.procitano).length;

  // Statistika po tipu
  const stats = Object.entries(TIP_CFG).map(([key, cfg]) => ({
    tip: key, cfg,
    ukupno: notif.filter(n => n.tip === key).length,
    neproc: notif.filter(n => n.tip === key && !n.procitano).length,
  })).filter(s => s.ukupno > 0);

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader
        title="Obavijesti"
        sub={neprocitane > 0 ? `${neprocitane} nepročitanih` : "Sve pročitano"}
        action={
          neprocitane > 0 && (
            <button onClick={markAllRead} disabled={markingAll} className="btn-ghost" style={{ fontSize: 12 }}>
              {markingAll ? <Spinner size={14} /> : <Icon.Check />}
              Označi sve kao pročitano
            </button>
          )
        }
      />

      {/* Statistika po tipu */}
      {stats.length > 0 && (
        <div style={{ display: "flex", gap: 10, marginBottom: 20, flexWrap: "wrap" }}>
          {stats.map(({ tip, cfg, ukupno, neproc }) => (
            <div key={tip} style={{
              display: "flex", alignItems: "center", gap: 8,
              padding: "8px 14px", borderRadius: 20,
              background: `${cfg.color}12`, border: `1px solid ${cfg.color}30`,
              cursor: "pointer",
              outline: tipFilter === tip ? `2px solid ${cfg.color}` : "none",
            }} onClick={() => setTipFilter(tipFilter === tip ? "sve" : tip)}>
              <span style={{ fontSize: 15 }}>{cfg.ikona}</span>
              <div>
                <div style={{ fontSize: 11, fontWeight: 700, color: cfg.color }}>{cfg.label}</div>
                <div style={{ fontSize: 10, color: T.textMuted }}>
                  {ukupno} ukupno{neproc > 0 ? ` · ${neproc} novo` : ""}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Filteri: sve / neprocitane */}
      <div style={{ display: "flex", gap: 4, marginBottom: 16 }}>
        {[
          { key: "sve",         label: "Sve" },
          { key: "neprocitane", label: `Nepročitane${neprocitane > 0 ? ` (${neprocitane})` : ""}` },
        ].map(f => (
          <button key={f.key} onClick={() => setFilter(f.key)}
            className={`filter-chip ${filter === f.key ? "active" : ""}`}>
            {f.label}
          </button>
        ))}
      </div>

      {/* Lista */}
      {loading ? (
        <div style={{ display: "flex", justifyContent: "center", padding: 80 }}>
          <Spinner size={32} />
        </div>
      ) : prikazane.length === 0 ? (
        <div className="card" style={{ padding: 80, textAlign: "center" }}>
          <div style={{ fontSize: 40, marginBottom: 16, opacity: 0.3 }}>🔔</div>
          <div style={{ fontSize: 14, color: T.textSub, marginBottom: 4 }}>
            {filter === "neprocitane" ? "Nema nepročitanih obavijesti" : "Nema obavijesti"}
          </div>
          <div style={{ fontSize: 12, color: T.textMuted }}>
            Ovdje ćete vidjeti obavijesti o promjenama vaših prijava i dodjela.
          </div>
        </div>
      ) : (
        <div className="card" style={{ overflow: "hidden" }}>
          {prikazane.map((n, i) => (
            <div key={n.id} style={{ animation: `fadeUp 0.2s ease ${i * 0.02}s both` }}>
              <NotifCard n={n} onMarkRead={markRead} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
