import { useState, useEffect } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

const TIP_CFG = {
  "NOVA_PRIJAVA":         { icon: Icon.Plus,    color: "#2ECC71",  label: "Nova prijava"          },
  "PROMJENA_STATUSA":     { icon: Icon.Refresh, color: "#3498DB",  label: "Promjena statusa"      },
  "NOVI_KOMENTAR":        { icon: Icon.Mail,    color: "#9B59B6",  label: "Novi komentar"         },
  "DODIJELJENO":          { icon: Icon.User,    color: "#F39C12",  label: "Dodijeljena prijava"   },
  "UPOZORENJE":           { icon: Icon.Alert,   color: "#E74C3C",  label: "Upozorenje"            },
};

function NotifCard({ n, onMarkRead }) {
  const cfg = TIP_CFG[n.tip] || { icon: Icon.Bell, color: T.textSub, label: n.tip };
  const Ic = cfg.icon;

  return (
    <div
      style={{
        display: "flex", alignItems: "flex-start", gap: 14,
        padding: "16px 24px",
        background: n.procitano ? "transparent" : "rgba(46,204,113,0.04)",
        borderBottom: `1px solid ${T.line}`,
        transition: "background 0.2s",
      }}
    >
      <div style={{
        width: 34, height: 34, borderRadius: "50%", flexShrink: 0,
        background: `${cfg.color}18`, border: `1px solid ${cfg.color}30`,
        display: "flex", alignItems: "center", justifyContent: "center",
        color: cfg.color,
      }}>
        <Ic/>
      </div>

      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 4 }}>
          <div>
            <span style={{
              fontSize: 10, fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase",
              color: cfg.color, marginRight: 8,
            }}>{cfg.label}</span>
            {!n.procitano && (
              <span style={{
                display: "inline-block", width: 6, height: 6, borderRadius: "50%",
                background: "#2ECC71", verticalAlign: "middle",
              }}/>
            )}
          </div>
          <span style={{ fontSize: 11, color: T.textMuted, flexShrink: 0, marginLeft: 12 }}>
            {n.datumKreiranja ? new Date(n.datumKreiranja).toLocaleString("bs") : "—"}
          </span>
        </div>

        <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 3 }}>{n.naslov}</div>
        {n.poruka && <p style={{ fontSize: 12, color: T.textSub, lineHeight: 1.5, margin: 0 }}>{n.poruka}</p>}
      </div>

      {!n.procitano && (
        <button
          onClick={() => onMarkRead(n.id)}
          className="btn-ghost"
          style={{ padding: "4px 10px", fontSize: 11, flexShrink: 0, alignSelf: "center" }}
          title="Označi kao pročitano"
        >
          <Icon.Check/>
        </button>
      )}
    </div>
  );
}

export default function NotifikacijePage() {
  const { user } = useAuth();
  const [notif, setNotif] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("sve"); // sve | neprocitane
  const [markingAll, setMarkingAll] = useState(false);

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
      setNotif(prev => prev.map(n => n.id === id ? { ...n, procitano: true } : n));
    } catch {}
  }

  async function markAllRead() {
    setMarkingAll(true);
    try {
      const neproc = notif.filter(n => !n.procitano);
      await Promise.all(neproc.map(n => apiCall(`/api/notifikacije/${n.id}/procitano`, { method: "PATCH" }).catch(() => {})));
      setNotif(prev => prev.map(n => ({ ...n, procitano: true })));
    } catch {}
    finally { setMarkingAll(false); }
  }

  const neprocitane = notif.filter(n => !n.procitano).length;

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader
        title="Obavijesti"
        sub={neprocitane > 0 ? `${neprocitane} nepročitanih` : "Sve pročitano"}
        action={
          neprocitane > 0 && (
            <button onClick={markAllRead} disabled={markingAll} className="btn-ghost" style={{ fontSize: 12 }}>
              {markingAll ? <Spinner size={14}/> : <Icon.Check/>}
              Označi sve kao pročitano
            </button>
          )
        }
      />

      {/* Filter tabovi */}
      <div style={{ display: "flex", gap: 4, marginBottom: 16 }}>
        {[
          { key: "sve",        label: "Sve" },
          { key: "neprocitane", label: `Nepročitane${neprocitane > 0 ? ` (${neprocitane})` : ""}` },
        ].map(f => (
          <button key={f.key} onClick={() => setFilter(f.key)} className={`filter-chip ${filter === f.key ? "active" : ""}`}>
            {f.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spinner size={32}/></div>
      ) : notif.length === 0 ? (
        <div className="card" style={{ padding: 80, textAlign: "center" }}>
          <div style={{ fontSize: 40, marginBottom: 16, opacity: 0.3 }}>🔔</div>
          <div style={{ fontSize: 14, color: T.textSub, marginBottom: 4 }}>
            {filter === "neprocitane" ? "Nema nepročitanih obavijesti" : "Nema obavijesti"}
          </div>
          <div style={{ fontSize: 12, color: T.textMuted }}>
            Ovdje ćete vidjeti obavijesti o promjenama vaših prijava.
          </div>
        </div>
      ) : (
        <div className="card" style={{ overflow: "hidden" }}>
          {notif.map((n, i) => (
            <div key={n.id} style={{ animation: `fadeUp 0.2s ease ${i * 0.02}s both` }}>
              <NotifCard n={n} onMarkRead={markRead}/>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
