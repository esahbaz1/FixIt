// frontend/src/__tests__/integration/socketNotifications.test.jsx
//
// Integracijski test koji simulira cijeli tok:
//   Korisnik prijavljen → socket spajan → server šalje event →
//   badge se povećava → toast se prikazuje → reset pri odlasku na stranicu.

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, act, waitFor, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import { NotificationProvider } from "../../context/NotificationProvider";
import { useNotifications } from "../../context/useNotifications";
import { io } from "socket.io-client";
import { __mockSocket } from "../setup.js";

vi.mock("../../api/client", () => ({
  apiCall: vi.fn().mockResolvedValue({ brojNeprocitanih: 2 }),
}));

const user = { id: 10, ime: "Emir", uloga: "GRADJANIN", email: "emir@test.com" };

function App() {
  const { notifCount, resetCount, liveToast } = useNotifications();
  return (
    <div>
      <span data-testid="badge">{notifCount}</span>
      <span data-testid="toast-title">{liveToast?.naslov ?? ""}</span>
      <button onClick={resetCount} data-testid="go-notif">Obavijesti</button>
    </div>
  );
}

function FullWrapper({ children }) {
  return (
    <MemoryRouter>
      <AuthContext.Provider value={{ user, logout: vi.fn(), showToast: vi.fn() }}>
        <NotificationProvider>{children}</NotificationProvider>
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

describe("Socket.IO → UI integracija", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("badge se inicijalizira iz REST-a, socket push ga povećava", async () => {
    render(<App />, { wrapper: FullWrapper });
    await waitFor(() => expect(screen.getByTestId("badge").textContent).toBe("2"));

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { id: 99, naslov: "Nova prijava stigla", tip: "NOVA_PRIJAVA" });
    });

    expect(screen.getByTestId("badge").textContent).toBe("3");
  });

  it("live toast se prikazuje i sadrži naslov notifikacije", async () => {
    render(<App />, { wrapper: FullWrapper });

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { id: 100, naslov: "Promjena statusa prijave", tip: "STATUS_PROMJENA" });
    });

    expect(screen.getByTestId("toast-title").textContent).toBe("Promjena statusa prijave");
  });

  it("više uzastopnih pusheva ispravno akumuliraju count", async () => {
    render(<App />, { wrapper: FullWrapper });
    await waitFor(() => expect(screen.getByTestId("badge").textContent).toBe("2"));

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { id: 1, naslov: "A", tip: "NOVA_PRIJAVA" });
      __mockSocket._trigger("nova-notifikacija", { id: 2, naslov: "B", tip: "NOVA_PRIJAVA" });
      __mockSocket._trigger("nova-notifikacija", { id: 3, naslov: "C", tip: "NOVI_KOMENTAR" });
    });

    expect(screen.getByTestId("badge").textContent).toBe("5");
  });

  it("resetCount (klik na Obavijesti) postavlja badge na 0", async () => {
    render(<App />, { wrapper: FullWrapper });
    await waitFor(() => expect(screen.getByTestId("badge").textContent).toBe("2"));

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { id: 5, naslov: "Test", tip: "UPOZORENJE" });
    });
    expect(screen.getByTestId("badge").textContent).toBe("3");

    fireEvent.click(screen.getByTestId("go-notif"));
    expect(screen.getByTestId("badge").textContent).toBe("0");
  });

  it("socket se kreira s ispravnim auth.userId", async () => {
    render(<App />, { wrapper: FullWrapper });
    await waitFor(() => expect(io).toHaveBeenCalled());
    expect(io.mock.calls[0][1].auth).toEqual({ userId: "10" });
  });

  it("notifikacija tipa RIJESENO prikazuje naslov u toastu", async () => {
    render(<App />, { wrapper: FullWrapper });

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { id: 200, naslov: "Vaša prijava je riješena!", tip: "RIJESENO" });
    });

    expect(screen.getByTestId("toast-title").textContent).toBe("Vaša prijava je riješena!");
  });

  it("novi korisnik (drugačiji id) kreira novu socket konekciju", async () => {
    const { rerender } = render(<App />, { wrapper: FullWrapper });
    await waitFor(() => expect(io).toHaveBeenCalledTimes(1));

    // Simuliramo promjenu korisnika
    function AnotherWrapper({ children }) {
      return (
        <MemoryRouter>
          <AuthContext.Provider value={{ user: { ...user, id: 99 }, logout: vi.fn(), showToast: vi.fn() }}>
            <NotificationProvider>{children}</NotificationProvider>
          </AuthContext.Provider>
        </MemoryRouter>
      );
    }
    rerender(<AnotherWrapper><App /></AnotherWrapper>);

    await waitFor(() => expect(io).toHaveBeenCalledTimes(2));
    expect(io.mock.calls[1][1].auth).toEqual({ userId: "99" });
  });
});
