// frontend/src/__tests__/integration/prijaveFlow.test.jsx
//
// Integracijski testovi za PrijaveListPage:
// - Inicijalno učitavanje
// - Search kombiniran s filterima
// - Sort redosljed
// - Klik na red navigira na /prijave/:id

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import { NotificationProvider } from "../../context/NotificationContext";
import PrijaveListPage from "../../pages/PrijaveListPage";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const user = { id: 1, uloga: "GRADJANIN", ime: "Test" };

const fixture = [
  { id: 1, naslov: "Rupa na cesti A",    statusNaziv: "Novo",      prioritet: "HITNO",   datumPodnosenja: "2025-06-03T10:00:00", adresa: "Titova 1",   nazivKategorije: "Put" },
  { id: 2, naslov: "Pokvarena rasvjeta", statusNaziv: "U radu",    prioritet: "SREDNJI", datumPodnosenja: "2025-05-15T10:00:00", adresa: "Ferhadija",  nazivKategorije: "Rasvjeta" },
  { id: 3, naslov: "Vodovod pukao",      statusNaziv: "Rijeseno",  prioritet: "NIZAK",   datumPodnosenja: "2025-04-10T10:00:00", adresa: "Maršala 5", nazivKategorije: "Vodovod" },
  { id: 4, naslov: "Rupa na cesti B",    statusNaziv: "Novo",      prioritet: "VISOK",   datumPodnosenja: "2025-06-01T10:00:00", adresa: "Obala 2",    nazivKategorije: "Put" },
];

function renderPage() {
  global.fetch.mockResolvedValue({
    ok: true, status: 200,
    json: () => Promise.resolve({ brojNeprocitanih: 0 }),
  });
  return render(
    <MemoryRouter initialEntries={["/prijave"]}>
      <AuthContext.Provider value={{ user, logout: vi.fn(), showToast: vi.fn() }}>
        <NotificationProvider>
          <Routes>
            <Route path="/prijave"    element={<PrijaveListPage />} />
            <Route path="/prijave/:id" element={<div data-testid="detail-page">Detalji</div>} />
          </Routes>
        </NotificationProvider>
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe("PrijaveListPage – integracija", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("učitava i prikazuje sve prijave", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => {
      expect(screen.getByText("Rupa na cesti A")).toBeInTheDocument();
      expect(screen.getByText("Pokvarena rasvjeta")).toBeInTheDocument();
      expect(screen.getByText("Vodovod pukao")).toBeInTheDocument();
      expect(screen.getByText("Rupa na cesti B")).toBeInTheDocument();
    });
  });

  it("search po naslovu filtrira listu u realnom vremenu", async () => {
    apiCall.mockResolvedValue(fixture);
    const user_ = userEvent.setup();
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti A"));

    await user_.type(screen.getByPlaceholderText(/Pretraži/i), "Rupa");

    expect(screen.getByText("Rupa na cesti A")).toBeInTheDocument();
    expect(screen.getByText("Rupa na cesti B")).toBeInTheDocument();
    expect(screen.queryByText("Pokvarena rasvjeta")).not.toBeInTheDocument();
    expect(screen.queryByText("Vodovod pukao")).not.toBeInTheDocument();
    expect(screen.getByText(/2 od 4/)).toBeInTheDocument();
  });

  it("search po kategoriji filtrira ispravno", async () => {
    apiCall.mockResolvedValue(fixture);
    const user_ = userEvent.setup();
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti A"));

    await user_.type(screen.getByPlaceholderText(/Pretraži/i), "Vodovod");

    expect(screen.getByText("Vodovod pukao")).toBeInTheDocument();
    expect(screen.queryByText("Rupa na cesti A")).not.toBeInTheDocument();
  });

  it("brisanje searcha vraća sve prijave", async () => {
    apiCall.mockResolvedValue(fixture);
    const user_ = userEvent.setup();
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti A"));

    const input = screen.getByPlaceholderText(/Pretraži/i);
    await user_.type(input, "Rupa");
    expect(screen.getByText(/2 od 4/)).toBeInTheDocument();

    await user_.clear(input);
    expect(screen.getByText(/4 od 4/)).toBeInTheDocument();
  });

  it("kombinacija search + status filter", async () => {
    apiCall.mockResolvedValue(fixture);
    const user_ = userEvent.setup();
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti A"));

    // Search "cesti" pronalazi 2, ali filter "U radu" treba dati 0
    await user_.type(screen.getByPlaceholderText(/Pretraži/i), "cesti");

    // Pronađi status select/filter
    const statusSelects = screen.getAllByRole("combobox");
    if (statusSelects[0]) {
      fireEvent.change(statusSelects[0], { target: { value: "U radu" } });
      await waitFor(() => {
        // "Rupa na cesti" su Novo i Visok, ne "U radu" → 0 rezultata
        expect(screen.queryByText("Rupa na cesti A")).not.toBeInTheDocument();
      });
    }
  });

  it("prikazuje poruku greške pri API padu", async () => {
    apiCall.mockRejectedValue(new Error("Sistem je trenutno nedostupan. Pokušajte ponovo za nekoliko minuta."));
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/nedostupan/i)).toBeInTheDocument();
    });
  });

  it("klik na red prijave navigira na detalj stranicu", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti A"));

    fireEvent.click(screen.getByText("Rupa na cesti A").closest("tr") || screen.getByText("Rupa na cesti A"));

    await waitFor(() => {
      expect(screen.getByTestId("detail-page")).toBeInTheDocument();
    });
  });
});
