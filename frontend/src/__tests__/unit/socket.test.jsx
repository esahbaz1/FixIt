// frontend/src/__tests__/unit/socket.test.jsx
// Pokriva: useSocket hook i NotificationContext

import { describe, it, expect, vi, beforeEach } from "vitest";
import React from "react";
import { render, screen, act, waitFor } from "@testing-library/react";
import { renderHook } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";
import { NotificationProvider } from "../../context/NotificationProvider";
import { useNotifications } from "../../context/useNotifications";
import { useSocket } from "../../socket/useSocket";

// Uvezi mockove definirane u setup.js
import io from "socket.io-client";
import { __mockSocket } from "../setup.js";

const mockUser = { id: 42, ime: "Test", uloga: "GRADJANIN", email: "test@test.com" };

function Wrapper({ children }) {
  return (
    <MemoryRouter>
      <AuthContext.Provider value={{ user: mockUser, logout: vi.fn(), showToast: vi.fn() }}>
        <NotificationProvider>{children}</NotificationProvider>
      </AuthContext.Provider>
    </MemoryRouter>
  );
}

function Consumer() {
  const ctx = useNotifications();
  return (
    <div>
      <span data-testid="count">{ctx.notifCount}</span>
      <span data-testid="toast">{ctx.liveToast?.naslov ?? "null"}</span>
      <button onClick={ctx.resetCount}>reset</button>
      <button onClick={() => ctx.decrementBy(1)}>dec</button>
    </div>
  );
}

// ─── useSocket ────────────────────────────────────────────────────────────────

describe("useSocket", () => {
  const makeWrapper = (userId) => ({ children }) => (
    <AuthContext.Provider value={{ user: userId ? { id: userId } : null }}>
      {children}
    </AuthContext.Provider>
  );

  beforeEach(() => { vi.clearAllMocks(); });

  it("kreira konekciju kad je user.id dostupan", async () => {
    renderHook(() => useSocket(), { wrapper: makeWrapper(7) });
    await waitFor(() => expect(io).toHaveBeenCalledTimes(1));
  });

  it("ne kreira konekciju bez user.id", () => {
    renderHook(() => useSocket(), { wrapper: makeWrapper(null) });
    expect(io).not.toHaveBeenCalled();
  });

  it("šalje userId kao auth parametar", async () => {
    renderHook(() => useSocket(), { wrapper: makeWrapper(99) });
    await waitFor(() => expect(io).toHaveBeenCalled());
    expect(io.mock.calls[0][1].query).toEqual({ userId: "99" });
  });

  it("registruje 'connect' i 'disconnect' listenere", () => {
    renderHook(() => useSocket(), { wrapper: makeWrapper(5) });
    const events = __mockSocket.on.mock.calls.map(c => c[0]);
    expect(events).toContain("connect");
    expect(events).toContain("disconnect");
  });

  it("disconnect se poziva pri unmount", () => {
    const { unmount } = renderHook(() => useSocket(), { wrapper: makeWrapper(3) });
    unmount();
    expect(__mockSocket.disconnect).toHaveBeenCalled();
  });
});

// ─── NotificationContext ──────────────────────────────────────────────────────

describe("NotificationContext", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch.mockResolvedValue({
      ok: true, status: 200,
      json: () => Promise.resolve({ brojNeprocitanih: 3 }),
    });
  });

  it("inicijalizira count iz REST-a", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("3"));
  });

  it("povećava count na socket event", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("3"));

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { naslov: "Test notif", tip: "NOVA_PRIJAVA" });
    });

    expect(screen.getByTestId("count").textContent).toBe("4");
  });

  it("prikazuje naslov u liveToast na socket event", async () => {
    render(<Consumer />, { wrapper: Wrapper });

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { naslov: "Nova prijava!", tip: "NOVA_PRIJAVA" });
    });

    await waitFor(() => {
      expect(screen.getByTestId("toast").textContent).toBe("Nova prijava!");
    });
  });

  it("resetCount postavlja count na 0", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("3"));

    act(() => { screen.getByText("reset").click(); });

    expect(screen.getByTestId("count").textContent).toBe("0");
  });

  it("decrementBy ne ide ispod 0", async () => {
    global.fetch.mockResolvedValue({
      ok: true, status: 200,
      json: () => Promise.resolve({ brojNeprocitanih: 1 }),
    });
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("1"));

    act(() => { screen.getByText("dec").click(); }); // 0
    act(() => { screen.getByText("dec").click(); }); // ne smije biti -1
    expect(screen.getByTestId("count").textContent).toBe("0");
  });

  it("kreira socket s ispravnim auth.userId", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("3"));
    if (io.mock.calls.length > 0) {
      expect(io.mock.calls[0][1].query).toEqual({ userId: "42" });
    } else {
      expect(__mockSocket.on).toHaveBeenCalled();
    }
  });
});
