import { useState, useEffect, useCallback, useRef } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

const PALETTE = ["#2ECC71","#3498DB","#9B59B6","#F39C12","#E67E22","#E74C3C","#1ABC9C","#E91E63"];
const STATUS_COLORS = {
  "Novo":"#F39C12","Dodijeljeno":"#9B59B6","U radu":"#3498DB","Rijeseno":"#2ECC71","Zatvoreno":"#95A5A6",
};


function AnimatedBar({ value, max, color, delay = 0 }) {
  const [width, setWidth] = useState(0);
  useEffect(() => {
    const t = setTimeout(() => setWidth(max ? Math.round((value / max) * 100) : 0), delay);
    return () => clearTimeout(t);
  }, [value, max, delay]);
  return (
    <div style={{ height: 6, background: "rgba(255,255,255,0.06)", borderRadius: 3, overflow: "hidden" }}>
      <div style={{ height:"100%", width:`${width}%`, background:color, borderRadius:3, transition:"width 0.9s cubic-bezier(0.4,0,0.2,1)" }} />
    </div>
  );
}

function HBarRow({ label, value, max, total, color, index }) {
  const pct = total ? Math.round((value / total) * 100) : 0;
  return (
    <div style={{ marginBottom: 14, animation:`fadeUp 0.3s ease ${index*0.06}s both` }}>
      <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:5 }}>
        <div style={{ display:"flex", alignItems:"center", gap:8 }}>
          <div style={{ width:8, height:8, borderRadius:2, background:color, flexShrink:0 }} />
          <span style={{ fontSize:12, color:T.textSub }}>{label}</span>
        </div>
        <div style={{ display:"flex", gap:10, alignItems:"center" }}>
          <span style={{ fontSize:10, color:T.textMuted, background:"rgba(255,255,255,0.04)", padding:"1px 6px", borderRadius:10 }}>{pct}%</span>
          <span style={{ fontSize:12, color:T.text, fontFamily:"'Geist Mono',monospace", minWidth:24, textAlign:"right" }}>{value}</span>
        </div>
      </div>
      <AnimatedBar value={value} max={max} color={color} delay={index*60+100} />
    </div>
  );
}

function DonutChart({ segments, size = 120 }) {
  const r = 44, cx = size/2, cy = size/2, circ = 2*Math.PI*r;
  const total = segments.reduce((s,x) => s+x.value, 0);
  let offset = 0;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <circle cx={cx} cy={cy} r={r} fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth={16} />
      {segments.map((seg, i) => {
        const pct = total ? seg.value/total : 0;
        const dash = circ*pct;
        const el = (
          <circle key={i} cx={cx} cy={cy} r={r} fill="none" stroke={seg.color}
            strokeWidth={16} strokeDasharray={`${dash} ${circ-dash}`} strokeDashoffset={-offset}
            style={{ transform:`rotate(-90deg)`, transformOrigin:`${cx}px ${cy}px` }} />
        );
        offset += dash;
        return el;
      })}
      <text x={cx} y={cy-6} textAnchor="middle" dominantBaseline="middle"
        style={{ fontSize:20, fontWeight:700, fill:T.text, fontFamily:"inherit" }}>{total}</text>
      <text x={cx} y={cy+12} textAnchor="middle" dominantBaseline="middle"
        style={{ fontSize:9, fill:T.textMuted, fontFamily:"inherit" }}>prijava</text>
    </svg>
  );
}

function TrendChart({ data, color = "#2ECC71" }) {
  if (!data || data.length === 0) return (
    <div style={{ position:"absolute", inset:0, display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center", gap:8 }}>
      <div style={{ fontSize:28, opacity:0.25 }}>📈</div>
      <p style={{ color:"#6FA882", fontSize:12, textAlign:"center" }}>Nema podataka za odabrani period</p>
    </div>
  );
  if (data.length === 1) return (
    <div style={{ position:"absolute", inset:0, display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center", gap:8 }}>
      <div style={{ fontSize:32, fontWeight:700, color }}>{data[0][1]}</div>
      <div style={{ fontSize:11, color:"#6FA882" }}>{data[0][0]}</div>
      <p style={{ color:"#6FA882", fontSize:11 }}>Samo jedan mjesec podataka</p>
    </div>
  );
  const W=100, H=100;
  const values = data.map(d => d[1]);
  const max = Math.max(...values, 1);
  const min = Math.min(...values, 0);
  const range = max - min || 1;
  const pts = data.map(([,v],i) => [(i/(data.length-1))*(W-4)+2, H-8-((v-min)/range*(H-24)+4)]);
  const polyPts = pts.map(p => p.join(",")).join(" ");
  const areaPts = [`2,${H-4}`, ...pts.map(p => p.join(",")), `${W-2},${H-4}`].join(" ");
  return (
    <svg width="100%" height="100%" viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none" style={{ display:"block", position:"absolute", top:0, left:0 }}>
      <defs>
        <linearGradient id="tg" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.3" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <polygon points={areaPts} fill="url(#tg)" />
      <polyline points={polyPts} fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      {pts.map(([x,y],i) => <circle key={i} cx={x} cy={y} r="2.5" fill={color} />)}
    </svg>
  );
}

function KPICard({ label, value, sub, color, icon }) {
  return (
    <div className="card" style={{ padding:"20px 22px", position:"relative", overflow:"hidden" }}>
      <div style={{ position:"absolute", top:0, left:0, right:0, height:2, background:color, opacity:0.7, borderRadius:"8px 8px 0 0" }} />
      <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start", marginBottom:10 }}>
        <div style={{ fontSize:11, fontWeight:500, color:T.textMuted, textTransform:"uppercase", letterSpacing:"0.07em" }}>{label}</div>
        <div style={{ width:28, height:28, borderRadius:7, background:`${color}20`, border:`1px solid ${color}40`,
          display:"flex", alignItems:"center", justifyContent:"center", fontSize:13, color }}>{icon}</div>
      </div>
      <div style={{ fontSize:32, fontWeight:700, color, letterSpacing:"-0.03em", fontVariantNumeric:"tabular-nums" }}>{value}</div>
      {sub && <div style={{ fontSize:11, color:T.textMuted, marginTop:5 }}>{sub}</div>}
    </div>
  );
}

function HeatmapMapa({ prijave }) {
  const [divEl, setDivEl] = useState(null);
  const mapRef = useRef(null);
  const heatRef = useRef(null);
  const [heatMode, setHeatMode] = useState("sve");
  const [showLegend, setShowLegend] = useState(true);

  const filtered = heatMode === "sve" ? prijave
    : prijave.filter(p => p.prioritet === heatMode.toUpperCase());

  const initHeat = useCallback((map, data) => {
    const L = window.L;
    if (!L || !L.heatLayer) return;
    const pts = data.filter(p => p.latitude && p.longitude).map(p => {
      const intensity = p.prioritet==="HITNO"?1.0 : p.prioritet==="VISOK"?0.7 : p.prioritet==="SREDNJI"?0.5 : 0.3;
      return [p.latitude, p.longitude, intensity];
    });
    if (heatRef.current) map.removeLayer(heatRef.current);
    heatRef.current = L.heatLayer(pts, {
      radius:35, blur:20, maxZoom:17,
      gradient:{0.2:"#2ECC71", 0.4:"#F39C12", 0.6:"#E67E22", 0.8:"#E74C3C", 1.0:"#8B0000"},
    }).addTo(map);
  }, []);

  useEffect(() => {
    if (!divEl) return;
    if (mapRef.current) return;

    const loadLeaflet = () => {
      if (!window.L) return;
      const L = window.L;
      const map = L.map(divEl, {zoomControl:true}).setView([43.8563, 18.4131], 13);
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution:"© OpenStreetMap", maxZoom:19,
      }).addTo(map);
      mapRef.current = map;

      const loadHeat = () => {
        if (!window.L.heatLayer) {
          const s = document.createElement("script");
          s.src = "https://unpkg.com/leaflet.heat@0.2.0/dist/leaflet-heat.js";
          s.onload = () => initHeat(map, prijave);
          document.head.appendChild(s);
        } else {
          initHeat(map, prijave);
        }
      };
      loadHeat();
    };

    if (!document.getElementById("leaflet-css")) {
      const link = document.createElement("link");
      link.id = "leaflet-css"; link.rel = "stylesheet";
      link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
      document.head.appendChild(link);
    }
    if (window.L) { loadLeaflet(); }
    else if (!document.querySelector('script[src*="leaflet@"]')) {
      const s = document.createElement("script");
      s.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
      s.onload = loadLeaflet;
      document.head.appendChild(s);
    } else {
      const iv = setInterval(() => { if (window.L) { clearInterval(iv); loadLeaflet(); } }, 100);
    }
    return () => { if (mapRef.current) { mapRef.current.remove(); mapRef.current=null; heatRef.current=null; } };
  }, [divEl]);

  useEffect(() => {
    if (!mapRef.current || !window.L?.heatLayer) return;
    initHeat(mapRef.current, filtered);
  }, [heatMode, filtered, initHeat]);

  const prikazanih = filtered.filter(p => p.latitude && p.longitude).length;

  return (
    <div>
      <div style={{ display:"flex", gap:8, marginBottom:12, flexWrap:"wrap", alignItems:"center" }}>
        <span style={{ fontSize:11, color:T.textMuted }}>Filter:</span>
        {[{v:"sve",label:"Sve"},{v:"hitno",label:"Hitno"},{v:"visok",label:"Visok"},{v:"srednji",label:"Srednji"},{v:"nizak",label:"Nizak"}]
          .map(f => (
            <button key={f.v} onClick={() => setHeatMode(f.v)}
              className={heatMode===f.v?"btn-prim":"btn-ghost"}
              style={{ fontSize:11, padding:"4px 10px" }}>{f.label}</button>
          ))}
        <span style={{ marginLeft:"auto", fontSize:11, color:T.textMuted }}>{prikazanih} lokacija</span>
      </div>

      <div style={{ position:"relative" }}>
        <div ref={el => { if (el && el!==divEl) setDivEl(el); }}
          style={{ width:"100%", height:400, borderRadius:10, border:`1px solid ${T.line}`,
            overflow:"hidden", background:T.bgRaised }} />

        {showLegend && (
          <div style={{ position:"absolute", bottom:40, right:12, zIndex:1000,
            background:"rgba(15,42,26,0.92)", backdropFilter:"blur(8px)",
            border:`1px solid ${T.line}`, borderRadius:10, padding:"12px 14px" }}>
            <div style={{ fontSize:10, fontWeight:600, color:T.textMuted, textTransform:"uppercase",
              letterSpacing:"0.07em", marginBottom:8 }}>Intenzitet</div>
            {[{color:"#2ECC71",label:"Nizak"},{color:"#F39C12",label:"Srednji"},
              {color:"#E67E22",label:"Visok"},{color:"#E74C3C",label:"Kritičan"},{color:"#8B0000",label:"Ekstremni"}]
              .map(({color,label}) => (
                <div key={label} style={{ display:"flex", alignItems:"center", gap:8, marginBottom:5 }}>
                  <div style={{ width:24, height:8, borderRadius:2, background:color }} />
                  <span style={{ fontSize:11, color:T.textSub }}>{label}</span>
                </div>
              ))}
          </div>
        )}
        <button onClick={() => setShowLegend(v => !v)}
          style={{ position:"absolute", bottom:40, left:52, zIndex:1000,
            background:"rgba(15,42,26,0.85)", border:`1px solid ${T.line}`,
            color:T.textSub, fontSize:11, padding:"5px 10px", borderRadius:6, cursor:"pointer" }}>
          {showLegend?"Sakrij legendu":"Prikaži legendu"}
        </button>
      </div>

      <div style={{ marginTop:16, display:"grid", gridTemplateColumns:"repeat(3,1fr)", gap:10 }}>
        {[
          {label:"Kritične (HITNO)", val:prijave.filter(p=>p.prioritet==="HITNO").length, color:"#E74C3C", sub:"max intenzitet"},
          {label:"Aktivnih lokacija", val:prijave.filter(p=>p.latitude&&p.longitude&&p.statusNaziv!=="Rijeseno").length, color:"#F39C12", sub:"s koordinatama"},
          {label:"Riješenih lokacija", val:prijave.filter(p=>p.statusNaziv==="Rijeseno"&&p.latitude).length, color:"#2ECC71", sub:"problem otklonjen"},
        ].map(item => (
          <div key={item.label} style={{ padding:"14px 16px", borderRadius:10,
            background:`${item.color}10`, border:`1px solid ${item.color}30` }}>
            <div style={{ fontSize:22, fontWeight:700, color:item.color, fontVariantNumeric:"tabular-nums" }}>{item.val}</div>
            <div style={{ fontSize:12, color:T.textSub, marginTop:2 }}>{item.label}</div>
            <div style={{ fontSize:10, color:T.textMuted, marginTop:1 }}>{item.sub}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

function exportPDF(filtriranePrijave, period, avgH, efikasnost) {
  const kategorije = {};
  filtriranePrijave.forEach(p => { const k=p.nazivKategorije||"Ostalo"; kategorije[k]=(kategorije[k]||0)+1; });
  const statusi = {};
  filtriranePrijave.forEach(p => { const s=p.statusNaziv||"—"; statusi[s]=(statusi[s]||0)+1; });
  const periodLabel = {"1":"1 mjesec","3":"3 mjeseca","6":"6 mjeseci","12":"1 godina","36":"Sve"}[period]||period;
  const rijesene = filtriranePrijave.filter(p=>p.statusNaziv==="Rijeseno").length;
  const COLORS = ["#2ECC71","#3498DB","#9B59B6","#F39C12","#E67E22","#E74C3C"];
  const SC = {"Novo":"#F39C12","Dodijeljeno":"#9B59B6","U radu":"#3498DB","Rijeseno":"#2ECC71","Zatvoreno":"#95A5A6"};

  const html = `<!DOCTYPE html><html><head><meta charset="UTF-8"/>
<title>FixIt Izvještaj</title>
<style>
*{margin:0;padding:0;box-sizing:border-box;}
body{font-family:'Segoe UI',Arial,sans-serif;color:#1a2e1a;background:#fff;padding:32px;font-size:13px;}
.hdr{background:linear-gradient(135deg,#0f2a1a,#1a4a2a);color:#fff;padding:28px 32px;border-radius:12px;margin-bottom:28px;}
.logo{font-size:26px;font-weight:800;color:#2ECC71;margin-bottom:4px;}
.sub{font-size:12px;color:rgba(255,255,255,0.55);}
.meta{font-size:10px;color:rgba(255,255,255,0.4);margin-top:8px;}
h2{font-size:14px;font-weight:600;color:#0f2a1a;margin:22px 0 12px;padding-bottom:6px;border-bottom:2px solid #2ECC71;}
.kpi-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:24px;}
.kpi{padding:14px 16px;border:1px solid #e0e0e0;border-radius:8px;border-top-width:3px;border-top-style:solid;}
.kpi .val{font-size:26px;font-weight:700;}
.kpi .lbl{font-size:10px;text-transform:uppercase;letter-spacing:.07em;color:#777;margin-top:2px;}
.kpi .sub{font-size:10px;color:#aaa;margin-top:4px;}
.two{display:grid;grid-template-columns:1fr 1fr;gap:20px;}
.bar-row{margin-bottom:10px;}
.bar-lbl{display:flex;justify-content:space-between;margin-bottom:3px;font-size:11px;color:#444;}
.bar-bg{height:6px;background:#e8f5e9;border-radius:3px;}
.bar-fill{height:100%;border-radius:3px;}
table{width:100%;border-collapse:collapse;font-size:12px;margin-top:8px;}
th{background:#f0f7f0;padding:7px 12px;text-align:left;font-size:10px;text-transform:uppercase;letter-spacing:.07em;color:#555;}
td{padding:7px 12px;border-bottom:1px solid #f0f0f0;}
.badge{display:inline-block;padding:2px 7px;border-radius:10px;font-size:10px;font-weight:600;}
.footer{margin-top:32px;padding-top:14px;border-top:1px solid #e0e0e0;font-size:10px;color:#aaa;text-align:center;}
@media print{body{padding:16px;}.hdr{-webkit-print-color-adjust:exact;print-color-adjust:exact;}}
</style></head><body>
<div class="hdr">
  <div class="logo">FixIt</div>
  <div class="sub">Statistički izvještaj komunalnih prijava</div>
  <div class="meta">Period: ${periodLabel} &nbsp;·&nbsp; Generisano: ${new Date().toLocaleString("bs")} &nbsp;·&nbsp; Prijava u periodu: ${filtriranePrijave.length}</div>
</div>
<h2>Ključni pokazatelji</h2>
<div class="kpi-grid">
  <div class="kpi" style="border-top-color:#3498DB"><div class="val" style="color:#3498DB">${filtriranePrijave.length}</div><div class="lbl">Ukupno prijava</div><div class="sub">u periodu</div></div>
  <div class="kpi" style="border-top-color:#2ECC71"><div class="val" style="color:#2ECC71">${efikasnost}%</div><div class="lbl">Efikasnost</div><div class="sub">${rijesene} riješenih</div></div>
  <div class="kpi" style="border-top-color:#F39C12"><div class="val" style="color:#F39C12">${avgH!==null?Math.round(avgH)+"h":"N/A"}</div><div class="lbl">Avg. rješavanje</div><div class="sub">prosječno vrijeme</div></div>
  <div class="kpi" style="border-top-color:#E74C3C"><div class="val" style="color:#E74C3C">${filtriranePrijave.filter(p=>p.prioritet==="HITNO").length}</div><div class="lbl">Hitne prijave</div><div class="sub">prioritet HITNO</div></div>
</div>
<div class="two">
<div>
<h2>Prijave po kategorijama</h2>
${Object.entries(kategorije).sort((a,b)=>b[1]-a[1]).map(([k,v],i)=>{
  const p=filtriranePrijave.length?Math.round(v/filtriranePrijave.length*100):0;
  return `<div class="bar-row"><div class="bar-lbl"><span>${k}</span><span>${v} (${p}%)</span></div>
  <div class="bar-bg"><div class="bar-fill" style="width:${p}%;background:${COLORS[i%COLORS.length]}"></div></div></div>`;
}).join("")}
</div>
<div>
<h2>Raspodjela po statusima</h2>
${Object.entries(statusi).sort((a,b)=>b[1]-a[1]).map(([s,v])=>{
  const p=filtriranePrijave.length?Math.round(v/filtriranePrijave.length*100):0;
  return `<div class="bar-row"><div class="bar-lbl"><span>${s}</span><span>${v} (${p}%)</span></div>
  <div class="bar-bg"><div class="bar-fill" style="width:${p}%;background:${SC[s]||'#ccc'}"></div></div></div>`;
}).join("")}
</div>
</div>
<h2>Detaljna tabela</h2>
<table><thead><tr><th>Kategorija</th><th>Ukupno</th><th>Riješeno</th><th>U toku</th><th>Čeka</th><th>% Riješeno</th></tr></thead><tbody>
${Object.entries(kategorije).sort((a,b)=>b[1]-a[1]).map(([kat,total])=>{
  const kp=filtriranePrijave.filter(p=>(p.nazivKategorije||"Ostalo")===kat);
  const r=kp.filter(p=>p.statusNaziv==="Rijeseno").length;
  const u=kp.filter(p=>["U radu","Dodijeljeno"].includes(p.statusNaziv)).length;
  const c=kp.filter(p=>p.statusNaziv==="Novo").length;
  const rp=total>0?Math.round(r/total*100):0;
  return `<tr><td><b>${kat}</b></td><td style="text-align:center">${total}</td><td style="text-align:center;color:#2ECC71">${r}</td><td style="text-align:center;color:#3498DB">${u}</td><td style="text-align:center;color:#F39C12">${c}</td><td style="text-align:center"><span class="badge" style="background:${rp>70?"#e8f5e9":"#fff3e0"};color:${rp>70?"#2ECC71":"#F39C12"}">${rp}%</span></td></tr>`;
}).join("")}
</tbody></table>
<div class="footer">FixIt — Sistem za upravljanje komunalnim prijavama · ${new Date().toLocaleDateString("bs")}</div>
</body></html>`;

  const win = window.open("","_blank","width=900,height=700");
  if (!win) { alert("Omogućite popup prozore za generisanje PDF-a"); return; }
  win.document.write(html);
  win.document.close();
  win.onload = () => setTimeout(() => win.print(), 400);
}


const TABS = [{id:"pregled",label:"Pregled"},{id:"kategorije",label:"Kategorije"},{id:"trendovi",label:"Trendovi"},{id:"heatmap",label:"🗺 Heatmap"}];


export default function StatistikaPage() {
  const { showToast } = useAuth();
  const [stat, setStat] = useState(null);
  const [prijave, setPrijave] = useState([]);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState("12");
  const [activeTab, setActiveTab] = useState("pregled");

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [statData, prijaveData] = await Promise.all([
        apiCall("/api/prijave/statistika"),
        apiCall("/api/prijave"),
      ]);
      setStat(statData);
      setPrijave(Array.isArray(prijaveData) ? prijaveData : []);
    } catch (err) { showToast("Greška pri učitavanju statistike.", "error"); console.error(err); }
    finally { setLoading(false); }
  }, [showToast]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const cutoff = new Date();
  cutoff.setMonth(cutoff.getMonth() - parseInt(period));
  const fil = prijave.filter(p => !p.datumPodnosenja || new Date(p.datumPodnosenja) >= cutoff);

  const rijesene = fil.filter(p => p.datumZavrsetka && p.datumPodnosenja);
  const avgH = rijesene.length > 0
    ? rijesene.reduce((s,p) => s + Math.abs(new Date(p.datumZavrsetka)-new Date(p.datumPodnosenja))/3600000, 0) / rijesene.length
    : (stat?.prosjecnoVrijemeRjesavanjaH > 0 ? stat.prosjecnoVrijemeRjesavanjaH : null);

  const efikasnost = fil.length > 0 ? Math.round(fil.filter(p=>p.statusNaziv==="Rijeseno").length/fil.length*100) : 0;

  const poKat = {};
  fil.forEach(p => { const k=p.nazivKategorije||"Ostalo"; poKat[k]=(poKat[k]||0)+1; });
  const katEntries = Object.entries(poKat).sort((a,b)=>b[1]-a[1]);
  const katMax = katEntries.length ? Math.max(...katEntries.map(e=>e[1])) : 1;

  const poStat = {};
  fil.forEach(p => { const s=p.statusNaziv||"—"; poStat[s]=(poStat[s]||0)+1; });
  const statEntries = Object.entries(poStat).sort((a,b)=>b[1]-a[1]);
  const donutSegs = statEntries.map(([name,value]) => ({ name, value, color:STATUS_COLORS[name]||"#95A5A6" }));


  const mesecniData = (() => {
    if (!stat?.poMjesecima) return [];
    const sviMjeseci = Object.entries(stat.poMjesecima);
    const periodNum = parseInt(period);
    if (periodNum >= 36) return sviMjeseci; 
    return sviMjeseci.slice(-periodNum);
  })();
  const avgDisplay = avgH !== null ? `${Math.round(avgH)}h` : "N/A";

  return (
    <div style={{ animation:"fadeIn 0.3s ease" }}>
      <PageHeader
        title="Statistika i izvještaji"
        sub="Analiza podataka, trendovi i heatmap prikaz problema"
        action={
          <div style={{ display:"flex", gap:8 }}>
            <button onClick={fetchData} disabled={loading} className="btn-ghost" style={{ fontSize:12, padding:"7px 12px" }}>
              {loading ? <Spinner size={14}/> : <Icon.Refresh/>}
            </button>
            <button onClick={() => exportPDF(fil, period, avgH, efikasnost)} disabled={!stat||loading}
              className="btn-prim" style={{ fontSize:12, padding:"7px 14px", display:"flex", alignItems:"center", gap:6 }}>
              <span>📄</span> Izvezi PDF
            </button>
          </div>
        }
      />

      <div className="card" style={{ padding:"12px 20px", marginBottom:20, display:"flex", alignItems:"center", gap:16, flexWrap:"wrap" }}>
        <div style={{ display:"flex", gap:6 }}>
          {[{v:"1",l:"1 mj."},{v:"3",l:"3 mj."},{v:"6",l:"6 mj."},{v:"12",l:"1 god."},{v:"36",l:"Sve"}].map(p => (
            <button key={p.v} onClick={() => setPeriod(p.v)} className={period===p.v?"btn-prim":"btn-ghost"} style={{ fontSize:11, padding:"4px 12px" }}>{p.l}</button>
          ))}
        </div>
        <div style={{ width:1, height:20, background:T.line }} />
        <div style={{ display:"flex", gap:2 }}>
          {TABS.map(t => (
            <button key={t.id} onClick={() => setActiveTab(t.id)} className={activeTab===t.id?"btn-prim":"btn-ghost"} style={{ fontSize:11, padding:"4px 12px" }}>{t.label}</button>
          ))}
        </div>
        <span style={{ marginLeft:"auto", fontSize:11, color:T.textMuted }}>{fil.length} prijava</span>
      </div>

      {loading ? (
        <div style={{ display:"flex", justifyContent:"center", alignItems:"center", height:200 }}><Spinner size={32}/></div>
      ) : (
        <>
         
          {activeTab === "pregled" && (<>
            <div style={{ display:"grid", gridTemplateColumns:"repeat(4,1fr)", gap:12, marginBottom:20 }}>
              <KPICard label="Ukupno prijava" value={fil.length} sub="u odabranom periodu" color="#3498DB" icon="◆" />
              <KPICard label="Efikasnost" value={`${efikasnost}%`} sub={`${fil.filter(p=>p.statusNaziv==="Rijeseno").length} riješenih`} color="#2ECC71" icon="✓" />
              <KPICard label="Avg. rješavanje" value={avgDisplay} sub={rijesene.length>0?`${rijesene.length} zatvorenih`:"nema zatvorenih"} color="#F39C12" icon="⏱" />
              <KPICard label="Hitne prijave" value={fil.filter(p=>p.prioritet==="HITNO").length} sub="prioritet: HITNO" color="#E74C3C" icon="!" />
            </div>
            <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:16 }}>
              <div className="card" style={{ padding:"22px 24px" }}>
                <div style={{ fontSize:13, fontWeight:600, color:T.text, marginBottom:4 }}>Raspodjela po statusima</div>
                <div style={{ fontSize:11, color:T.textMuted, marginBottom:16 }}>Sve prijave u periodu</div>
                <div style={{ display:"flex", gap:24, alignItems:"center" }}>
                  <DonutChart segments={donutSegs} size={120} />
                  <div style={{ flex:1 }}>
                    {donutSegs.map((s,i) => (
                      <div key={s.name} style={{ display:"flex", alignItems:"center", gap:8, marginBottom:9, animation:`fadeUp 0.3s ease ${i*0.07}s both` }}>
                        <div style={{ width:10, height:10, borderRadius:2, background:s.color, flexShrink:0 }} />
                        <span style={{ fontSize:12, color:T.textSub, flex:1 }}>{s.name}</span>
                        <span style={{ fontSize:12, color:T.text, fontFamily:"'Geist Mono',monospace" }}>{s.value}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
              <div className="card" style={{ padding:"22px 24px" }}>
                <div style={{ fontSize:13, fontWeight:600, color:T.text, marginBottom:4 }}>Efikasnost rješavanja</div>
                <div style={{ fontSize:11, color:T.textMuted, marginBottom:16 }}>% prijava sa statusom Riješeno</div>
                <div style={{ display:"flex", alignItems:"center", gap:24 }}>
                  <svg width="110" height="110" viewBox="0 0 110 110">
                    <circle cx="55" cy="55" r="46" fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth="12" />
                    <circle cx="55" cy="55" r="46" fill="none"
                      stroke={efikasnost>70?"#2ECC71":efikasnost>40?"#F39C12":"#E74C3C"}
                      strokeWidth="12" strokeDasharray={`${efikasnost/100*289} 289`} strokeLinecap="round"
                      style={{ transform:"rotate(-90deg)", transformOrigin:"55px 55px", transition:"stroke-dasharray 1s ease" }} />
                    <text x="55" y="50" textAnchor="middle" dominantBaseline="middle"
                      style={{ fontSize:20, fontWeight:700, fill:T.text, fontFamily:"inherit" }}>{efikasnost}%</text>
                    <text x="55" y="65" textAnchor="middle" dominantBaseline="middle"
                      style={{ fontSize:9, fill:T.textMuted, fontFamily:"inherit" }}>riješeno</text>
                  </svg>
                  <div style={{ flex:1 }}>
                    {[
                      {label:"U toku", val:fil.filter(p=>["U radu","Dodijeljeno"].includes(p.statusNaziv)).length, color:"#3498DB"},
                      {label:"Čeka",   val:fil.filter(p=>p.statusNaziv==="Novo").length,          color:"#F39C12"},
                      {label:"Riješeno",val:fil.filter(p=>p.statusNaziv==="Rijeseno").length,     color:"#2ECC71"},
                      {label:"Zatvoreno",val:fil.filter(p=>p.statusNaziv==="Zatvoreno").length,   color:T.textSub},
                    ].map(item => (
                      <div key={item.label} style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:8 }}>
                        <div style={{ display:"flex", alignItems:"center", gap:6 }}>
                          <div style={{ width:6, height:6, borderRadius:"50%", background:item.color }} />
                          <span style={{ fontSize:12, color:T.textSub }}>{item.label}</span>
                        </div>
                        <span style={{ fontSize:13, fontWeight:600, color:item.color, fontFamily:"'Geist Mono',monospace" }}>{item.val}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </>)}

    
          {activeTab === "kategorije" && (
            <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:16 }}>
              <div className="card" style={{ padding:"22px 24px" }}>
                <div style={{ fontSize:13, fontWeight:600, color:T.text, marginBottom:4 }}>Prijave po kategorijama</div>
                <div style={{ fontSize:11, color:T.textMuted, marginBottom:18 }}>Distribucija prijava</div>
                {katEntries.length===0 ? <p style={{ color:T.textMuted, fontSize:13 }}>Nema podataka</p>
                  : katEntries.map(([k,v],i) => <HBarRow key={k} label={k} value={v} max={katMax} total={fil.length} color={PALETTE[i%PALETTE.length]} index={i} />)}
              </div>
              <div className="card" style={{ overflow:"hidden" }}>
                <div style={{ padding:"22px 24px 14px", borderBottom:`1px solid ${T.line}` }}>
                  <div style={{ fontSize:13, fontWeight:600, color:T.text }}>Detalji po kategorijama</div>
                </div>
                <div style={{ display:"grid", gridTemplateColumns:"1fr 55px 55px 55px 55px",
                  padding:"8px 20px", borderBottom:`1px solid ${T.line}`,
                  fontSize:10, fontWeight:600, color:T.textMuted, textTransform:"uppercase", letterSpacing:"0.07em" }}>
                  <span>Kategorija</span><span style={{ textAlign:"center" }}>Ukupno</span>
                  <span style={{ textAlign:"center", color:"#2ECC71" }}>✓</span>
                  <span style={{ textAlign:"center", color:"#3498DB" }}>⟳</span>
                  <span style={{ textAlign:"center", color:"#F39C12" }}>○</span>
                </div>
                {katEntries.map(([kat,total],i) => {
                  const kp = fil.filter(p=>(p.nazivKategorije||"Ostalo")===kat);
                  return (
                    <div key={kat} style={{ display:"grid", gridTemplateColumns:"1fr 55px 55px 55px 55px",
                      padding:"9px 20px", borderBottom:i<katEntries.length-1?`1px solid ${T.line}`:"none",
                      alignItems:"center", fontSize:12, animation:`fadeUp 0.25s ease ${i*0.04}s both` }}>
                      <div style={{ display:"flex", alignItems:"center", gap:7 }}>
                        <div style={{ width:7, height:7, borderRadius:2, background:PALETTE[i%PALETTE.length], flexShrink:0 }} />
                        <span style={{ color:T.textSub }}>{kat}</span>
                      </div>
                      <span style={{ textAlign:"center", color:T.text, fontFamily:"'Geist Mono',monospace" }}>{total}</span>
                      <span style={{ textAlign:"center", color:"#2ECC71", fontFamily:"'Geist Mono',monospace" }}>{kp.filter(p=>p.statusNaziv==="Rijeseno").length}</span>
                      <span style={{ textAlign:"center", color:"#3498DB", fontFamily:"'Geist Mono',monospace" }}>{kp.filter(p=>["U radu","Dodijeljeno"].includes(p.statusNaziv)).length}</span>
                      <span style={{ textAlign:"center", color:"#F39C12", fontFamily:"'Geist Mono',monospace" }}>{kp.filter(p=>p.statusNaziv==="Novo").length}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          
          {activeTab === "trendovi" && (
            <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:16 }}>
              <div className="card" style={{ padding:"22px 24px" }}>
                <div style={{ fontSize:13, fontWeight:600, color:T.text, marginBottom:4 }}>Trend novih prijava</div>
                <div style={{ fontSize:11, color:T.textMuted, marginBottom:16 }}>Broj prijava po mjesecu</div>
                <div style={{ height:160, marginBottom:8, position:"relative", overflow:"hidden" }}><TrendChart data={mesecniData} color="#2ECC71" /></div>
                <div style={{ display:"flex", justifyContent:"space-between", fontSize:9, color:T.textMuted, marginTop:4 }}>
                  {mesecniData.length > 0 && <span>{mesecniData[0][0]}</span>}
                  {mesecniData.length > 2 && <span>{mesecniData[Math.floor(mesecniData.length/2)][0]}</span>}
                  {mesecniData.length > 1 && <span>{mesecniData[mesecniData.length-1][0]}</span>}
                </div>
                <div style={{ marginTop:16, display:"grid", gridTemplateColumns:"repeat(3,1fr)", gap:8 }}>
                  {mesecniData.slice(-3).map(([mj,br]) => (
                    <div key={mj} style={{ padding:"10px 12px", background:T.bgRaised, border:`1px solid ${T.line}`, borderRadius:8 }}>
                      <div style={{ fontSize:18, fontWeight:700, color:"#2ECC71", fontVariantNumeric:"tabular-nums" }}>{br}</div>
                      <div style={{ fontSize:10, color:T.textMuted, marginTop:2 }}>{mj}</div>
                    </div>
                  ))}
                </div>
              </div>
              <div className="card" style={{ padding:"22px 24px" }}>
                <div style={{ fontSize:13, fontWeight:600, color:T.text, marginBottom:4 }}>Distribucija prioriteta</div>
                <div style={{ fontSize:11, color:T.textMuted, marginBottom:20 }}>Po nivou hitnosti</div>
                {[{label:"Hitno",key:"HITNO",color:"#E74C3C"},{label:"Visok",key:"VISOK",color:"#E67E22"},
                  {label:"Srednji",key:"SREDNJI",color:"#F39C12"},{label:"Nizak",key:"NIZAK",color:"#2ECC71"}]
                  .map((p,i) => (
                    <HBarRow key={p.key} label={p.label} value={fil.filter(x=>x.prioritet===p.key).length}
                      max={fil.length||1} total={fil.length} color={p.color} index={i} />
                  ))}
              </div>
            </div>
          )}

          
          {activeTab === "heatmap" && (
            <div className="card" style={{ padding:"22px 24px" }}>
              <div style={{ marginBottom:16 }}>
                <div style={{ fontSize:13, fontWeight:600, color:T.text, marginBottom:4 }}>Heatmap analiza problema</div>
                <div style={{ fontSize:11, color:T.textMuted }}>Vizualni prikaz gustine prijava — toplije zone = veći broj problema</div>
              </div>
              <HeatmapMapa prijave={fil} />
            </div>
          )}
        </>
      )}
    </div>
  );
}
