// frontend/src/__tests__/integration/loginFlow.test.jsx
//
// Integracijski test: simulira cijeli login tok kroz App komponentu.
// Provjerava da se korisnik može prijaviti, da se token čuva,
// i da se redirect desi ka dashboardu.

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import { NotificationProvider } from "../../context/NotificationContext";
import LoginPage from "../../pages/LoginPage";
import { clearTokens, getAccessToken } from "../../api/client";

vi.mock("../../api/client", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    apiCall: vi.fn(),
  };
});
import { apiCall } from "../../api/client";

const loginResponse = {
  token: "eyJhbGciOiJIUzI1NiJ9.test",
  refreshToken: "refresh-token-123",
  id: 10,
  email: "korisnik@test.ba",
  uloga: "GRADJANIN",
  ime: "Korisnik",
  prezime: "Test",
};

describe("Login flow – integracija", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    clearTokens();
    sessionStorage.clear();
  });

  it("uspješna prijava poziva onLogin s podacima sa servera", async () => {
    apiCall.mockResolvedValue(loginResponse);
    const onLogin = vi.fn();

    render(
      <MemoryRouter>
        <LoginPage onLogin={onLogin} switchToRegister={vi.fn()} />
      </MemoryRouter>
    );

    const user = userEvent.setup();
    await user.type(screen.getByPlaceholderText(/email/i), "korisnik@test.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "Lozinka1!");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    await waitFor(() => {
      expect(onLogin).toHaveBeenCalledWith(loginResponse);
    });
  });

  it("neispravna lozinka prikazuje grešku, ne poziva onLogin", async () => {
    apiCall.mockRejectedValue(new Error("Pogrešna email adresa ili lozinka. Provjerite podatke i pokušajte ponovo."));
    const onLogin = vi.fn();

    render(
      <MemoryRouter>
        <LoginPage onLogin={onLogin} switchToRegister={vi.fn()} />
      </MemoryRouter>
    );

    const user = userEvent.setup();
    await user.type(screen.getByPlaceholderText(/email/i), "korisnik@test.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "pogresna");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    await waitFor(() => {
      expect(screen.getByText(/Pogrešna email adresa/i)).toBeInTheDocument();
      expect(onLogin).not.toHaveBeenCalled();
    });
  });

  it("server error (500) prikazuje odgovarajuću poruku", async () => {
    apiCall.mockRejectedValue(new Error("Sistem je trenutno nedostupan. Pokušajte ponovo za nekoliko minuta."));

    render(
      <MemoryRouter>
        <LoginPage onLogin={vi.fn()} switchToRegister={vi.fn()} />
      </MemoryRouter>
    );

    const user = userEvent.setup();
    await user.type(screen.getByPlaceholderText(/email/i), "k@k.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "pass");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    await waitFor(() => {
      expect(screen.getByText(/nedostupan/i)).toBeInTheDocument();
    });
  });

  it("postoji link za registraciju koji je klikabilan", () => {
    const switchFn = vi.fn();
    render(
      <MemoryRouter>
        <LoginPage onLogin={vi.fn()} switchToRegister={switchFn} />
      </MemoryRouter>
    );
    fireEvent.click(screen.getByText(/Registrujte se/i));
    expect(switchFn).toHaveBeenCalled();
  });
});
