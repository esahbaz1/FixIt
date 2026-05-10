import { useState, useEffect } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import { StatusChip } from "../components/Chips";
import Icon from "../components/Icon";

export default function Dashboard({ setActiveTab }) {
  const { token } = useAuth();
  const [prijave, setPrijave] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiCall("/api/prijave", {}, token).then(setPrijave).catch(() => {}).finally(() => setLoading(false));
  }, [token]);

  const s = {
    ukupno:  prijave.length,
    novo:    prijave.filter(p => p.statusNaziv === "Novo").length,
    uRadu:   prijave.filter(p => ["U radu","Dodijeljeno"].includes(p.statusNaziv)).length,
    rijeseno:prijave.filter(p => p.statusNaziv === "Rijeseno").length,
    hitno:   prijave.filter(p => p.prioritet === "HITNO").length,
  };

  const recent = [...prijave].sort((a,b) => new Date(b.datumPodnosenja)-new Date(a.datumPodnosenja)).slice(0,6);

  const byCat = {};
  prijave.forEach(p => { byCat[p.nazivKategorije||"Ostalo"] = (byCat[p.nazivKategorije||"Ostalo"]||0)+1; });
  const catList = Object.entries(byCat).sort((a,b) => b[1]-a[1]).slice(0,6);
  const catColors = [T.blue, T.green, T.purple, T.amber, T.orange, T.red];

  const statCards = [
    { label: "Ukupno",   val: s.ukupno,   color: T.textSub },
    { label: "Nove",     val: s.novo,     color: T.amber   },
    { label: "U toku",   val: s.uRadu,    color: T.blue    },
    { label: "Riješeno", val: s.rijeseno, color: T.green   },
    { label: "Hitne",    val: s.hitno,    color: T.red     },
  ];

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader
        title="Pregled"
        sub={new Date().toLocaleDateString("bs", { weekday: "long", day: "numeric", month: "long", year: "numeric" })}
        action={
          <button className="btn-prim" onClick={() => setActiveTab("nova")}>
            <Icon.Plus/> Nova prijava
          </button>
        }
      />

      {/* Stat row */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 12, marginBottom: 24 }}>
        {statCards.map((c, i) => (
          <div key={i} className="card stat-card" style={{ padding: "20px 22px" }}>
            <div style={{ fontSize: 11, fontWeight: 500, color: T.textMuted, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: 10 }}>{c.label}</div>
            <div style={{ fontSize: 30, fontWeight: 600, color: c.color, letterSpacing: "-0.03em", fontVariantNumeric: "tabular-nums" }}>
              {loading ? <span style={{ animation: "blink 1.4s infinite", color: T.textMuted }}>—</span> : c.val}
            </div>
          </div>
        ))}
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 360px", gap: 16 }}>
        {/* Recent */}
        <div className="card" style={{ overflow: "hidden" }}>
          <div style={{
            display: "flex", justifyContent: "space-between", alignItems: "center",
            padding: "18px 24px", borderBottom: `1px solid ${T.line}`,
          }}>
            <span style={{ fontSize: 13, fontWeight: 500, color: T.text }}>Nedavne prijave</span>
            <button onClick={() => setActiveTab("prijave")} className="btn-ghost" style={{ padding: "4px 10px", fontSize: 12 }}>
              Sve <Icon.ArrowR/>
            </button>
          </div>

          {loading ? (
            <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><Spinner size={28}/></div>
          ) : recent.length === 0 ? (
            <div style={{ padding: 48, textAlign: "center", color: T.textMuted, fontSize: 13 }}>Nema prijava</div>
          ) : (
            <div>
              <div style={{
                display: "grid", gridTemplateColumns: "1fr 100px 90px",
                padding: "8px 24px", gap: 12,
                borderBottom: `1px solid ${T.line}`,
                fontSize: 11, fontWeight: 500, color: T.textMuted, letterSpacing: "0.05em", textTransform: "uppercase",
              }}>
                <span>Naslov</span><span>Status</span><span style={{ textAlign: "right" }}>Datum</span>
              </div>
              {recent.map((p, i) => (
                <div key={p.id} className="tbl-row" style={{
                  gridTemplateColumns: "1fr 100px 90px", gap: 12,
                  height: 52,
                  animation: `fadeUp 0.25s ease ${i * 0.04}s both`,
                }}>
                  <div style={{ overflow: "hidden" }}>
                    <div style={{ fontSize: 13, fontWeight: 500, color: T.text, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{p.naslov}</div>
                    {p.adresa && <div style={{ fontSize: 11, color: T.textMuted, display: "flex", alignItems: "center", gap: 3, marginTop: 1 }}><Icon.Pin/>{p.adresa}</div>}
                  </div>
                  <div><StatusChip status={p.statusNaziv}/></div>
                  <div style={{ fontSize: 11, color: T.textMuted, textAlign: "right" }}>
                    {p.datumPodnosenja ? new Date(p.datumPodnosenja).toLocaleDateString("bs") : "—"}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right col */}
        <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          {/* Categories */}
          <div className="card" style={{ padding: "18px 22px" }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 18 }}>Po kategorijama</div>
            {loading ? (
              <div style={{ display: "flex", justifyContent: "center", padding: 24 }}><Spinner/></div>
            ) : catList.length === 0 ? (
              <p style={{ color: T.textMuted, fontSize: 13 }}>Nema podataka</p>
            ) : catList.map(([cat, count], i) => {
              const pct = s.ukupno ? Math.round((count/s.ukupno)*100) : 0;
              const col = catColors[i % catColors.length];
              return (
                <div key={cat} style={{ marginBottom: 14, animation: `fadeUp 0.25s ease ${i*0.05}s both` }}>
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 5 }}>
                    <span style={{ fontSize: 12, color: T.textSub }}>{cat}</span>
                    <span style={{ fontSize: 11, color: T.textMuted, fontFamily: "'Geist Mono', monospace" }}>{count}</span>
                  </div>
                  <div style={{ height: 3, background: T.bgActive, borderRadius: 2, overflow: "hidden" }}>
                    <div style={{ height: "100%", width: `${pct}%`, background: col, borderRadius: 2, transition: "width 0.8s ease" }}/>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Priority */}
          {!loading && prijave.length > 0 && (
            <div className="card" style={{ padding: "18px 22px" }}>
              <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 14 }}>Po prioritetu</div>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
                {[
                  { label: "Hitno",   key: "HITNO",   color: T.red    },
                  { label: "Visok",   key: "VISOK",   color: T.orange  },
                  { label: "Srednji", key: "SREDNJI", color: T.amber  },
                  { label: "Nizak",   key: "NIZAK",   color: T.green  },
                ].map(p => {
                  const n = prijave.filter(x => x.prioritet === p.key).length;
                  return (
                    <div key={p.key} style={{
                      padding: "12px 14px", borderRadius: 8,
                      background: T.bgRaised, border: `1px solid ${T.line}`,
                    }}>
                      <div style={{ fontSize: 20, fontWeight: 600, color: p.color, letterSpacing: "-0.03em", fontVariantNumeric: "tabular-nums" }}>{n}</div>
                      <div style={{ fontSize: 11, color: T.textMuted, marginTop: 2 }}>{p.label}</div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
