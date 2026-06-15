import { useState, useEffect, useRef } from "react";
import T from "../styles/tokens";
import { apiCall } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { KATEGORIJE, PRIO_CFG } from "../api/constants";
import PageHeader from "../components/PageHeader";
import Spinner from "../components/Spinner";

function LeafletMap({ lat, lng, onPick }) {
  const mapRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerRef = useRef(null);
  const containerRef = useRef(null);

  useEffect(() => {
    if (!document.getElementById("leaflet-css")) {
      const link = document.createElement("link");
      link.id = "leaflet-css";
      link.rel = "stylesheet";
      link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
      document.head.appendChild(link);
    }

    const initMap = () => {
      if (mapInstanceRef.current) return;
      const L = window.L;
      if (!L) return;

      const defaultLat = lat || 43.8563;
      const defaultLng = lng || 18.4131;

      const map = L.map(containerRef.current, { zoomControl: true }).setView(
        [defaultLat, defaultLng],
        14
      );

      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap",
        maxZoom: 19,
      }).addTo(map);

      const icon = L.divIcon({
        html: `<div style="
          width:28px;height:28px;border-radius:50% 50% 50% 0;
          background:#E74C3C;border:3px solid #fff;
          transform:rotate(-45deg);box-shadow:0 2px 8px rgba(0,0,0,0.4);
        "></div>`,
        iconSize: [28, 28],
        iconAnchor: [14, 28],
        className: "",
      });

      if (lat && lng) {
        markerRef.current = L.marker([lat, lng], { icon }).addTo(map);
      }

      map.on("click", (e) => {
        const { lat: clickLat, lng: clickLng } = e.latlng;
        if (markerRef.current) {
          markerRef.current.setLatLng([clickLat, clickLng]);
        } else {
          markerRef.current = L.marker([clickLat, clickLng], { icon }).addTo(map);
        }
        onPick(clickLat, clickLng);
      });

      mapInstanceRef.current = map;
      mapRef.current = map;
    };

    if (window.L) {
      initMap();
    } else {
      const script = document.createElement("script");
      script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
      script.onload = initMap;
      document.head.appendChild(script);
    }

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
        markerRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (!mapInstanceRef.current || !window.L) return;
    if (!lat || !lng) return;
    const L = window.L;
    const icon = L.divIcon({
      html: `<div style="
        width:28px;height:28px;border-radius:50% 50% 50% 0;
        background:#E74C3C;border:3px solid #fff;
        transform:rotate(-45deg);box-shadow:0 2px 8px rgba(0,0,0,0.4);
      "></div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 28],
      className: "",
    });
    if (markerRef.current) {
      markerRef.current.setLatLng([lat, lng]);
    } else {
      markerRef.current = L.marker([lat, lng], { icon }).addTo(mapInstanceRef.current);
    }
    mapInstanceRef.current.setView([lat, lng], mapInstanceRef.current.getZoom());
  }, [lat, lng]);

  return (
    <div style={{ position: "relative" }}>
      <div
        ref={containerRef}
        style={{
          width: "100%",
          height: 320,
          borderRadius: 10,
          border: `1px solid ${T.line}`,
          overflow: "hidden",
          background: T.bgRaised,
        }}
      />
      <div
        style={{
          position: "absolute",
          bottom: 10,
          left: "50%",
          transform: "translateX(-50%)",
          background: "rgba(0,0,0,0.65)",
          backdropFilter: "blur(6px)",
          color: "#fff",
          fontSize: 11,
          padding: "5px 12px",
          borderRadius: 20,
          pointerEvents: "none",
          whiteSpace: "nowrap",
        }}
      >
        Kliknite na mapu za odabir lokacije
      </div>
    </div>
  );
}

export default function NovaPrijavaPage({ onSuccess }) {
  const { user } = useAuth();
  const [form, setForm] = useState({
    naslov: "",
    opis: "",
    latitude: "",
    longitude: "",
    adresa: "",
    kategorijaId: "1",
    prioritet: "SREDNJI",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);
  
  // Ref pomoću kojeg ćemo skrolati do greške ako se pojavi
  const errorRef = useRef(null);

  function handleMapPick(lat, lng) {
    setForm((prev) => ({
      ...prev,
      latitude: lat.toFixed(6),
      longitude: lng.toFixed(6),
    }));
  }

  // Automatsko skrolovanje do poruke o grešci čim se pojavi
  useEffect(() => {
    if (error && errorRef.current) {
      errorRef.current.scrollIntoView({ behavior: "smooth", block: "center" });
    }
  }, [error]);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError("");

    if (!form.naslov || form.naslov.trim() === "") {
      setError("Potrebno unijeti naslov.");
      setLoading(false);
      return;
    }

    if (!form.opis || form.opis.trim() === "") {
      setError("Potrebno unijeti opis.");
      setLoading(false);
      return;
    }

    if (!form.latitude || !form.longitude) {
      setError("Potrebno unijeti longitude i latitude ili označiti lokaciju na mapi.");
      setLoading(false);
      return;
    }

    try {
      const res = await apiCall("/api/prijave", {
        method: "POST",
        body: JSON.stringify({
          naslov: form.naslov,
          opis: form.opis,
          latitude: parseFloat(form.latitude),
          longitude: parseFloat(form.longitude),
          adresa: form.adresa,
          kategorijaId: parseInt(form.kategorijaId),
          prioritet: form.prioritet,
        }),
      });
      setDone(true);
      setTimeout(onSuccess, 2000);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  if (done)
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          padding: "80px 0",
          animation: "fadeUp 0.4s ease",
        }}
      >
        <div
          style={{
            width: 56,
            height: 56,
            borderRadius: "50%",
            background: T.greenDim,
            border: `1px solid ${T.greenBorder}`,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            marginBottom: 20,
          }}
        >
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <polyline
              points="3,10 8,15 17,5"
              stroke={T.green}
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </div>
        <h3
          style={{
            fontSize: 18,
            fontWeight: 600,
            color: T.text,
            marginBottom: 8,
            letterSpacing: "-0.01em",
          }}
        >
          Prijava kreirana
        </h3>
        <p style={{ color: T.textSub, fontSize: 13 }}>Preusmjeravamo vas...</p>
      </div>
    );

  const prioOptions = [
    { v: "NIZAK", label: "Nizak", color: T.green },
    { v: "SREDNJI", label: "Srednji", color: T.amber },
    { v: "VISOK", label: "Visok", color: T.orange },
    { v: "HITNO", label: "Hitno", color: T.red },
  ];

  const mapLat = parseFloat(form.latitude) || null;
  const mapLng = parseFloat(form.longitude) || null;

  return (
    <div style={{ animation: "fadeIn 0.3s ease" }}>
      <PageHeader title="Nova prijava" sub="Prijavite komunalni problem" />

      <div style={{ display: "grid", gridTemplateColumns: "1fr 280px", gap: 16 }}>
        <div className="card" style={{ padding: 32 }}>
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: 18 }}>
              <label className="label">Naslov *</label>
              <input
                type="text"
                value={form.naslov}
                onChange={(e) => setForm({ ...form, naslov: e.target.value })}
                placeholder="npr. Oštećenje asfalta — Titova ulica"
                className="input-field"
              />
            </div>

            <div style={{ marginBottom: 18 }}>
              <label className="label">Opis *</label>
              <textarea
                value={form.opis}
                onChange={(e) => setForm({ ...form, opis: e.target.value })}
                placeholder="Opišite problem — veličina, utjecaj, lokacija..."
                rows={4}
                className="input-field"
                style={{ resize: "vertical", minHeight: 100 }}
              />
            </div>

            <div style={{ marginBottom: 18 }}>
              <label className="label">Kategorija *</label>
              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "repeat(4, 1fr)",
                  gap: 6,
                }}
              >
                {KATEGORIJE.map((k) => (
                  <button
                    key={k.id}
                    type="button"
                    onClick={() =>
                      setForm({ ...form, kategorijaId: String(k.id) })
                    }
                    style={{
                      padding: "9px 6px",
                      borderRadius: 8,
                      cursor: "pointer",
                      background:
                        form.kategorijaId === String(k.id)
                          ? T.blueDim
                          : T.bgRaised,
                      border: `1px solid ${
                        form.kategorijaId === String(k.id)
                          ? T.blueBorder
                          : T.line
                      }`,
                      color:
                        form.kategorijaId === String(k.id)
                          ? T.blue
                          : T.textSub,
                      fontSize: 11,
                      fontWeight: 500,
                      transition: "all 0.12s",
                      textAlign: "center",
                    }}
                  >
                    {k.naziv}
                  </button>
                ))}
              </div>
            </div>

            <div style={{ marginBottom: 18 }}>
              <label className="label">Prioritet</label>
              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "repeat(4, 1fr)",
                  gap: 6,
                }}
              >
                {prioOptions.map((p) => {
                  const active = form.prioritet === p.v;
                  const cfg = PRIO_CFG[p.v];
                  return (
                    <button
                      key={p.v}
                      type="button"
                      onClick={() => setForm({ ...form, prioritet: p.v })}
                      style={{
                        padding: "9px 6px",
                        borderRadius: 8,
                        cursor: "pointer",
                        background: active ? cfg.dim : T.bgRaised,
                        border: `1px solid ${
                          active ? p.color + "50" : T.line
                        }`,
                        color: active ? p.color : T.textSub,
                        fontSize: 11,
                        fontWeight: 500,
                        transition: "all 0.12s",
                        textAlign: "center",
                      }}
                    >
                      {p.label}
                    </button>
                  );
                })}
              </div>
            </div>

            <div style={{ marginBottom: 16 }}>
              <label className="label">Adresa</label>
              <input
                type="text"
                value={form.adresa}
                onChange={(e) => setForm({ ...form, adresa: e.target.value })}
                placeholder="Titova ulica 15, Sarajevo"
                className="input-field"
              />
            </div>

            <div style={{ marginBottom: 16 }}>
              <label className="label">
                Lokacija na mapi{" "}
                <span style={{ color: T.textMuted, fontWeight: 400 }}>
                  — kliknite za tačno označavanje
                </span>
              </label>
              <LeafletMap
                lat={mapLat}
                lng={mapLng}
                onPick={handleMapPick}
              />
            </div>

            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 12,
                marginBottom: 24,
              }}
            >
              <div>
                <label className="label">Latitude</label>
                <input
                  type="number"
                  step="any"
                  value={form.latitude}
                  onChange={(e) =>
                    setForm({ ...form, latitude: e.target.value })
                  }
                  placeholder="npr. 43.8563"
                  className="input-field"
                  style={{
                    background: form.latitude ? T.greenDim : undefined,
                    borderColor: form.latitude ? T.greenBorder : undefined,
                  }}
                />
              </div>
              <div>
                <label className="label">Longitude</label>
                <input
                  type="number"
                  step="any"
                  value={form.longitude}
                  onChange={(e) =>
                    setForm({ ...form, longitude: e.target.value })
                  }
                  placeholder="npr. 18.4131"
                  className="input-field"
                  style={{
                    background: form.longitude ? T.greenDim : undefined,
                    borderColor: form.longitude ? T.greenBorder : undefined,
                  }}
                />
              </div>
            </div>

            {/* --- PORUKA O GREŠCI PREBAČENA OVDJE (Tik iznad dugmeta za slanje) --- */}
            {error && (
              <div
                ref={errorRef}
                style={{
                  background: T.redDim,
                  border: `2px solid ${T.redBorder}`, // Podebljan border radi bolje uočljivosti
                  color: T.red,
                  padding: "12px 16px",
                  borderRadius: 8,
                  marginBottom: 16,
                  fontSize: 14,
                  fontWeight: "500",
                  display: "flex",
                  alignItems: "center",
                  gap: "8px",
                  boxShadow: "0 2px 8px rgba(231, 76, 60, 0.15)", // Blaga sjena u boji greške
                  animation: "fadeIn 0.2s ease-in-out"
                }}
              >
                {/* Ikona uzvika da dodatno skrene pažnju */}
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>{error}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="btn-prim"
              style={{ width: "100%", padding: "11px" }}
            >
              {loading ? (
                <>
                  <Spinner size={16} color="#fff" /> Kreiranje...
                </>
              ) : (
                "Pošalji prijavu"
              )}
            </button>
          </form>
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <div className="card" style={{ padding: 20 }}>
            <div
              style={{
                fontSize: 12,
                fontWeight: 500,
                color: T.textSub,
                marginBottom: 14,
              }}
            >
              Smjernice
            </div>
            {[
              "Konkretan naslov koji opisuje problem",
              "Detaljan opis veličine i utjecaja",
              "Kliknite na mapu za tačnu GPS lokaciju",
              "Tačna adresa ubrzava terene",
              "Realan prioritet — hitno samo ako je opasnost",
            ].map((tip, i) => (
              <div
                key={i}
                style={{
                  display: "flex",
                  gap: 10,
                  marginBottom: 10,
                  alignItems: "flex-start",
                }}
              >
                <div
                  style={{
                    width: 16,
                    height: 16,
                    borderRadius: 3,
                    background: T.bgActive,
                    border: `1px solid ${T.line}`,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    flexShrink: 0,
                    marginTop: 1,
                  }}
                >
                  <svg width="7" height="7" viewBox="0 0 7 7" fill="none">
                    <polyline
                      points="1,3.5 3,5.5 6,1.5"
                      stroke={T.textMuted}
                      strokeWidth="1.2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                </div>
                <span style={{ fontSize: 12, color: T.textSub, lineHeight: 1.5 }}>
                  {tip}
                </span>
              </div>
            ))}
          </div>

          <div
            className="card"
            style={{ padding: 20, borderColor: T.redBorder }}
          >
            <div
              style={{
                fontSize: 12,
                fontWeight: 500,
                color: T.red,
                marginBottom: 10,
              }}
            >
              Hitne situacije
            </div>
            <p
              style={{
                fontSize: 12,
                color: T.textSub,
                lineHeight: 1.6,
                margin: 0,
              }}
            >
              Za situacije koje direktno ugrožavaju ljude ili imovinu,
              kontaktirajte komunalne službe direktno uz odabir prioriteta
              &quot;Hitno&quot;.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}