// frontend/src/__tests__/unit/PrijaveListPage.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import PrijaveListPage from "../../pages/PrijaveListPage";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const user = { id: 1, uloga: "GRADJANIN", ime: "Test" };

const fixture = [
  { id: 1, naslov: "Rupa na cesti",      statusNaziv: "Novo",     prioritet: "HITNO",   datumPodnosenja: "2025-06-01T10:00:00", adresa: "Titova 1",  nazivKategorije: "Put / cesta" },
  { id: 2, naslov: "Pokvarena rasvjeta", statusNaziv: "U radu",   prioritet: "SREDNJI", datumPodnosenja: "2025-05-15T10:00:00", adresa: "Ferhadija",  nazivKategorije: "Javna rasvjeta" },
  { id: 3, naslov: "Vodovod pukao",      statusNaziv: "Rijeseno", prioritet: "NIZAK",   datumPodnosenja: "2025-04-10T10:00:00", adresa: "Maršala 5", nazivKategorije: "Vodovod" },
];

function renderPage() {
  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ user, logout: vi.fn(), showToast: vi.fn() }}>
        <PrijaveListPage />
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe("PrijaveListPage", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("prikazuje sve prijave po učitavanju", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => {
      expect(screen.getByText("Rupa na cesti")).toBeInTheDocument();
      expect(screen.getByText("Pokvarena rasvjeta")).toBeInTheDocument();
      expect(screen.getByText("Vodovod pukao")).toBeInTheDocument();
    });
  });

  it("prikazuje broj prijava u podnaslovu", async () => {
    apiCall.mockResolvedValue(fixture);
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/3 od 3/)).toBeInTheDocument();
    });
  });

  it("search filtrira po naslovu", async () => {
    apiCall.mockResolvedValue(fixture);
    const user_ = userEvent.setup();
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti"));

    await user_.type(screen.getByPlaceholderText(/Pretraži/i), "Rupa");

    expect(screen.getByText("Rupa na cesti")).toBeInTheDocument();
    expect(screen.queryByText("Pokvarena rasvjeta")).not.toBeInTheDocument();
  });

  it("search filtrira po adresi", async () => {
    apiCall.mockResolvedValue(fixture);
    const user_ = userEvent.setup();
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti"));

    await user_.type(screen.getByPlaceholderText(/Pretraži/i), "Ferhadija");

    expect(screen.getByText("Pokvarena rasvjeta")).toBeInTheDocument();
    expect(screen.queryByText("Rupa na cesti")).not.toBeInTheDocument();
  });

  it("prikazuje poruku greške kad API padne", async () => {
    apiCall.mockRejectedValue(new Error("Sistem je trenutno nedostupan."));
    renderPage();
    await waitFor(() => {
      expect(screen.getByText(/nedostupan/i)).toBeInTheDocument();
    });
  });

  it("prikazuje prazno stanje kad nema prijava", async () => {
    apiCall.mockResolvedValue([]);
    renderPage();
    await waitFor(() => {
      // Nema redova u tabeli / listi
      expect(screen.queryByText("Rupa na cesti")).not.toBeInTheDocument();
    });
  });

  it("search koji ne odgovara nijednoj prijavi prikazuje 0 rezultata", async () => {
    apiCall.mockResolvedValue(fixture);
    const user_ = userEvent.setup();
    renderPage();
    await waitFor(() => screen.getByText("Rupa na cesti"));

    await user_.type(screen.getByPlaceholderText(/Pretraži/i), "xyznepronalazim");

    expect(screen.queryByText("Rupa na cesti")).not.toBeInTheDocument();
    expect(screen.queryByText("Pokvarena rasvjeta")).not.toBeInTheDocument();
    expect(screen.getByText(/0 od 3/)).toBeInTheDocument();
  });
});
