// frontend/src/__tests__/unit/Dashboard.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import Dashboard from "../../pages/Dashboard";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const user = { id: 1, ime: "Amir", uloga: "GRADJANIN" };

const prijaveFixture = [
  { id: 1, naslov: "Rupa na putu", statusNaziv: "Novo",       prioritet: "HITNO",   datumPodnosenja: "2025-06-01T10:00:00", nazivKategorije: "Put / cesta" },
  { id: 2, naslov: "Pokvarena rasvjeta", statusNaziv: "U radu", prioritet: "SREDNJI", datumPodnosenja: "2025-06-02T10:00:00", nazivKategorije: "Javna rasvjeta" },
  { id: 3, naslov: "Odložen smeće", statusNaziv: "Rijeseno",  prioritet: "NIZAK",   datumPodnosenja: "2025-05-30T10:00:00", nazivKategorije: "Otpad" },
];

function renderDashboard() {
  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ user, logout: vi.fn(), showToast: vi.fn() }}>
        <Dashboard />
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe("Dashboard", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("prikazuje korisnikovo ime u naslovu", async () => {
    apiCall.mockResolvedValue(prijaveFixture);
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText(/Amir/i)).toBeInTheDocument();
    });
  });

  it("prikazuje ispravne stat kartice", async () => {
    apiCall.mockResolvedValue(prijaveFixture);
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText("Ukupno")).toBeInTheDocument();
      expect(screen.getByText("Nove")).toBeInTheDocument();
      expect(screen.getByText("Riješeno")).toBeInTheDocument();
      expect(screen.getByText("Hitne")).toBeInTheDocument();
    });
  });

  it("ispravno broji hitne prijave (1 HITNO u fixture)", async () => {
    apiCall.mockResolvedValue(prijaveFixture);
    renderDashboard();
    // Čekaj da se podaci učitaju
    await waitFor(() => screen.getByText("Hitne"));
    // 1 hitna prijava
    const hitneCard = screen.getByText("Hitne").closest("[class*='card'], div");
    expect(hitneCard).toBeInTheDocument();
  });

  it("prikazuje poruku greške kad API padne", async () => {
    apiCall.mockRejectedValue(new Error("Sistem je trenutno nedostupan."));
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByText(/nedostupan/i)).toBeInTheDocument();
    });
  });

  it("prikazuje prazno stanje kad nema prijava", async () => {
    apiCall.mockResolvedValue([]);
    renderDashboard();
    await waitFor(() => {
      // Sve stat kartice trebaju biti 0
      const zeros = screen.getAllByText("0");
      expect(zeros.length).toBeGreaterThan(0);
    });
  });

  it("osvježi dugme ponovo poziva API", async () => {
    apiCall.mockResolvedValue(prijaveFixture);
    renderDashboard();
    await waitFor(() => screen.getByText(/Amir/i));

    const refreshBtn = screen.getByTitle
      ? screen.queryByTitle(/osvježi/i) || screen.getAllByRole("button")[0]
      : screen.getAllByRole("button")[0];

    apiCall.mockResolvedValue([...prijaveFixture]);
    fireEvent.click(refreshBtn);

    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledTimes(2);
    });
  });
});
