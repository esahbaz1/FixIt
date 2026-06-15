// frontend/src/__tests__/unit/LoginPage.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import LoginPage from "../../pages/LoginPage";

vi.mock("../../api/client", () => ({ apiCall: vi.fn() }));
import { apiCall } from "../../api/client";

const onLogin       = vi.fn();
const switchToReg   = vi.fn();

function renderLogin() {
  return render(
    <MemoryRouter>
      <LoginPage onLogin={onLogin} switchToRegister={switchToReg} />
    </MemoryRouter>
  );
}

describe("LoginPage", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("renderira email i lozinka polja", () => {
    renderLogin();
    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/••••/)).toBeInTheDocument();
  });

  it("renderira dugme 'Prijavi se'", () => {
    renderLogin();
    expect(screen.getByRole("button", { name: /Prijavi se/i })).toBeInTheDocument();
  });

  it("poziva apiCall s ispravnim podacima pri submitu", async () => {
    apiCall.mockResolvedValue({ token: "t", refreshToken: "r", id: 1, uloga: "GRADJANIN" });
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/email/i), "test@test.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "lozinka123");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    await waitFor(() => {
      expect(apiCall).toHaveBeenCalledWith(
        "/api/auth/prijava",
        expect.objectContaining({ method: "POST" })
      );
    });
  });

  it("poziva onLogin callback pri uspješnoj prijavi", async () => {
    const fakeData = { token: "t", refreshToken: "r", id: 5, uloga: "GRADJANIN", ime: "Ana" };
    apiCall.mockResolvedValue(fakeData);
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/email/i), "ana@test.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "pass");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    await waitFor(() => expect(onLogin).toHaveBeenCalledWith(fakeData));
  });

  it("prikazuje grešku pri neuspješnoj prijavi", async () => {
    apiCall.mockRejectedValue(new Error("Pogrešna email adresa ili lozinka."));
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/email/i), "bad@bad.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "pogresno");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    await waitFor(() => {
      expect(screen.getByText(/Pogrešna email adresa/i)).toBeInTheDocument();
    });
  });

  it("ne poziva onLogin pri grešci", async () => {
    apiCall.mockRejectedValue(new Error("greška"));
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/email/i), "x@x.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "xxx");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    await waitFor(() => expect(screen.getByText("greška")).toBeInTheDocument());
    expect(onLogin).not.toHaveBeenCalled();
  });

  it("klik na 'Registrujte se' poziva switchToRegister", async () => {
    renderLogin();
    await userEvent.setup().click(screen.getByText(/Registrujte se/i));
    expect(switchToReg).toHaveBeenCalled();
  });

  it("submit dugme je disabled dok traje zahtjev", async () => {
    apiCall.mockReturnValue(new Promise(() => {})); 
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/email/i), "x@x.ba");
    await user.type(screen.getByPlaceholderText(/••••/), "pass");
    await user.click(screen.getByRole("button", { name: /Prijavi se/i }));

    expect(screen.getByRole("button", { name: /Provjera/i })).toBeDisabled();
  });
});
