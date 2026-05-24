import { useState, useEffect, useCallback, useRef } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

function DonutChart({ segments, size = 110 }) {
  const r = 38, cx = size/2, cy = size/2, circ = 2*Math.PI*r;
  const total = segments.reduce((s,x) => s+x.value, 0);
  let offset = 0;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <circle cx={cx} cy={cy} r={r} fill="none" stroke={T.bgActive} strokeWidth={14} />
      {segments.map((seg, i) => {
        const pct = total ? seg.value/total : 0;
        const dash = circ*pct;
        const el = (
          <circle key={i} cx={cx} cy={cy} r={r} fill="none" stroke={seg.color}
            strokeWidth={14} strokeDasharray={`${dash} ${circ-dash}`} strokeDashoffset={-offset}
            style={{ transform:`rotate(-90deg)`, transformOrigin:`${cx}px ${cy}px` }} />
        );
        offset += dash;
        return el;
      })}
      <text x={cx} y={cy+1} textAnchor="middle" dominantBaseline="middle"
        style={{ fontSize:16, fontWeight:700, fill:T.text, fontFamily:"inherit" }}>{total}</text>
      <text x={cx} y={cy+14} textAnchor="middle" dominantBaseline="middle"
        style={{ fontSize:8, fill:T.textMuted, fontFamily:"inherit" }}>ukupno</text>
    </svg>
  );
}

function BarChart({ data, maxVal, color }) {
  if (!data || data.length === 0)
    return <p style={{ color:T.textMuted, fontSize:12 }}>Nema podataka</p>;
  return (
    <div style={{ display:"flex", alignItems:"flex-end", gap:4, height:90, paddingTop:20, boxSizing:"border-box" }}>
      {data.map(([label, value], i) => {
        const h = maxVal ? Math.round((value / maxVal) * 65) : 0;
        return (
          <div key={i} style={{ flex:1, display:"flex", flexDirection:"column",
            alignItems:"center", gap:0, height:"100%", justifyContent:"flex-end" }}
            title={`${label}: ${value}`}>
           
            <span style={{ fontSize:9, color:T.textMuted, marginBottom:2,
              fontFamily:"'Geist Mono',monospace" }}>{value > 0 ? value : ""}</span>
            
            <div style={{ width:"100%", height:h||2, background:color||T.blue,
              borderRadius:"3px 3px 0 0", minHeight:2 }} />
          </div>
        );
      })}
    </div>
  );
}

function BarLabels({ data }) {
  if (!data || data.length === 0) return null;
  return (
    <div style={{ display:"flex", gap:4, marginTop:4, paddingBottom:4 }}>
      {data.map(([label], i) => (
        <div key={i} style={{ flex:1, textAlign:"center" }}>
          <span style={{ fontSize:9, color:T.textMuted, display:"block",
            overflow:"hidden", textOverflow:"ellipsis", whiteSpace:"nowrap" }}
            title={label}>
            {label.length > 6 ? label.slice(0,5)+"…" : label}
          </span>
        </div>
      ))}
    </div>
  );
}

function LineChart({ data, color }) {
  if (!data || data.length < 2) return <p style={{ color:T.textMuted, fontSize:12 }}>Nema podataka</p>;
  const W=280, H=80;
  const values = data.map(d => d[1]);
  const max = Math.max(...values, 1);
  const points = data.map(([,v],i) => {
    const x = (i/(data.length-1))*(W-20)+10;
    const y = H-10-((v/max)*(H-20));
    return `${x},${y}`;
  }).join(" ");
  const areaPoints = [
    `10,${H-10}`,
    ...data.map(([,v],i) => {
      const x = (i/(data.length-1))*(W-20)+10;
      const y = H-10-((v/max)*(H-20));
      return `${x},${y}`;
    }),
    `${W-10},${H-10}`,
  ].join(" ");
  return (
    <svg width="100%" viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none">
      <polygon points={areaPoints} fill={color||T.blue} opacity="0.12" />
      <polyline points={points} fill="none" stroke={color||T.blue} strokeWidth="2"
        strokeLinecap="round" strokeLinejoin="round" />
      {data.map(([,v],i) => {
        const x = (i/(data.length-1))*(W-20)+10;
        const y = H-10-((v/max)*(H-20));
        return <circle key={i} cx={x} cy={y} r="3" fill={color||T.blue} />;
      })}
    </svg>
  );
}

function AktivneMapaFinal({ prijave }) {
  const [divEl, setDivEl] = useState(null);
  const mapRef = useRef(null);

  useEffect(() => {
    if (!divEl) return;
    if (mapRef.current) return;

    const loadAndInit = () => {
      const L = window.L;
      if (!L) return;
      const map = L.map(divEl, {zoomControl:true}).setView([43.8563, 18.4131], 12);
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution:"© OpenStreetMap", maxZoom:19,
      }).addTo(map);

      prijave.forEach(p => {
        if (!p.latitude || !p.longitude) return;
        const color = p.prioritet==="HITNO"?"#E74C3C":p.prioritet==="VISOK"?"#E67E22":p.prioritet==="SREDNJI"?"#F39C12":"#2ECC71";
        const icon = L.divIcon({
          html:`<div style="width:14px;height:14px;border-radius:50%;background:${color};border:2px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,0.5);"></div>`,
          iconSize:[14,14], iconAnchor:[7,7], className:"",
        });
        L.marker([p.latitude, p.longitude], {icon}).addTo(map)
          .bindPopup(`<b>${p.naslov||"Prijava"}</b><br/>${p.adresa||""}<br/><small>Status: ${p.statusNaziv||"—"}</small>`);
      });
      mapRef.current = map;
    };

    if (!document.getElementById("leaflet-css")) {
      const link = document.createElement("link");
      link.id="leaflet-css"; link.rel="stylesheet";
      link.href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
      document.head.appendChild(link);
    }
    if (window.L) { loadAndInit(); }
    else if (!document.querySelector('script[src*="leaflet@"]')) {
      const s = document.createElement("script");
      s.src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
      s.onload=loadAndInit;
      document.head.appendChild(s);
    } else {
      const iv = setInterval(() => { if (window.L) { clearInterval(iv); loadAndInit(); } }, 100);
    }
    return () => { if (mapRef.current) { mapRef.current.remove(); mapRef.current=null; } };
  }, [divEl]);

  return (
    <div ref={el => { if (el && el!==divEl) setDivEl(el); }}
      style={{ width:"100%", height:280, borderRadius:10, border:`1px solid ${T.line}`,
        overflow:"hidden", background:T.bgRaised }} />
  );
}

export default function AdminDashboardPage() {
  const { showToast } = useAuth();
  const [stat, setStat] = useState(null);
  const [prijave, setPrijave] = useState([]);
  const [sluzbe, setSluzbe] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [statData, prijaveData, sluzbeData] = await Promise.all([
        apiCall("/api/prijave/statistika"),
        apiCall("/api/prijave"),
        apiCall("/api/sluzbe").catch(() => []),
      ]);
      setStat(statData);
      setPrijave(Array.isArray(prijaveData) ? prijaveData : []);
      setSluzbe(Array.isArray(sluzbeData) ? sluzbeData : []);
    } catch (err) {
      showToast("Greška pri učitavanju dashboard podataka.", "error");
      console.error(err);
    } finally { setLoading(false); }
  }, [showToast]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const prekoraceni = prijave.filter(p =>
    p.datumRoka && new Date(p.datumRoka) < new Date() &&
    p.statusNaziv !== "Rijeseno" && p.statusNaziv !== "Zatvoreno"
  );

  const opterecenje = {};
  prijave.forEach(p => {
    if (p.grdSluzbald) opterecenje[p.grdSluzbald] = (opterecenje[p.grdSluzbald]||0)+1;
  });

  const aktivne = prijave.filter(p => p.statusNaziv!=="Rijeseno" && p.statusNaziv!=="Zatvoreno");

  const statusSegments = stat
    ? Object.entries(stat.poStatusima||{}).map(([name,value],i) => ({
        name, value, color:["#F39C12","#9B59B6","#3498DB","#2ECC71","#95A5A6"][i%5],
      }))
    : [];

  const catBarData = stat
    ? Object.entries(stat.poKategorijama||{}).sort((a,b)=>b[1]-a[1]).slice(0,8)
    : [];
  const catBarMax = catBarData.length ? Math.max(...catBarData.map(d=>d[1]),1) : 1;

  const mesecniData = stat ? Object.entries(stat.poMjesecima||{}).slice(-12) : [];

  const avgRaw = stat?.prosjecnoVrijemeRjesavanjaH;
  const avgDisplay = avgRaw && avgRaw > 0 ? `${Math.round(avgRaw)}h` : "N/A";

  return (
    <div style={{ animation:"fadeIn 0.3s ease" }}>
      <PageHeader
        title="Admin Dashboard"
        sub="Centralni pregled sistema"
        action={
          <button onClick={fetchData} disabled={loading} className="btn-ghost" style={{ fontSize:12, padding:"7px 12px" }}>
            {loading ? <Spinner size={14}/> : <Icon.Refresh/>} Osvježi
          </button>
        }
      />

      {prekoraceni.length > 0 && (
        <div style={{ background:T.redDim, border:`1px solid ${T.redBorder}`, borderRadius:10,
          padding:"14px 20px", marginBottom:20, display:"flex", alignItems:"center", gap:12 }}>
          <span style={{ color:T.red, fontSize:18, flexShrink:0 }}>⚠</span>
          <div>
            <div style={{ fontSize:13, fontWeight:600, color:T.red }}>
              {prekoraceni.length} prijav{prekoraceni.length===1?"a":"e"} ima prekoračen rok!
            </div>
            <div style={{ fontSize:12, color:T.textSub, marginTop:2 }}>
              {prekoraceni.slice(0,3).map(p=>p.naslov).join(", ")}
              {prekoraceni.length>3 && ` i još ${prekoraceni.length-3}...`}
            </div>
          </div>
        </div>
      )}


      <div style={{ display:"grid", gridTemplateColumns:"repeat(5,1fr)", gap:12, marginBottom:20 }}>
        {[
          {label:"Ukupno prijava",       val:stat?.ukupnoPrijava??  "—", color:T.text},
          {label:"Aktivne",              val:stat?.aktivnePrijave?? "—", color:"#3498DB"},
          {label:"Arhivirane",           val:stat?.arhiviranePrijave??"—",color:T.textSub},
          {label:"Prekoračeni rokovi",   val:stat?.prekoraceniRokovi??"—",color:T.red},
          {label:"Avg. rješavanje",      val:loading?"—":avgDisplay,    color:T.amber},
        ].map(c => (
          <div key={c.label} className="card" style={{ padding:"16px 20px" }}>
            <div style={{ fontSize:24, fontWeight:700, color:c.color,
              fontFamily:"'Geist Mono',monospace", letterSpacing:"-0.03em" }}>
              {loading ? "—" : c.val}
            </div>
            <div style={{ fontSize:11, color:T.textMuted, marginTop:4 }}>{c.label}</div>
          </div>
        ))}
      </div>

      <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:16, marginBottom:16 }}>
        <div className="card" style={{ padding:"20px 24px" }}>
          <div style={{ fontSize:13, fontWeight:500, color:T.text, marginBottom:16 }}>Raspodjela po statusima</div>
          {loading ? <div style={{ display:"flex", justifyContent:"center", padding:24 }}><Spinner size={24}/></div> : (
            <div style={{ display:"flex", gap:24, alignItems:"center" }}>
              <DonutChart segments={statusSegments} size={110} />
              <div style={{ flex:1 }}>
                {statusSegments.map(s => (
                  <div key={s.name} style={{ display:"flex", alignItems:"center", gap:8, marginBottom:8 }}>
                    <div style={{ width:10, height:10, borderRadius:2, background:s.color, flexShrink:0 }} />
                    <span style={{ fontSize:12, color:T.textSub, flex:1 }}>{s.name}</span>
                    <span style={{ fontSize:12, color:T.text, fontFamily:"'Geist Mono',monospace" }}>{s.value}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="card" style={{ padding:"20px 24px" }}>
          <div style={{ fontSize:13, fontWeight:500, color:T.text, marginBottom:8 }}>Prijave po kategorijama</div>
          {loading ? <div style={{ display:"flex", justifyContent:"center", padding:24 }}><Spinner size={24}/></div> : (
            <>
              <BarChart data={catBarData} maxVal={catBarMax} color="#3498DB" />
              <BarLabels data={catBarData} />
            </>
          )}
        </div>
      </div>

      <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:16, marginBottom:16 }}>
        <div className="card" style={{ padding:"20px 24px" }}>
          <div style={{ fontSize:13, fontWeight:500, color:T.text, marginBottom:4 }}>Trend (posljednjih 12 mj.)</div>
          <div style={{ fontSize:11, color:T.textMuted, marginBottom:12 }}>Broj novih prijava po mjesecu</div>
          {loading ? <div style={{ display:"flex", justifyContent:"center", padding:24 }}><Spinner size={24}/></div> : (
            <>
              <LineChart data={mesecniData} color="#2ECC71" />
              <div style={{ display:"flex", justifyContent:"space-between", marginTop:4, fontSize:9, color:T.textMuted }}>
                {mesecniData.slice(0,1).map(([l]) => <span key={l}>{l}</span>)}
                {mesecniData.slice(-1).map(([l]) => <span key={l}>{l}</span>)}
              </div>
            </>
          )}
        </div>
        <div className="card" style={{ padding:"20px 24px" }}>
          <div style={{ fontSize:13, fontWeight:500, color:T.text, marginBottom:16 }}>Opterećenje službi</div>
          {loading ? <div style={{ display:"flex", justifyContent:"center", padding:24 }}><Spinner size={24}/></div>
          : Object.keys(opterecenje).length === 0 ? (
            <p style={{ fontSize:13, color:T.textMuted }}>Nema podataka o dodjeli službi.</p>
          ) : (
            Object.entries(opterecenje).sort((a,b)=>b[1]-a[1]).map(([sluzbaId, count]) => {
              const sluzba = sluzbe.find(s => String(s.id)===String(sluzbaId));
              const naziv = sluzba?.naziv || `Služba #${sluzbaId}`;
              const maxCount = Math.max(...Object.values(opterecenje));
              const pct = Math.round((count/maxCount)*100);
              const color = pct>75?T.red:pct>50?T.amber:"#3498DB";
              return (
                <div key={sluzbaId} style={{ marginBottom:12 }}>
                  <div style={{ display:"flex", justifyContent:"space-between", marginBottom:4 }}>
                    <span style={{ fontSize:12, color:T.textSub }}>{naziv}</span>
                    <span style={{ fontSize:12, color:T.text, fontFamily:"'Geist Mono',monospace" }}>
                      {count} prijav{count!==1?"a":""}
                    </span>
                  </div>
                  <div style={{ height:5, background:T.bgActive, borderRadius:3, overflow:"hidden" }}>
                    <div style={{ height:"100%", width:`${pct}%`, background:color, borderRadius:3, transition:"width 0.6s ease" }} />
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      <div className="card" style={{ padding:"20px 24px", marginBottom:16 }}>
        <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:12 }}>
          <div>
            <div style={{ fontSize:13, fontWeight:500, color:T.text }}>Mapa aktivnih problema</div>
            <div style={{ fontSize:11, color:T.textMuted, marginTop:2 }}>
              {aktivne.length} aktivnih prijava
            </div>
          </div>
          <div style={{ display:"flex", gap:12, fontSize:11, color:T.textMuted }}>
            {[{label:"Hitno",color:"#E74C3C"},{label:"Visok",color:"#E67E22"},{label:"Srednji",color:"#F39C12"},{label:"Nizak",color:"#2ECC71"}]
              .map(p => (
                <div key={p.label} style={{ display:"flex", alignItems:"center", gap:4 }}>
                  <div style={{ width:8, height:8, borderRadius:"50%", background:p.color }} />
                  {p.label}
                </div>
              ))}
          </div>
        </div>
        {loading ? (
          <div style={{ height:280, display:"flex", alignItems:"center", justifyContent:"center",
            background:T.bgRaised, borderRadius:10, border:`1px solid ${T.line}` }}>
            <Spinner size={28}/>
          </div>
        ) : (
          <AktivneMapaFinal prijave={aktivne.filter(p=>p.latitude&&p.longitude)} />
        )}
      </div>

      {prekoraceni.length > 0 && (
        <div className="card" style={{ overflow:"hidden" }}>
          <div style={{ padding:"14px 20px", borderBottom:`1px solid ${T.line}`,
            display:"flex", alignItems:"center", gap:8 }}>
            <span style={{ color:T.red }}>⚠</span>
            <span style={{ fontSize:13, fontWeight:500, color:T.text }}>
              Prijave s prekoračenim rokom ({prekoraceni.length})
            </span>
          </div>
          {prekoraceni.slice(0,10).map((p, i) => {
            const daysLate = Math.floor((new Date()-new Date(p.datumRoka))/(1000*60*60*24));
            return (
              <div key={p.id} style={{ display:"grid", gridTemplateColumns:"1fr 140px 100px 120px",
                padding:"10px 20px",
                borderBottom:i<prekoraceni.length-1?`1px solid ${T.line}`:"none",
                alignItems:"center", gap:12 }}>
                <div>
                  <div style={{ fontSize:13, color:T.text, fontWeight:500 }}>{p.naslov}</div>
                  <div style={{ fontSize:11, color:T.textMuted }}>{p.adresa||"—"}</div>
                </div>
                <div style={{ fontSize:11, color:T.textSub }}>
                  Rok: {new Date(p.datumRoka).toLocaleDateString("bs")}
                </div>
                <div style={{ fontSize:11, color:T.red, fontWeight:600 }}>+{daysLate}d kašnjenja</div>
                <div style={{ fontSize:10, padding:"3px 8px", borderRadius:4,
                  background:T.amberDim, border:`1px solid ${T.amberBorder}`,
                  color:T.amber, textAlign:"center", fontWeight:600 }}>
                  {p.statusNaziv}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
