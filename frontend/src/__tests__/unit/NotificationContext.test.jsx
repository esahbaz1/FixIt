// frontend/src/__tests__/unit/NotificationContext.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, act, waitFor } from "@testing-library/react";
import { NotificationProvider } from "../../context/NotificationProvider";
import { useNotifications } from "../../context/useNotifications";
import { AuthContext } from "../../context/AuthContext";
import { io, __mockSocket } from "socket.io-client";

// ─── Helper – renderira komponentu s potrebnim provajderima ────────────────

const mockUser = { id: 42, ime: "Test", uloga: "GRADJANIN", email: "test@test.com" };

function Wrapper({ children }) {
  const authCtx = { user: mockUser, logout: vi.fn(), showToast: vi.fn() };
  return (
    <AuthContext.Provider value={authCtx}>
      <NotificationProvider>{children}</NotificationProvider>
    </AuthContext.Provider>
  );
}

// Komponenta koja eksponira context vrijednosti za asercije
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

// ─── Testovi ────────────────────────────────────────────────────────────────

describe("NotificationContext", () => {
  beforeEach(() => {
    // Resetuj mock fetch za svaki test
    global.fetch.mockResolvedValue({
      ok: true, status: 200,
      json: () => Promise.resolve({ brojNeprocitanih: 3 }),
    });
    vi.clearAllMocks();
  });

  it("učitava inicijalni broj nepročitanih via REST", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => {
      expect(screen.getByTestId("count").textContent).toBe("3");
    });
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/notifikacije/korisnik/42/broj-neprocitanih"),
      expect.any(Object)
    );
  });

  it("povećava count na socket event 'nova-notifikacija'", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("3"));

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { naslov: "Test notif", tip: "NOVA_PRIJAVA" });
    });

    expect(screen.getByTestId("count").textContent).toBe("4");
  });

  it("prikazuje live toast na socket event", async () => {
    render(<Consumer />, { wrapper: Wrapper });

    act(() => {
      __mockSocket._trigger("nova-notifikacija", { naslov: "Nova prijava kreirana", tip: "NOVA_PRIJAVA" });
    });

    await waitFor(() => {
      expect(screen.getByTestId("toast").textContent).toBe("Nova prijava kreirana");
    });
  });

  it("resetCount postavlja count na 0", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("3"));

    act(() => { screen.getByText("reset").click(); });

    expect(screen.getByTestId("count").textContent).toBe("0");
  });

  it("decrementBy smanjuje count, ne ispod 0", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(screen.getByTestId("count").textContent).toBe("3"));

    act(() => { screen.getByText("dec").click(); });
    expect(screen.getByTestId("count").textContent).toBe("2");

    // Spusti na 0, pa još jednom – ne smije ići ispod 0
    act(() => { screen.getByText("dec").click(); });
    act(() => { screen.getByText("dec").click(); });
    act(() => { screen.getByText("dec").click(); }); // 4. klik
    expect(screen.getByTestId("count").textContent).toBe("0");
  });

  it("ne kreira socket konekciju bez korisnika", () => {
    function EmptyWrapper({ children }) {
      return (
        <AuthContext.Provider value={null}>
          <NotificationProvider>{children}</NotificationProvider>
        </AuthContext.Provider>
      );
    }
    render(<Consumer />, { wrapper: EmptyWrapper });
    expect(io).not.toHaveBeenCalled();
  });

  it("kreira socket konekciju s ispravnim userId auth parametrom", async () => {
    render(<Consumer />, { wrapper: Wrapper });
    await waitFor(() => expect(io).toHaveBeenCalled());
    const callArgs = io.mock.calls[0];
    expect(callArgs[1]).toMatchObject({ auth: { userId: "42" } });
  });
});
