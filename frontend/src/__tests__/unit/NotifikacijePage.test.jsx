// frontend/src/__tests__/unit/NotifikacijePage.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import NotifikacijePage from "../../pages/NotifikacijePage";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const user = { id: 1, uloga: "GRADJANIN", ime: "Ana" };

const fixture = [
  { id: 1, tip: "NOVA_PRIJAVA",    naslov: "Prijava primljena",   poruka: "Prijava je primljena.", procitano: false, datumKreiranja: "2025-06-01T10:00:00" },
  { id: 2, tip: "STATUS_PROMJENA", naslov: "Status promijenjen",  poruka: "Status je U radu.",     procitano: true,  datumKreiranja: "2025-06-02T12:00:00" },
  { id: 3, tip: "NOVI_KOMENTAR",   naslov: "Novi komentar dodan", poruka: "Neko je komentarisao.", procitano: false, datumKreiranja: "2025-06-03T08:00:00" },
];

function renderPage() {
  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ user, logout: vi.fn(), showToast: vi.fn() }}>
        <NotifikacijePage />
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe("NotifikacijePage", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("prikazuje listu notifikacija", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => {
      expect(screen.getByText("Prijava primljena")).toBeInTheDocument();
      expect(screen.getByText("Status promijenjen")).toBeInTheDocument();
      expect(screen.getByText("Novi komentar dodan")).toBeInTheDocument();
    });
  });

  it("prikazuje broj nepročitanih u podnaslovu", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => {
      // 2 nepročitane u fixture
      expect(screen.getByText(/2 nepročitanih/i)).toBeInTheDocument();
    });
  });

  it("prikazuje 'Sve pročitano' kad su sve pročitane", async () => {
    apiCall.mockResolvedValue(fixture.map(n => ({ ...n, procitano: true })));
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/Sve pročitano/i)).toBeInTheDocument();
    });
  });

  it("prazno stanje kad nema notifikacija", async () => {
    apiCall.mockResolvedValue([]);
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/Nema obavijesti/i)).toBeInTheDocument();
    });
  });

  it("prazno stanje za nepročitane kad ih nema", async () => {
    apiCall.mockResolvedValue([]);
    renderPage();
    await waitFor(() => screen.getByText(/Nema obavijesti/i));

    // Klikni na filter Nepročitane
    fireEvent.click(screen.getByText(/Nepročitane/i));
    apiCall.mockResolvedValue([]);

    await waitFor(() => {
      expect(screen.getByText(/Nema nepročitanih/i)).toBeInTheDocument();
    });
  });

  it("PATCH se poziva pri kliku na 'Označi kao pročitano'", async () => {
    apiCall
      .mockResolvedValueOnce(fixture)
      .mockResolvedValueOnce({ ...fixture[0], procitano: true });
    renderPage();
    await waitFor(() => screen.getByText("Prijava primljena"));

    const checkBtns = screen.getAllByTitle("Označi kao pročitano");
    fireEvent.click(checkBtns[0]);

    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledWith(
        expect.stringContaining("/procitano"),
        expect.objectContaining({ method: "PATCH" })
      );
    });
  });

  it("'Označi sve' PATCH-a sve nepročitane", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => screen.getByText("Prijava primljena"));

    const markAllBtn = screen.getByText(/Označi sve/i);
    fireEvent.click(markAllBtn);

    await waitFor(() => {
      // 2 nepročitane → 2 PATCH poziva
      const patchCalls = apiCall.mock.calls.filter(
        c => c[1]?.method === "PATCH"
      );
      expect(patchCalls.length).toBe(2);
    });
  });

  it("filter 'Nepročitane' šalje zahtjev na /neprocitane endpoint", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => screen.getByText("Prijava primljena"));

    fireEvent.click(screen.getByText(/Nepročitane/i));

    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledWith(
        expect.stringContaining("/neprocitane")
      );
    });
  });

  it("filter 'Sve' vraća na glavni endpoint", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => screen.getByText("Prijava primljena"));

    // Prvo klikni Nepročitane, onda Sve
    fireEvent.click(screen.getByText(/Nepročitane/i));
    apiCall.mockResolvedValue(fixture);
    fireEvent.click(screen.getByText("Sve"));

    await waitFor(() => {
      const sveCalls = apiCall.mock.calls.filter(
        c => typeof c[0] === "string" && c[0].match(/korisnik\/\d+$/)
      );
      expect(sveCalls.length).toBeGreaterThan(0);
    });
  });
});
