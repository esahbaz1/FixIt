// frontend/src/__tests__/unit/useSocket.test.jsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { io, __mockSocket } from "socket.io-client";
import { useSocket } from "../../socket/useSocket";
import { AuthContext } from "../../context/AuthContext";

const makeWrapper = (userId) => ({ children }) => (
  <AuthContext.Provider value={{ user: userId ? { id: userId } : null }}>
    {children}
  </AuthContext.Provider>
);

describe("useSocket", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("kreira socket konekciju kad je user.id dostupan", () => {
    renderHook(() => useSocket(), { wrapper: makeWrapper(7) });
    expect(io).toHaveBeenCalledTimes(1);
  });

  it("ne kreira konekciju bez user.id", () => {
    renderHook(() => useSocket(), { wrapper: makeWrapper(null) });
    expect(io).not.toHaveBeenCalled();
  });

  it("šalje userId kao auth parametar", () => {
    renderHook(() => useSocket(), { wrapper: makeWrapper(99) });
    const opts = io.mock.calls[0][1];
    expect(opts.auth).toEqual({ userId: "99" });
  });

  it("registruje connect i disconnect event listenere", () => {
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
