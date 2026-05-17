import { useState, useEffect } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import Spinner from "../components/Spinner";
import Toast from "../components/Toast";
import { StatusChip, PrioChip } from "../components/Chips";
import Icon from "../components/Icon";

export default function PrijavaDetailPage({ prijava: initialPrijava, onBack, onUpdated }) {
  const { user } = useAuth();
  const [prijava, setPrijava] = useState(initialPrijava);
  const [noviStatus, setNoviStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [komentarTekst, setKomentarTekst] = useState("");
  const [komentari, setKomentari] = useState([]);
  const [komentarLoading, setKomentarLoading] = useState(false);
  const [komentarSlanje, setKomentarSlanje] = useState(false);
  const [toast, setToast] = useState(null);
  const [korisnici, setKorisnici] = useState({});

  const statuses = ["Novo","Dodijeljeno","U radu","Rijeseno","Zatvoreno"];

  // Dohvati detalje prijave + komentare
  useEffect(() => {
    // Osvježi prijavu
    apiCall(`/api/prijave/${initialPrijava.id}`).then(p => setPrijava(p)).catch(() => {});
    // Dohvati komentare
    setKomentarLoading(true);
    apiCall(`/api/prijave/${initialPrijava.id}/komentari`)
      .then(data => {
        const lista = Array.isArray(data) ? data : [];
        setKomentari(lista);
        // Dohvati podatke o autorima komentara (sem trenutnog korisnika)
        const uniqueIds = [...new Set(lista.map(k => k.korisnikId).filter(id => id && id !== user?.id))];
        Promise.all(
          uniqueIds.map(id =>
            apiCall(`/api/korisnici/${id}`)
              .then(k => ({ id, ime: k.ime, prezime: k.prezime }))
              .catch(() => ({ id, ime: null, prezime: null }))
          )
        ).then(results => {
          const map = {};
          results.forEach(r => { map[r.id] = r; });
          setKorisnici(map);
        });
      })
      .catch(() => setKomentari([]))
      .finally(() => setKomentarLoading(false));
  }, [initialPrijava.id]);

  async function handleStatusChange() {
    if (!noviStatus) return;
    setLoading(true);
    try {
      await apiCall(
        `/api/prijave/${prijava.id}/status?noviStatus=${encodeURIComponent(noviStatus)}&korisnikId=${user?.id || 1}`,
        { method: "PATCH" }
      );
      setToast({ msg: `Status promijenjen u "${noviStatus}".`, type: "success" });
      setPrijava(p => ({ ...p, statusNaziv: noviStatus }));
      setNoviStatus("");
      setTimeout(() => onUpdated(`Status promijenjen u "${noviStatus}".`), 1000);
    } catch (err) {
      setToast({ msg: err.message, type: "error" });
    } finally {
      setLoading(false);
    }
  }

  async function handleKomentar(e) {
    e.preventDefault();
    if (!komentarTekst.trim()) return;
    setKomentarSlanje(true);
    try {
      const novi = await apiCall(`/api/prijave/${prijava.id}/komentari`, {
        method: "POST",
        body: JSON.stringify({ tekst: komentarTekst, korisnikId: user?.id }),
      });
      setKomentari(prev => [...prev, novi]);
      // Ako novi komentar ima korisnikId koji nismo dohvatili, dodaj u mapu
      if (novi.korisnikId && novi.korisnikId !== user?.id && !korisnici[novi.korisnikId]) {
        apiCall(`/api/korisnici/${novi.korisnikId}`)
          .then(k => setKorisnici(prev => ({ ...prev, [novi.korisnikId]: { id: novi.korisnikId, ime: k.ime, prezime: k.prezime } })))
          .catch(() => {});
      }
      setKomentarTekst("");
      setToast({ msg: "Komentar dodan.", type: "success" });
    } catch (err) {
      setToast({ msg: err.message, type: "error" });
    } finally {
      setKomentarSlanje(false);
    }
  }

  const fmt   = d => d ? new Date(d).toLocaleDateString("bs") : "—";
  const fmtDt = d => d ? new Date(d).toLocaleString("bs") : "—";

  return (
    <div style={{ animation: "fadeIn 0.25s ease" }}>
      {toast && <Toast message={toast.msg} type={toast.type} onDone={() => setToast(null)}/>}

      <button className="btn-ghost" onClick={onBack} style={{ marginBottom: 28, fontSize: 12, padding: "5px 12px" }}>
        <Icon.ChevLeft/> Nazad
      </button>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 300px", gap: 16 }}>
        {/* ─── Lijeva kolona ────────────────────────────────────────── */}
        <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>

          {/* Glavna kartica */}
          <div className="card" style={{ padding: 32 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 28 }}>
              <div>
                <h2 style={{ fontSize: 20, fontWeight: 600, color: T.text, letterSpacing: "-0.02em", marginBottom: 8 }}>
                  {prijava.naslov}
                </h2>
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
                { label: "Kategorija",    val: prijava.nazivKategorije || "—" },
                { label: "Prioritet",     val: <PrioChip priority={prijava.prioritet}/> },
                { label: "Korisnik ID",   val: `#${prijava.korisnikId}` },
                { label: "Datum prijave", val: fmtDt(prijava.datumPodnosenja) },
                { label: "Rok",           val: fmt(prijava.datumRoka) || "Nije postavljen" },
                { label: "Završeno",      val: fmt(prijava.datumZavrsetka) },
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
                background: "rgba(46,204,113,0.08)", border: "1px solid rgba(46,204,113,0.2)",
                borderRadius: 8, display: "flex", alignItems: "center", gap: 10,
              }}>
                <span style={{ color: "#2ECC71" }}><Icon.Pin/></span>
                <span style={{ fontSize: 12, color: T.textSub }}>GPS koordinate: </span>
                <code style={{ fontSize: 12, color: "#2ECC71", fontFamily: "'Geist Mono', monospace" }}>
                  {Number(prijava.latitude).toFixed(6)}, {Number(prijava.longitude).toFixed(6)}
                </code>
              </div>
            )}
          </div>

          {/* ─── Komentari ────────────────────────────────────────── */}
          <div className="card" style={{ overflow: "hidden" }}>
            <div style={{
              padding: "16px 24px", borderBottom: `1px solid ${T.line}`,
              display: "flex", alignItems: "center", gap: 10,
            }}>
              <Icon.Mail/>
              <span style={{ fontSize: 13, fontWeight: 500, color: T.text }}>Komentari</span>
              <span style={{
                fontSize: 11, color: T.textMuted,
                background: T.bgActive, padding: "1px 7px", borderRadius: 100,
              }}>{komentari.length}</span>
            </div>

            {/* Lista komentara */}
            {komentarLoading ? (
              <div style={{ display: "flex", justifyContent: "center", padding: 32 }}><Spinner/></div>
            ) : komentari.length === 0 ? (
              <div style={{ padding: "24px 24px", color: T.textMuted, fontSize: 13, textAlign: "center" }}>
                Još nema komentara. Budite prvi!
              </div>
            ) : (
              <div>
                {komentari.map((k, i) => (
                  <div key={k.id || i} style={{
                    padding: "14px 24px",
                    borderBottom: i < komentari.length - 1 ? `1px solid ${T.line}` : "none",
                    animation: `fadeUp 0.2s ease ${i * 0.03}s both`,
                  }}>
                    <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                      <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        <div style={{
                          width: 24, height: 24, borderRadius: "50%",
                          background: "rgba(46,204,113,0.15)", border: "1px solid rgba(46,204,113,0.3)",
                          display: "flex", alignItems: "center", justifyContent: "center",
                          fontSize: 10, fontWeight: 600, color: "#2ECC71",
                        }}>
                          {k.korisnikId === user?.id
                            ? (user?.ime?.[0] || "V").toUpperCase()
                            : (korisnici[k.korisnikId]?.ime?.[0] || "K").toUpperCase()}
                        </div>
                        <span style={{ fontSize: 12, fontWeight: 500, color: T.text }}>
                          {k.korisnikId === user?.id
                            ? (user?.ime || "Vi")
                            : korisnici[k.korisnikId]?.ime
                              ? `${korisnici[k.korisnikId].ime} ${korisnici[k.korisnikId].prezime || ""}`.trim()
                              : `Korisnik #${k.korisnikId}`}
                        </span>
                        {k.korisnikId === user?.id && (
                          <span style={{ fontSize: 10, color: "#2ECC71", padding: "1px 6px", borderRadius: 3, background: "rgba(46,204,113,0.1)", border: "1px solid rgba(46,204,113,0.2)" }}>
                            Vi
                          </span>
                        )}
                      </div>
                      <span style={{ fontSize: 11, color: T.textMuted }}>
                        {k.datumKreiranja ? new Date(k.datumKreiranja).toLocaleString("bs") : "—"}
                      </span>
                    </div>
                    <p style={{ fontSize: 13, color: T.textSub, lineHeight: 1.6, margin: 0, paddingLeft: 32 }}>
                      {k.tekst}
                    </p>
                  </div>
                ))}
              </div>
            )}

            {/* Forma za komentar */}
            <div style={{ padding: "16px 24px", borderTop: `1px solid ${T.line}` }}>
              <form onSubmit={handleKomentar} style={{ display: "flex", gap: 10 }}>
                <textarea
                  value={komentarTekst}
                  onChange={e => setKomentarTekst(e.target.value)}
                  placeholder="Dodajte komentar..."
                  rows={2}
                  className="input-field"
                  style={{ resize: "none", flex: 1, fontSize: 13 }}
                />
                <button type="submit" disabled={!komentarTekst.trim() || komentarSlanje} className="btn-prim" style={{ alignSelf: "flex-end", padding: "9px 16px" }}>
                  {komentarSlanje ? <Spinner size={14} color="#fff"/> : <Icon.Send/>}
                </button>
              </form>
            </div>
          </div>
        </div>

        {/* ─── Desna kolona (sidebar) ───────────────────────────── */}
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>

          {/* Status promjena */}
          <div className="card" style={{ padding: 22 }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 16 }}>
              Promijeni status
            </div>
            <div style={{ marginBottom: 12 }}>
              <label className="label">Trenutni status</label>
              <div style={{ marginBottom: 12 }}><StatusChip status={prijava.statusNaziv}/></div>
              <label className="label">Novi status</label>
              <select value={noviStatus} onChange={e => setNoviStatus(e.target.value)} className="input-field">
                <option value="">Odaberi...</option>
                {statuses.filter(s => s !== prijava.statusNaziv).map(s => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
            <button onClick={handleStatusChange} disabled={!noviStatus || loading} className="btn-prim" style={{ width: "100%" }}>
              {loading ? <><Spinner size={14} color="#fff"/> Ažuriranje...</> : <><Icon.Refresh/> Ažuriraj status</>}
            </button>
          </div>

          {/* Detalji prijave */}
          <div className="card" style={{ padding: 22 }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginBottom: 14 }}>Detalji prijave</div>
            {[
              { label: "ID",        val: `#${prijava.id}`,          mono: true },
              { label: "Arhivirana", val: prijava.arhiviran ? "Da" : "Ne", color: prijava.arhiviran ? T.amber : T.textSub },
              { label: "Komentari", val: komentari.length, color: T.textSub },
            ].map(({ label, val, color, mono }) => (
              <div key={label} style={{
                display: "flex", justifyContent: "space-between", alignItems: "center",
                padding: "9px 0", borderBottom: `1px solid ${T.line}`,
              }}>
                <span style={{ fontSize: 12, color: T.textMuted }}>{label}</span>
                <span style={{ fontSize: 12, fontWeight: 500, color: color || T.text, fontFamily: mono ? "'Geist Mono', monospace" : "inherit" }}>{val}</span>
              </div>
            ))}
          </div>

          {/* Arhivacija (samo za RUKOVODILAC/RADNIK) */}
          {(user?.uloga === "RUKOVODILAC" || user?.uloga === "RADNIK" || user?.uloga === "ADMIN") && (
            <div className="card" style={{ padding: 22, borderColor: T.amberBorder }}>
              <div style={{ fontSize: 13, fontWeight: 500, color: T.amber, marginBottom: 12 }}>Admin akcije</div>
              <button
                onClick={async () => {
                  try {
                    await apiCall(`/api/prijave/${prijava.id}/arhiviraj`, { method: "PATCH" });
                    setPrijava(p => ({ ...p, arhiviran: true }));
                    setToast({ msg: "Prijava arhivirana.", type: "success" });
                  } catch (err) {
                    setToast({ msg: err.message, type: "error" });
                  }
                }}
                disabled={prijava.arhiviran}
                className="btn-ghost"
                style={{ width: "100%", fontSize: 12, borderColor: T.amberBorder, color: T.amber }}
              >
                {prijava.arhiviran ? "✓ Arhivirano" : "Arhiviraj prijavu"}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
