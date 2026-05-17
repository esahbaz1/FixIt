import { useState, useEffect } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { PRIO_CFG } from "../api/constants";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import { StatusChip, PrioChip } from "../components/Chips";
import Icon from "../components/Icon";

export default function PrijaveListPage({ onView }) {
  const [prijave, setPrijave] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [fStatus, setFStatus] = useState("Sve");
  const [fPrio, setFPrio] = useState("Sve");
  const [sortBy, setSortBy] = useState("datum_desc");

  useEffect(() => {
    apiCall("/api/prijave")
      .then(data => setPrijave(Array.isArray(data) ? data : []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filtered = prijave
    .filter(p => {
      if (fStatus !== "Sve" && p.statusNaziv !== fStatus) return false;
      if (fPrio   !== "Sve" && p.prioritet !== fPrio)  return false;
      if (search) {
        const q = search.toLowerCase();
        if (!p.naslov?.toLowerCase().includes(q) && !p.adresa?.toLowerCase().includes(q) && !p.nazivKategorije?.toLowerCase().includes(q)) return false;
      }
      return true;
    })
    .sort((a, b) => {
      if (sortBy === "datum_desc") return new Date(b.datumPodnosenja) - new Date(a.datumPodnosenja);
      if (sortBy === "datum_asc")  return new Date(a.datumPodnosenja) - new Date(b.datumPodnosenja);
      if (sortBy === "prio") {
        const ord = { HITNO:0, VISOK:1, SREDNJI:2, NIZAK:3 };
        return (ord[a.prioritet]??4) - (ord[b.prioritet]??4);
      }
      return 0;
    });

  const cols = "2fr 1fr 100px 90px 120px 24px";

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader
        title="Prijave"
        sub={`${filtered.length} od ${prijave.length} prijava`}
      />

      {/* Toolbar */}
      <div className="card" style={{ padding: "12px 16px", marginBottom: 16 }}>
        <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
          <div style={{ position: "relative", flex: "0 0 220px" }}>
            <span style={{ position: "absolute", left: 10, top: "50%", transform: "translateY(-50%)", color: T.textMuted, pointerEvents: "none" }}>
              <Icon.Search/>
            </span>
            <input
              value={search} onChange={e => setSearch(e.target.value)}
              placeholder="Pretraži..."
              className="input-field"
              style={{ paddingLeft: 32, fontSize: 12 }}
            />
          </div>

          <div className="divider"/>

          <div style={{ display: "flex", gap: 4, flexWrap: "wrap" }}>
            {["Sve","Novo","Dodijeljeno","U radu","Rijeseno","Zatvoreno"].map(s => (
              <button key={s} onClick={() => setFStatus(s)} className={`filter-chip ${fStatus === s ? "active" : ""}`}>{s}</button>
            ))}
          </div>

          <div className="divider"/>

          <div style={{ display: "flex", gap: 4 }}>
            {["Sve","HITNO","VISOK","SREDNJI","NIZAK"].map(p => (
              <button key={p} onClick={() => setFPrio(p)} className={`filter-chip ${fPrio === p ? "active" : ""}`} style={
                fPrio === p && p !== "Sve" ? {
                  background: PRIO_CFG[p]?.dim,
                  borderColor: PRIO_CFG[p]?.color + "60",
                  color: PRIO_CFG[p]?.color,
                } : {}
              }>
                {p === "Sve" ? "Sve" : p.charAt(0) + p.slice(1).toLowerCase()}
              </button>
            ))}
          </div>

          <div className="divider"/>

          <select value={sortBy} onChange={e => setSortBy(e.target.value)} className="input-field" style={{ width: "auto", fontSize: 12, padding: "5px 32px 5px 10px" }}>
            <option value="datum_desc">Najnovije</option>
            <option value="datum_asc">Najstarije</option>
            <option value="prio">Prioritet</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spinner size={32}/></div>
      ) : filtered.length === 0 ? (
        <div style={{ textAlign: "center", padding: 80, color: T.textMuted, fontSize: 13 }}>
          <div style={{ fontSize: 32, marginBottom: 12, opacity: 0.3 }}>◌</div>
          Nema pronađenih prijava
        </div>
      ) : (
        <div className="card" style={{ overflow: "hidden" }}>
          <div style={{
            display: "grid", gridTemplateColumns: cols, gap: 12,
            padding: "10px 24px",
            background: T.bgRaised, borderBottom: `1px solid ${T.line}`,
            fontSize: 11, fontWeight: 500, color: T.textMuted, letterSpacing: "0.05em", textTransform: "uppercase",
          }}>
            <span>Naslov</span><span>Kategorija</span><span>Status</span><span>Prioritet</span><span>Datum</span><span/>
          </div>

          {filtered.map((p, i) => (
            <div key={p.id} onClick={() => onView(p)} className="tbl-row row-enter" style={{
              gridTemplateColumns: cols, gap: 12, height: 56,
              animationDelay: `${Math.min(i * 0.02, 0.4)}s`,
            }}>
              <div style={{ overflow: "hidden" }}>
                <div style={{ fontSize: 13, fontWeight: 500, color: T.text, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{p.naslov}</div>
                {p.adresa && <div style={{ fontSize: 11, color: T.textMuted, display: "flex", alignItems: "center", gap: 3, marginTop: 2 }}><Icon.Pin/>{p.adresa}</div>}
              </div>
              <span style={{ fontSize: 12, color: T.textSub }}>{p.nazivKategorije || "—"}</span>
              <StatusChip status={p.statusNaziv}/>
              <PrioChip priority={p.prioritet}/>
              <span style={{ fontSize: 11, color: T.textMuted, display: "flex", alignItems: "center", gap: 4 }}>
                <Icon.Clock/>
                {p.datumPodnosenja ? new Date(p.datumPodnosenja).toLocaleDateString("bs") : "—"}
              </span>
              <span style={{ color: T.textMuted, display: "flex", alignItems: "center" }}><Icon.ChevRight/></span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
