import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import { StatusChip } from "../components/Chips";
import Icon from "../components/Icon";

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [prijave, setPrijave] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  async function loadPrijave(isRefresh = false) {
    if (isRefresh) setRefreshing(true);
    setError("");
    try {
      const data = await apiCall("/api/prijave");
      setPrijave(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Greška pri učitavanju prijava:", err);
      setError(err.message || "Sistem je trenutno nedostupan. Pokušajte ponovo za nekoliko minuta.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }

  useEffect(() => { loadPrijave(); }, []);

  const s = {
    ukupno:   prijave.length,
    novo:     prijave.filter(p => p.statusNaziv === "Novo").length,
    uRadu:    prijave.filter(p => ["U radu","Dodijeljeno"].includes(p.statusNaziv)).length,
    rijeseno: prijave.filter(p => p.statusNaziv === "Rijeseno").length,
    hitno:    prijave.filter(p => p.prioritet === "HITNO").length,
  };

  const recent = [...prijave].sort((a,b) => new Date(b.datumPodnosenja)-new Date(a.datumPodnosenja)).slice(0,6);

  const byCat = {};
  prijave.forEach(p => { byCat[p.nazivKategorije||"Ostalo"] = (byCat[p.nazivKategorije||"Ostalo"]||0)+1; });
  const catList = Object.entries(byCat).sort((a,b) => b[1]-a[1]).slice(0,6);
  const catColors = ["#2ECC71","#3498DB","#9B59B6","#F39C12","#E67E22","#E74C3C"];

  const statCards = [
    { label: "Ukupno",   val: s.ukupno,    color: T.textSub,   icon: "◆" },
    { label: "Nove",     val: s.novo,      color: "#F39C12",   icon: "◉" },
    { label: "U toku",   val: s.uRadu,     color: "#3498DB",   icon: "⟳" },
    { label: "Riješeno", val: s.rijeseno,  color: "#2ECC71",   icon: "✓" },
    { label: "Hitne",    val: s.hitno,     color: "#E74C3C",   icon: "!" },
  ];

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader
        title={`Zdravo${user?.ime ? `, ${user.ime}` : ""}`}
        sub={new Date().toLocaleDateString("bs", { weekday: "long", day: "numeric", month: "long", year: "numeric" })}
        action={
          <div style={{ display: "flex", gap: 8 }}>
            <button onClick={() => loadPrijave(true)} disabled={refreshing} className="btn-ghost" style={{ padding: "7px 12px", fontSize: 12 }}>
              {refreshing ? <Spinner size={14}/> : <Icon.Refresh/>}
            </button>
            <button className="btn-prim" onClick={() => navigate("/nova")}>
              <Icon.Plus/> Nova prijava
            </button>
          </div>
        }
      />

      <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 12, marginBottom: 24 }}>
        {statCards.map((c, i) => (
          <div key={i} className="card stat-card" style={{ padding: "20px 22px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 10 }}>
              <div style={{ fontSize: 11, fontWeight: 500, color: T.textMuted, letterSpacing: "0.06em", textTransform: "uppercase" }}>{c.label}</div>
              <span style={{ fontSize: 14, color: c.color, opacity: 0.7 }}>{c.icon}</span>
            </div>
            <div style={{ fontSize: 30, fontWeight: 600, color: c.color, letterSpacing: "-0.03em", fontVariantNumeric: "tabular-nums" }}>
              {loading ? <span style={{ animation: "blink 1.4s infinite", color: T.textMuted }}>—</span> : c.val}
            </div>
          </div>
        ))}
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 360px", gap: 16 }}>
      
        <div className="card" style={{ overflow: "hidden" }}>
          <div style={{
            display: "flex", justifyContent: "space-between", alignItems: "center",
            padding: "18px 24px", borderBottom: `1px solid ${T.line}`,
          }}>
            <span style={{ fontSize: 13, fontWeight: 500, color: T.text }}>Nedavne prijave</span>
            <button onClick={() => navigate("/prijave")} className="btn-ghost" style={{ padding: "4px 10px", fontSize: 12 }}>
              Sve <Icon.ArrowR/>
            </button>
          </div>

          {loading ? (
            <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><Spinner size={28}/></div>
          ) : error ? (
            <div style={{ padding: 48, textAlign: "center" }}>
              <div style={{ fontSize: 28, opacity: 0.4, marginBottom: 12 }}>⚠</div>
              <div style={{ color: T.red, fontSize: 13, marginBottom: 16 }}>{error}</div>
              <button onClick={() => loadPrijave(true)} className="btn-ghost" style={{ fontSize: 12 }}>Pokušaj ponovo</button>
            </div>
          ) : recent.length === 0 ? (
            <div style={{ padding: 48, textAlign: "center" }}>
              <div style={{ fontSize: 28, opacity: 0.3, marginBottom: 12 }}>◌</div>
              <div style={{ color: T.textMuted, fontSize: 13, marginBottom: 16 }}>Nema prijava</div>
              <button onClick={() => navigate("/nova")} className="btn-prim" style={{ fontSize: 12, padding: "7px 16px" }}>
                <Icon.Plus/> Kreirajte prvu prijavu
              </button>
            </div>
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
                  gridTemplateColumns: "1fr 100px 90px", gap: 12, height: 52,
                  animation: `fadeUp 0.25s ease ${i * 0.04}s both`,
                  cursor: "pointer",
                }} onClick={() => navigate(`/prijave/${p.id}`)}>
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

        <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          <div className="card" style={{ padding: "18px 22px" }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 14 }}>Po kategorijama</div>
            {loading ? (
              <div style={{ display: "flex", justifyContent: "center", padding: 24 }}><Spinner/></div>
            ) : catList.length === 0 ? (
              <p style={{ color: T.textMuted, fontSize: 13 }}>Nema podataka</p>
            ) : catList.map(([cat, count], i) => {
              const pct = s.ukupno ? Math.round((count/s.ukupno)*100) : 0;
              const col = catColors[i % catColors.length];
              return (
                <div key={cat} style={{ marginBottom: 12, animation: `fadeUp 0.25s ease ${i*0.05}s both` }}>
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
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

          {!loading && prijave.length > 0 && (
            <div className="card" style={{ padding: "18px 22px" }}>
              <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 14 }}>Po prioritetu</div>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
                {[
                  { label: "Hitno",   key: "HITNO",   color: "#E74C3C" },
                  { label: "Visok",   key: "VISOK",   color: "#E67E22" },
                  { label: "Srednji", key: "SREDNJI", color: "#F39C12" },
                  { label: "Nizak",   key: "NIZAK",   color: "#2ECC71" },
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
