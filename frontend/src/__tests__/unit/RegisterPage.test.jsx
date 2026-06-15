// frontend/src/__tests__/unit/RegisterPage.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import RegisterPage from "../../pages/RegisterPage";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const onSuccess    = vi.fn();
const switchToLogin = vi.fn();

function renderReg() {
  return render(
    <MemoryRouter>
      <RegisterPage onSuccess={onSuccess} switchToLogin={switchToLogin} />
    </MemoryRouter>
  );
}

// ─── Helperi ─────────────────────────────────────────────────────────────────

async function popuni(user, { ime = "Ime", prezime = "Prezime", email = "test@test.ba", lozinka = "Jaka!1lozinka" } = {}) {
  const inputs = screen.getAllByRole("textbox");
  // Redoslijed u formi: ime, prezime, email (password je poseban tip)
  if (ime)     await user.type(inputs[0], ime);
  if (prezime) await user.type(inputs[1], prezime);
  if (email)   await user.type(inputs[2], email);
  if (lozinka) {
    const passInput = screen.getByPlaceholderText(/Min/i);
    await user.type(passInput, lozinka);
  }
}

describe("RegisterPage – validacija", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("prikazuje grešku za nevažeći email format", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { email: "nevaljan-email" });
    const emailInput = screen.getAllByRole("textbox")[2];
    emailInput.required = false; // disable native HTML validation for test
    const form = document.querySelector('form');
    if (form) form.noValidate = true;
    await user.click(screen.getByRole("button", { name: /Kreiraj nalog/i }));
    await waitFor(() => {
      expect(screen.getByText("Unesite valjanu email adresu (npr. korisnik@domena.ba)")).toBeInTheDocument();
    });
    expect(apiCall).not.toHaveBeenCalled();
  });

  it("prikazuje grešku za lozinku kraću od 8 znakova", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "Krat1!" });
    const passInput = screen.getByPlaceholderText(/Min/i);
    passInput.required = false; // disable native HTML validation for test
    await user.click(screen.getByRole("button", { name: /Kreiraj nalog/i }));
    await waitFor(() => {
      expect(screen.getByText("Lozinka mora imati najmanje 8 karaktera.")).toBeInTheDocument();
    });
  });

  it("prikazuje grešku ako lozinka nema veliko slovo", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "malalozinka1!" });
    await user.click(screen.getByRole("button", { name: /Kreiraj nalog/i }));
    await waitFor(() => {
      expect(screen.getByText("Lozinka mora sadržavati barem jedno veliko slovo.")).toBeInTheDocument();
    });
  });

  it("prikazuje grešku ako lozinka nema broj", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "Bezbroja!lozinka" });
    await user.click(screen.getByRole("button", { name: /Kreiraj nalog/i }));
    await waitFor(() => {
      expect(screen.getByText("Lozinka mora sadržavati barem jedan broj.")).toBeInTheDocument();
    });
  });

  it("prikazuje grešku ako lozinka nema specijalni karakter", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "Bezspecijala1" });
    await user.click(screen.getByRole("button", { name: /Kreiraj nalog/i }));
    await waitFor(() => {
      expect(screen.getByText("Lozinka mora sadržavati barem jedan specijalni karakter.")).toBeInTheDocument();
    });
  });

  it("uspješna registracija poziva apiCall i onSuccess", async () => {
    apiCall.mockResolvedValue({});
    const user = userEvent.setup();
    renderReg();
    await popuni(user);
    await user.click(screen.getByRole("button", { name: /Kreiraj nalog/i }));
    await waitFor(() => expect(onSuccess).toHaveBeenCalled());
  });

  it("klik na 'Prijavite se' poziva switchToLogin", async () => {
    renderReg();
    await userEvent.setup().click(screen.getByText(/Prijavite se/i));
    expect(switchToLogin).toHaveBeenCalled();
  });
});
