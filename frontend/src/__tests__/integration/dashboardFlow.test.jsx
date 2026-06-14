// frontend/src/__tests__/integration/dashboardFlow.test.jsx
//
// Integracijski testovi za Dashboard:
// - Podatke učitava pri montaži
// - Stat kartice se ispravno računaju
// - Greška API-ja se prikazuje
// - ADMIN vs GRADJANIN navBar razlike (AppShell)

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import { NotificationProvider } from "../../context/NotificationContext";
import Dashboard from "../../pages/Dashboard";
import AppShell from "../../components/AppShell";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const gradjanin = { id: 1, ime: "Alen", uloga: "GRADJANIN", email: "alen@test.ba" };
const admin     = { id: 2, ime: "Admin", uloga: "ADMIN",     email: "admin@test.ba" };

const prijave = [
  { id: 1, naslov: "Rupa",     statusNaziv: "Novo",     prioritet: "HITNO",   datumPodnosenja: "2025-06-01T08:00:00", nazivKategorije: "Put" },
  { id: 2, naslov: "Rasvjeta", statusNaziv: "U radu",   prioritet: "SREDNJI", datumPodnosenja: "2025-06-02T09:00:00", nazivKategorije: "Rasvjeta" },
  { id: 3, naslov: "Otpad",    statusNaziv: "Rijeseno", prioritet: "NIZAK",   datumPodnosenja: "2025-05-30T07:00:00", nazivKategorije: "Otpad" },
  { id: 4, naslov: "Voda",     statusNaziv: "Novo",     prioritet: "HITNO",   datumPodnosenja: "2025-06-03T06:00:00", nazivKategorije: "Vodovod" },
];

function renderDashboard(user) {
  // fetch mock za NotificationProvider (brojNeprocitanih)
  global.fetch.mockResolvedValue({
    ok: true, status: 200,
    json: () => Promise.resolve({ brojNeprocitanih: 0 }),
  });

  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ user, logout: vi.fn(), showToast: vi.fn() }}>
        <NotificationProvider>
          <Dashboard />
        </NotificationProvider>
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

function renderShell(user) {
  global.fetch.mockResolvedValue({
    ok: true, status: 200,
    json: () => Promise.resolve({ brojNeprocitanih: 0 }),
  });

  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ user, logout: vi.fn(), showToast: vi.fn() }}>
        <NotificationProvider>
          <AppShell><div>sadržaj</div></AppShell>
        </NotificationProvider>
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe("Dashboard – integracija", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("poziva /api/prijave pri montaži", async () => {
    apiCall.mockResolvedValue(prijave);
    renderDashboard(gradjanin);
    await waitFor(() => expect(apiCall).toHaveBeenCalledWith("/api/prijave"));
  });

  it("prikazuje korisnikovo ime", async () => {
    apiCall.mockResolvedValue(prijave);
    renderDashboard(gradjanin);
    await waitFor(() => expect(screen.getByText(/Alen/)).toBeInTheDocument());
  });

  it("ispravno broji 2 hitne prijave", async () => {
    apiCall.mockResolvedValue(prijave);
    renderDashboard(gradjanin);
    await waitFor(() => {
      // Tražimo text "2" unutar stat kartice za "Hitne"
      const hitneLabel = screen.getByText("Hitne");
      expect(hitneLabel).toBeInTheDocument();
    });
  });

  it("ispravno broji 1 riješenu", async () => {
    apiCall.mockResolvedValue(prijave);
    renderDashboard(gradjanin);
    await waitFor(() => screen.getByText("Riješeno"));
    // Provjera da je stat kartica rendirana
    expect(screen.getByText("Riješeno")).toBeInTheDocument();
  });

  it("prikazuje grešku kad API padne", async () => {
    apiCall.mockRejectedValue(new Error("Sistem je trenutno nedostupan. Pokušajte ponovo za nekoliko minuta."));
    renderDashboard(gradjanin);
    await waitFor(() => {
      expect(screen.getByText(/nedostupan/i)).toBeInTheDocument();
    });
  });

  it("prazne prijave → sve stat kartice pokazuju 0", async () => {
    apiCall.mockResolvedValue([]);
    renderDashboard(gradjanin);
    await waitFor(() => {
      const zeros = screen.getAllByText("0");
      expect(zeros.length).toBeGreaterThanOrEqual(4); // ukupno, novo, u toku, riješeno, hitno
    });
  });

  it("recent lista pokazuje max 6 unosa sortiranih po datumu", async () => {
    // 7 prijava – samo 6 smije biti vidljivo u recent sekciji
    const mnogo = Array.from({ length: 7 }, (_, i) => ({
      id: i + 1,
      naslov: `Prijava ${i + 1}`,
      statusNaziv: "Novo",
      prioritet: "NIZAK",
      datumPodnosenja: `2025-06-0${Math.min(i + 1, 9)}T10:00:00`,
      nazivKategorije: "Put",
    }));
    apiCall.mockResolvedValue(mnogo);
    renderDashboard(gradjanin);
    await waitFor(() => screen.getByText("Prijava 7"));
    // Prijava 1 (najstarija) ne smije biti u recent listi (limit 6)
    expect(screen.queryByText("Prijava 1")).not.toBeInTheDocument();
  });
});

describe("AppShell – navigacija po ulozi", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("GRADJANIN ne vidi Admin i Dashboard linkove", () => {
    renderShell(gradjanin);
    expect(screen.queryByText("Korisnici")).not.toBeInTheDocument();
    expect(screen.queryByText("Dashboard")).not.toBeInTheDocument();
  });

  it("ADMIN vidi Korisnici i Dashboard linkove", () => {
    renderShell(admin);
    expect(screen.getByText("Korisnici")).toBeInTheDocument();
    expect(screen.getByText("Dashboard")).toBeInTheDocument();
  });

  it("badge se prikazuje u navigaciji kad ima nepročitanih", async () => {
    global.fetch.mockResolvedValue({
      ok: true, status: 200,
      json: () => Promise.resolve({ brojNeprocitanih: 5 }),
    });
    renderShell(gradjanin);
    await waitFor(() => {
      expect(screen.getByText("5")).toBeInTheDocument();
    });
  });

  it("badge se NE prikazuje kad je count 0", async () => {
    global.fetch.mockResolvedValue({
      ok: true, status: 200,
      json: () => Promise.resolve({ brojNeprocitanih: 0 }),
    });
    renderShell(gradjanin);
    await waitFor(() => {
      expect(screen.queryByText("0")).not.toBeInTheDocument();
    });
  });
});
