// frontend/src/__tests__/setup.js
import "@testing-library/jest-dom";
import React from "react";


global.React = React;

// ─── Mock: socket.io-client ──────────────────────────────────────────────────
// Sprječava stvarne mrežne konekcije. Eksponira __mockSocket za ručno triggeranje
// eventa u testovima koji to trebaju.

const listeners = {};
export const __mockSocket = {
  on:         vi.fn((event, cb) => { listeners[event] = cb; }),
  off:        vi.fn((event)     => { delete listeners[event]; }),
  emit:       vi.fn(),
  disconnect: vi.fn(),
  connected:  true,
  _trigger:   (event, data) => listeners[event]?.(data),
};

vi.mock("socket.io-client", () => ({
  default: vi.fn(() => __mockSocket),
  io:      vi.fn(() => __mockSocket),
}));

// ─── Mock: fetch ─────────────────────────────────────────────────────────────
// Svaki test koji treba specifičan odgovor override-uje ovaj default s
// vi.mocked(fetch).mockResolvedValueOnce(...)
global.fetch = vi.fn(() =>
  Promise.resolve({
    ok:     true,
    status: 200,
    json:   () => Promise.resolve({}),
  })
);
