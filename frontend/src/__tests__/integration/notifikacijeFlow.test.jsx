// frontend/src/__tests__/integration/notifikacijeFlow.test.jsx
//
// Integracijski testovi za NotifikacijePage:
// - Učitavanje notifikacija
// - Označavanje kao pročitano (pojedinačno i sve)
// - Filter Nepročitane / Sve
// - Greška API-ja

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import NotifikacijePage from "../../pages/NotifikacijePage";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const user = { id: 5, uloga: "GRADJANIN", ime: "Zlatan" };

const fixture = [
  { id: 10, tip: "NOVA_PRIJAVA",    naslov: "Prijava kreirana",    poruka: "Vaša prijava je primljena.", procitano: false, datumKreiranja: "2025-06-03T10:00:00" },
  { id: 11, tip: "STATUS_PROMJENA", naslov: "Status ažuriran",     poruka: "Status: U radu",             procitano: false, datumKreiranja: "2025-06-02T09:00:00" },
  { id: 12, tip: "RIJESENO",        naslov: "Prijava riješena",    poruka: "Problem je riješen.",         procitano: true,  datumKreiranja: "2025-06-01T08:00:00" },
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

describe("NotifikacijePage – integracija", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("učitava notifikacije za korisnika i prikazuje ih", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledWith(
        expect.stringContaining(`/korisnik/${user.id}`)
      );
      expect(screen.getByText("Prijava kreirana")).toBeInTheDocument();
      expect(screen.getByText("Status ažuriran")).toBeInTheDocument();
      expect(screen.getByText("Prijava riješena")).toBeInTheDocument();
    });
  });

  it("prikazuje tačan broj nepročitanih (2 od 3)", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/2 nepročitanih/i)).toBeInTheDocument();
    });
  });

  it("označavanje pojedinačne notifikacije poziva PATCH endpoint", async () => {
    apiCall
      .mockResolvedValueOnce(fixture)                          // inicijalni load
      .mockResolvedValueOnce({ ...fixture[0], procitano: true }); // PATCH odgovor

    renderPage();
    await waitFor(() => screen.getByText("Prijava kreirana"));

    const checkBtns = screen.getAllByTitle("Označi kao pročitano");
    fireEvent.click(checkBtns[0]);

    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledWith(
        expect.stringContaining(`/10/procitano`),
        expect.objectContaining({ method: "PATCH" })
      );
    });
  });

  it("'Označi sve' šalje PATCH za svaku nepročitanu", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => screen.getByText("Prijava kreirana"));

    fireEvent.click(screen.getByText(/Označi sve/i));

    await waitFor(() => {
      const patchCalls = apiCall.mock.calls.filter(c => c[1]?.method === "PATCH");
      expect(patchCalls.length).toBe(2); // fixture ima 2 nepročitane
    });
  });

  it("sve pročitane → prikazuje 'Sve pročitano' umjesto broja", async () => {
    apiCall.mockResolvedValue(fixture.map(n => ({ ...n, procitano: true })));
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/Sve pročitano/i)).toBeInTheDocument();
    });
  });

  it("filter 'Nepročitane' poziva odgovarajući endpoint", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => screen.getByText("Prijava kreirana"));

    apiCall.mockResolvedValue(fixture.filter(n => !n.procitano));
    fireEvent.click(screen.getByText(/Nepročitane/i));

    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledWith(
        expect.stringContaining("/neprocitane")
      );
    });
  });

  it("filter 'Nepročitane' skriva pročitane iz liste", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => screen.getByText("Prijava riješena"));

    apiCall.mockResolvedValue(fixture.filter(n => !n.procitano));
    fireEvent.click(screen.getByText(/Nepročitane/i));

    await waitFor(() => {
      expect(screen.queryByText("Prijava riješena")).not.toBeInTheDocument();
      expect(screen.getByText("Prijava kreirana")).toBeInTheDocument();
    });
  });

  it("filter 'Sve' vraća sve notifikacije", async () => {
    apiCall.mockResolvedValue(fixture.filter(n => !n.procitano));
    renderPage();
    await waitFor(() => screen.getByText("Prijava kreirana"));

    apiCall.mockResolvedValue(fixture);
    fireEvent.click(screen.getByText("Sve"));

    await waitFor(() => {
      expect(screen.getByText("Prijava riješena")).toBeInTheDocument();
    });
  });

  it("prazna lista prikazuje 'Nema obavijesti'", async () => {
    apiCall.mockResolvedValue([]);
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/Nema obavijesti/i)).toBeInTheDocument();
    });
  });

  it("API greška prikazuje poruku o grešci", async () => {
    apiCall.mockRejectedValue(new Error("Sistem je trenutno nedostupan."));
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/nedostupan/i)).toBeInTheDocument();
    });
  });
});
