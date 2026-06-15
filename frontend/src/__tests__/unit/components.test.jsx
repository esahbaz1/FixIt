// frontend/src/__tests__/unit/components.test.jsx
// Pokriva: Toast, StatusChip, PrioChip, Spinner

import { describe, it, expect, vi, beforeEach } from "vitest";
import React from "react";
import { render, screen, waitFor, act } from "@testing-library/react";
import Toast from "../../components/Toast";
import { StatusChip, PrioChip } from "../../components/Chips";
import Spinner from "../../components/Spinner";

// ─── Toast ────────────────────────────────────────────────────────────────────

describe("Toast", () => {
  beforeEach(() => { vi.useFakeTimers(); });
  afterEach(() => { vi.useRealTimers(); });

  it("renderira poruku", () => {
    render(<Toast message="Operacija uspješna!" type="success" onDone={vi.fn()} />);
    expect(screen.getByText("Operacija uspješna!")).toBeInTheDocument();
  });

  it("poziva onDone nakon 3.5 sekunde", async () => {
    const onDone = vi.fn();
    render(<Toast message="test" type="success" onDone={onDone} />);
    act(() => { vi.advanceTimersByTime(3500); });
    expect(onDone).toHaveBeenCalledTimes(1);
  });

  it("ne poziva onDone prije isteka timera", () => {
    const onDone = vi.fn();
    render(<Toast message="test" type="error" onDone={onDone} />);
    act(() => { vi.advanceTimersByTime(1000); });
    expect(onDone).not.toHaveBeenCalled();
  });

  it("renderira i za tip 'error'", () => {
    render(<Toast message="Greška!" type="error" onDone={vi.fn()} />);
    expect(screen.getByText("Greška!")).toBeInTheDocument();
  });
});

// ─── StatusChip ───────────────────────────────────────────────────────────────

describe("StatusChip", () => {
  it("prikazuje tekst statusa", () => {
    render(<StatusChip status="Novo" />);
    expect(screen.getByText("Novo")).toBeInTheDocument();
  });

  it("prikazuje '—' za undefined status", () => {
    render(<StatusChip />);
    expect(screen.getByText("—")).toBeInTheDocument();
  });

  it("renderira sve poznate statuse bez crash-a", () => {
    const statusi = ["Novo", "Dodijeljeno", "U radu", "Rijeseno", "Zatvoreno"];
    statusi.forEach(s => {
      const { unmount } = render(<StatusChip status={s} />);
      expect(screen.getByText(s)).toBeInTheDocument();
      unmount();
    });
  });
});

// ─── PrioChip ─────────────────────────────────────────────────────────────────

describe("PrioChip", () => {
  it("prikazuje 'Hitno' za HITNO prioritet", () => {
    render(<PrioChip priority="HITNO" />);
    expect(screen.getByText("Hitno")).toBeInTheDocument();
  });

  it("prikazuje '—' za nepoznat prioritet", () => {
    render(<PrioChip priority="NEPOSTOJI" />);
    expect(screen.getByText("—")).toBeInTheDocument();
  });

  it("renderira sve poznate prioritete bez crash-a", () => {
    const prios = ["HITNO", "VISOK", "SREDNJI", "NIZAK"];
    prios.forEach(p => {
      const { unmount } = render(<PrioChip priority={p} />);
      unmount();
    });
  });
});

// ─── Spinner ──────────────────────────────────────────────────────────────────

describe("Spinner", () => {
  it("renderira bez crash-a s default propovima", () => {
    const { container } = render(<Spinner />);
    expect(container.firstChild).toBeInTheDocument();
  });

  it("prihvata size prop bez crash-a", () => {
    const { container } = render(<Spinner size={32} />);
    expect(container.firstChild).toBeInTheDocument();
  });
});
