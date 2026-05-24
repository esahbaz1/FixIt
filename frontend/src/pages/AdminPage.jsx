import { useState, useEffect, useCallback } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

const ULOGE = ["GRADJANIN", "RADNIK", "RUKOVODILAC", "ADMIN"];

const ULOGA_CFG = {
  GRADJANIN:   { color: T.textSub,  bg: "rgba(141,184,154,0.1)",  border: "rgba(141,184,154,0.2)"  },
  RADNIK:      { color: T.amber,    bg: T.amberDim,               border: T.amberBorder             },
  RUKOVODILAC: { color: T.purple,   bg: T.purpleDim,              border: T.purpleBorder            },
  ADMIN:       { color: T.red,      bg: T.redDim,                 border: T.redBorder               },
};

function UlogaBadge({ uloga }) {
  const cfg = ULOGA_CFG[uloga] || ULOGA_CFG.GRADJANIN;
  return (
    <span style={{
      fontSize: 10, fontWeight: 700, letterSpacing: "0.07em", textTransform: "uppercase",
      color: cfg.color, background: cfg.bg, border: `1px solid ${cfg.border}`,
      padding: "2px 8px", borderRadius: 4,
    }}>{uloga}</span>
  );
}

export default function AdminPage() {
  const { user, showToast } = useAuth();
  const [korisnici, setKorisnici] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [search, setSearch]     = useState("");
  const [filterUloga, setFilterUloga] = useState("SVE");
  const [pendingId, setPendingId]   = useState(null);   // koji red se sprema
  const [novaUloga, setNovaUloga]   = useState({});     // { [id]: uloga }
  const [confirmModal, setConfirmModal] = useState(null); // { korisnik, uloga }

  const fetchKorisnici = useCallback(() => {
    setLoading(true);
    apiCall("/api/korisnici")
      .then(data => setKorisnici(Array.isArray(data) ? data : []))
      .catch(() => showToast("Greška pri dohvatanju korisnika.", "error"))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchKorisnici(); }, [fetchKorisnici]);

  // Filtrirani prikaz
  const filtered = korisnici.filter(k => {
    const matchSearch =
      `${k.ime} ${k.prezime} ${k.email}`.toLowerCase().includes(search.toLowerCase());
    const matchUloga = filterUloga === "SVE" || k.uloga === filterUloga;
    return matchSearch && matchUloga;
  });

  async function handlePromijeniUlogu(k) {
    const odabranaUloga = novaUloga[k.id];
    if (!odabranaUloga || odabranaUloga === k.uloga) return;
    setConfirmModal({ korisnik: k, uloga: odabranaUloga });
  }

  async function potvrdiPromjenu() {
    const { korisnik, uloga } = confirmModal;
    setConfirmModal(null);
    setPendingId(korisnik.id);
    try {
      await apiCall(
        `/api/korisnici/${korisnik.id}/uloga?novaUloga=${encodeURIComponent(uloga)}`,
        { method: "PUT" }
      );
      setKorisnici(prev =>
        prev.map(k => k.id === korisnik.id ? { ...k, uloga } : k)
      );
      setNovaUloga(prev => { const n = { ...prev }; delete n[korisnik.id]; return n; });
      showToast(`Uloga korisnika ${korisnik.ime} promijenjena u ${uloga}.`);
    } catch (err) {
      showToast(err.message || "Greška pri promjeni uloge.", "error");
    } finally {
      setPendingId(null);
    }
  }

  const stats = {
    ukupno:      korisnici.length,
    gradjanin:   korisnici.filter(k => k.uloga === "GRADJANIN").length,
    radnik:      korisnici.filter(k => k.uloga === "RADNIK").length,
    rukovodilac: korisnici.filter(k => k.uloga === "RUKOVODILAC").length,
    admin:       korisnici.filter(k => k.uloga === "ADMIN").length,
  };

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader title="Upravljanje korisnicima" sub="Promjena uloga i pregled naloga"/>

      {/* Stat kartice */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 12, marginBottom: 24 }}>
        {[
          { label: "Ukupno korisnika", val: stats.ukupno,      color: T.text    },
          { label: "Građani",          val: stats.gradjanin,   color: T.textSub },
          { label: "Radnici",          val: stats.radnik,      color: T.amber   },
          { label: "Rukovodioci",      val: stats.rukovodilac, color: T.purple  },
          { label: "Admini",           val: stats.admin,       color: T.red     },
        ].map(s => (
          <div key={s.label} className="card" style={{ padding: "16px 20px" }}>
            <div style={{ fontSize: 22, fontWeight: 700, color: s.color, fontFamily: "'Geist Mono', monospace" }}>
              {loading ? "—" : s.val}
            </div>
            <div style={{ fontSize: 11, color: T.textMuted, marginTop: 4 }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Filteri */}
      <div className="card" style={{ padding: "14px 20px", marginBottom: 16, display: "flex", gap: 12, alignItems: "center" }}>
        <div style={{ position: "relative", flex: 1, maxWidth: 320 }}>
          <span style={{ position: "absolute", left: 10, top: "50%", transform: "translateY(-50%)", color: T.textMuted }}>
            <Icon.Search/>
          </span>
          <input
            className="input-field"
            placeholder="Pretraži ime, prezime, email..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ paddingLeft: 32, fontSize: 13 }}
          />
        </div>
        <div style={{ display: "flex", gap: 6 }}>
          {["SVE", ...ULOGE].map(u => (
            <button
              key={u}
              onClick={() => setFilterUloga(u)}
              className={filterUloga === u ? "btn-prim" : "btn-ghost"}
              style={{ fontSize: 11, padding: "5px 12px" }}
            >
              {u === "SVE" ? "Sve uloge" : u}
            </button>
          ))}
        </div>
        <button onClick={fetchKorisnici} className="btn-ghost" style={{ fontSize: 12, padding: "5px 10px", marginLeft: "auto" }}>
          <Icon.Refresh/> Osvježi
        </button>
      </div>

      {/* Tabela */}
      <div className="card" style={{ overflow: "hidden" }}>
        {/* Header */}
        <div style={{
          display: "grid", gridTemplateColumns: "40px 1fr 200px 120px 160px 44px",
          padding: "10px 20px", borderBottom: `1px solid ${T.line}`,
          fontSize: 10, fontWeight: 600, color: T.textMuted,
          letterSpacing: "0.07em", textTransform: "uppercase",
        }}>
          <span>#</span>
          <span>Korisnik</span>
          <span>Email</span>
          <span>Trenutna uloga</span>
          <span>Nova uloga</span>
          <span></span>
        </div>

        {loading ? (
          <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><Spinner size={28}/></div>
        ) : filtered.length === 0 ? (
          <div style={{ padding: "36px 20px", textAlign: "center", color: T.textMuted, fontSize: 13 }}>
            Nema korisnika koji odgovaraju filteru.
          </div>
        ) : (
          filtered.map((k, i) => {
            const odabrana = novaUloga[k.id] || k.uloga;
            const promijenjeno = novaUloga[k.id] && novaUloga[k.id] !== k.uloga;
            const jaSam = k.id === user?.id;
            const sprema = pendingId === k.id;

            return (
              <div key={k.id} style={{
                display: "grid",
                gridTemplateColumns: "40px 1fr 200px 120px 160px 44px",
                padding: "12px 20px",
                borderBottom: i < filtered.length - 1 ? `1px solid ${T.line}` : "none",
                alignItems: "center",
                background: promijenjeno ? "rgba(243,156,18,0.04)" : "transparent",
                transition: "background 0.2s",
              }}>
                {/* ID */}
                <span style={{ fontSize: 11, color: T.textMuted, fontFamily: "'Geist Mono', monospace" }}>
                  {k.id}
                </span>

                {/* Ime */}
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <div style={{
                    width: 30, height: 30, borderRadius: "50%",
                    background: jaSam ? "rgba(46,204,113,0.2)" : "rgba(255,255,255,0.05)",
                    border: `1px solid ${jaSam ? "rgba(46,204,113,0.4)" : T.line}`,
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 11, fontWeight: 600,
                    color: jaSam ? "#2ECC71" : T.textSub,
                    flexShrink: 0,
                  }}>
                    {(k.ime?.[0] || "?").toUpperCase()}
                  </div>
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 500, color: T.text }}>
                      {k.ime} {k.prezime}
                      {jaSam && (
                        <span style={{ marginLeft: 6, fontSize: 10, color: "#2ECC71",
                          background: "rgba(46,204,113,0.1)", border: "1px solid rgba(46,204,113,0.2)",
                          padding: "1px 5px", borderRadius: 3 }}>Vi</span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Email */}
                <span style={{ fontSize: 12, color: T.textSub }}>{k.email}</span>

                {/* Trenutna uloga */}
                <UlogaBadge uloga={k.uloga}/>

                {/* Select nova uloga */}
                <select
                  value={odabrana}
                  onChange={e => setNovaUloga(prev => ({ ...prev, [k.id]: e.target.value }))}
                  disabled={jaSam || sprema}
                  className="input-field"
                  style={{
                    fontSize: 12, padding: "5px 8px",
                    opacity: jaSam ? 0.4 : 1,
                    cursor: jaSam ? "not-allowed" : "pointer",
                    borderColor: promijenjeno ? T.amberBorder : undefined,
                  }}
                >
                  {ULOGE.map(u => (
                    <option key={u} value={u}>{u}</option>
                  ))}
                </select>

                {/* Dugme spremi */}
                <button
                  onClick={() => handlePromijeniUlogu(k)}
                  disabled={!promijenjeno || jaSam || sprema}
                  className="btn-prim"
                  style={{
                    padding: "6px 8px", fontSize: 11,
                    opacity: (!promijenjeno || jaSam) ? 0.3 : 1,
                    minWidth: 36,
                  }}
                  title={jaSam ? "Ne možete mijenjati vlastitu ulogu" : "Spremi promjenu"}
                >
                  {sprema ? <Spinner size={12} color="#fff"/> : <Icon.Check/>}
                </button>
              </div>
            );
          })
        )}
      </div>

      {filtered.length > 0 && !loading && (
        <div style={{ marginTop: 10, fontSize: 11, color: T.textMuted, textAlign: "right" }}>
          Prikazano {filtered.length} od {korisnici.length} korisnika
        </div>
      )}

      {/* Confirm modal */}
      {confirmModal && (
        <div style={{
          position: "fixed", inset: 0, zIndex: 999,
          background: "rgba(0,0,0,0.7)", backdropFilter: "blur(4px)",
          display: "flex", alignItems: "center", justifyContent: "center",
        }} onClick={() => setConfirmModal(null)}>
          <div
            className="card"
            style={{ padding: 32, maxWidth: 400, width: "100%", margin: "0 16px" }}
            onClick={e => e.stopPropagation()}
          >
            <div style={{ display: "flex", gap: 12, marginBottom: 20 }}>
              <span style={{ color: T.amber, flexShrink: 0 }}><Icon.Alert/></span>
              <div>
                <div style={{ fontSize: 15, fontWeight: 600, color: T.text, marginBottom: 8 }}>
                  Potvrda promjene uloge
                </div>
                <p style={{ fontSize: 13, color: T.textSub, lineHeight: 1.6, margin: 0 }}>
                  Jeste li sigurni da želite promijeniti ulogu korisnika{" "}
                  <strong style={{ color: T.text }}>
                    {confirmModal.korisnik.ime} {confirmModal.korisnik.prezime}
                  </strong>{" "}
                  sa{" "}
                  <strong style={{ color: ULOGA_CFG[confirmModal.korisnik.uloga]?.color }}>
                    {confirmModal.korisnik.uloga}
                  </strong>{" "}
                  na{" "}
                  <strong style={{ color: ULOGA_CFG[confirmModal.uloga]?.color }}>
                    {confirmModal.uloga}
                  </strong>
                  ?
                </p>
              </div>
            </div>
            <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
              <button className="btn-ghost" onClick={() => setConfirmModal(null)} style={{ fontSize: 13 }}>
                Odustani
              </button>
              <button className="btn-prim" onClick={potvrdiPromjenu} style={{ fontSize: 13 }}>
                <Icon.Check/> Potvrdi
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
