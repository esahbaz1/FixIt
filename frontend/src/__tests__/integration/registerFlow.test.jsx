// frontend/src/__tests__/integration/registerFlow.test.jsx
//
// Integracijski testovi za tok registracije:
// - Validacija polja (email, lozinka kompleksnost)
// - Uspješna registracija → callback
// - API greška se prikazuje

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import RegisterPage from "../../pages/RegisterPage";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

function renderReg(onSuccess = vi.fn(), switchToLogin = vi.fn()) {
  return render(
    <MemoryRouter>
      <RegisterPage onSuccess={onSuccess} switchToLogin={switchToLogin} />
    </MemoryRouter>
  );
}

// Helper koji popunjava formu
async function popuniFormu(user_, overrides = {}) {
  const defaults = {
    ime:     "Amira",
    prezime: "Kovač",
    email:   "amira@test.ba",
    lozinka: "Jaka!1Lozinka",
  };
  const v = { ...defaults, ...overrides };

  const textboxes = screen.getAllByRole("textbox");
  await user_.clear(textboxes[0]);
  await user_.type(textboxes[0], v.ime);
  await user_.clear(textboxes[1]);
  await user_.type(textboxes[1], v.prezime);
  await user_.clear(textboxes[2]);
  await user_.type(textboxes[2], v.email);

  const passInput = screen.getByPlaceholderText(/••••/);
  await user_.clear(passInput);
  await user_.type(passInput, v.lozinka);
}

describe("RegisterPage – integracija", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  // ─── Validacija email ─────────────────────────────────────────────────────

  it("email bez @ prikazuje validacijsku grešku", async () => {
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_, { email: "nevalidan-email" });
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/valjanu email adresu/i)).toBeInTheDocument();
    });
    expect(apiCall).not.toHaveBeenCalled();
  });

  it("email bez TLD-a prikazuje grešku", async () => {
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_, { email: "korisnik@domena" });
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/valjanu email adresu/i)).toBeInTheDocument();
    });
  });

  // ─── Validacija lozinke ───────────────────────────────────────────────────

  it("lozinka < 8 znakova prikazuje grešku", async () => {
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_, { lozinka: "Ab1!" });
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => expect(screen.getByText(/8 karaktera/i)).toBeInTheDocument());
  });

  it("lozinka bez velikog slova prikazuje grešku", async () => {
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_, { lozinka: "malalozing1!" });
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => expect(screen.getByText(/veliko slovo/i)).toBeInTheDocument());
  });

  it("lozinka bez broja prikazuje grešku", async () => {
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_, { lozinka: "BezBroja!" });
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => expect(screen.getByText(/jedan broj/i)).toBeInTheDocument());
  });

  it("lozinka bez specijalnog znaka prikazuje grešku", async () => {
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_, { lozinka: "BezSpecijala1" });
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => expect(screen.getByText(/specijalni karakter/i)).toBeInTheDocument());
  });

  // ─── Uspješna registracija ────────────────────────────────────────────────

  it("ispravni podaci pozivaju apiCall sa svim poljima", async () => {
    apiCall.mockResolvedValue({});
    const onSuccess = vi.fn();
    const user_ = userEvent.setup();
    renderReg(onSuccess);
    await popuniFormu(user_);
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledWith(
        expect.stringContaining("/registracija"),
        expect.objectContaining({ method: "POST" })
      );
    });
  });

  it("uspješna registracija poziva onSuccess callback", async () => {
    apiCall.mockResolvedValue({});
    const onSuccess = vi.fn();
    const user_ = userEvent.setup();
    renderReg(onSuccess);
    await popuniFormu(user_);
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => expect(onSuccess).toHaveBeenCalled());
  });

  // ─── API greška ───────────────────────────────────────────────────────────

  it("API greška (409 email postoji) prikazuje poruku", async () => {
    apiCall.mockRejectedValue(new Error("Korisnik sa ovim emailom već postoji."));
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_);
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/već postoji/i)).toBeInTheDocument();
    });
  });

  it("API server greška (500) prikazuje generalnu poruku", async () => {
    apiCall.mockRejectedValue(new Error("Sistem je trenutno nedostupan. Pokušajte ponovo za nekoliko minuta."));
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_);
    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/nedostupan/i)).toBeInTheDocument();
    });
  });

  it("dupli submit ne šalje dva zahtjeva (dugme disabled za trajanja)", async () => {
    let resolveApi;
    apiCall.mockReturnValue(new Promise(res => { resolveApi = res; }));
    const user_ = userEvent.setup();
    renderReg();
    await popuniFormu(user_);

    await user_.click(screen.getByRole("button", { name: /Registruj/i }));
    // Dugme treba biti disabled dok traje request
    expect(screen.getByRole("button", { name: /Registruj/i })).toBeDisabled();

    // Resolve pa provjeri da nije 2x pozvan
    resolveApi({});
    await waitFor(() => expect(apiCall).toHaveBeenCalledTimes(1));
  });
});
