// frontend/src/__tests__/unit/RegisterPage.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
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
  // Redosljed u formi: ime, prezime, email (password je poseban tip)
  if (ime)     await user.type(inputs[0], ime);
  if (prezime) await user.type(inputs[1], prezime);
  if (email)   await user.type(inputs[2], email);
  if (lozinka) {
    const passInput = screen.getByPlaceholderText(/••••/);
    await user.type(passInput, lozinka);
  }
}

describe("RegisterPage – validacija", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("prikazuje grešku za nevažeći email format", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { email: "nevaljan-email" });
    await user.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/valjanu email adresu/i)).toBeInTheDocument();
    });
    expect(apiCall).not.toHaveBeenCalled();
  });

  it("prikazuje grešku za lozinku kraću od 8 znakova", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "Krat1!" });
    await user.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/8 karaktera/i)).toBeInTheDocument();
    });
  });

  it("prikazuje grešku ako lozinka nema veliko slovo", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "malalozinka1!" });
    await user.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/veliko slovo/i)).toBeInTheDocument();
    });
  });

  it("prikazuje grešku ako lozinka nema broj", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "Bezbroja!lozinka" });
    await user.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/jedan broj/i)).toBeInTheDocument();
    });
  });

  it("prikazuje grešku ako lozinka nema specijalni karakter", async () => {
    const user = userEvent.setup();
    renderReg();
    await popuni(user, { lozinka: "Bezspecijala1" });
    await user.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => {
      expect(screen.getByText(/specijalni karakter/i)).toBeInTheDocument();
    });
  });

  it("uspješna registracija poziva apiCall i onSuccess", async () => {
    apiCall.mockResolvedValue({});
    const user = userEvent.setup();
    renderReg();
    await popuni(user);
    await user.click(screen.getByRole("button", { name: /Registruj/i }));
    await waitFor(() => expect(onSuccess).toHaveBeenCalled());
  });

  it("klik na 'Prijavite se' poziva switchToLogin", async () => {
    renderReg();
    await userEvent.setup().click(screen.getByText(/Prijavite se/i));
    expect(switchToLogin).toHaveBeenCalled();
  });
});
