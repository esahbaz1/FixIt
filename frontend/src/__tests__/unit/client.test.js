// frontend/src/__tests__/unit/client.test.js
// Testira: friendlyError(), apiCall() – error handling, refresh flow, logout dispatch

import { describe, it, expect, vi, beforeEach } from "vitest";
import {
  friendlyError,
  setTokens,
  clearTokens,
  getAccessToken,
  saveUserToStorage,
  loadUserFromStorage,
  loadRefreshTokenFromStorage,
} from "../../api/client";

// ─── friendlyError ────────────────────────────────────────────────────────────

describe("friendlyError()", () => {
  it("500 vraća generičku poruku o nedostupnosti", () => {
    expect(friendlyError(500)).toContain("nedostupan");
  });

  it("503 vraća isti tekst kao 500", () => {
    expect(friendlyError(503)).toContain("nedostupan");
  });

  it("401 bez konteksta vraća poruku o autorizaciji", () => {
    const msg = friendlyError(401);
    expect(msg).toContain("autorizovani");
  });

  it("401 s kontekstom 'login' vraća poruku o pogrešnoj lozinki", () => {
    const msg = friendlyError(401, "", "login");
    expect(msg).toContain("lozinka");
  });

  it("403 vraća poruku o nedostatku dozvole", () => {
    expect(friendlyError(403)).toContain("dozvolu");
  });

  it("404 vraća poruku o pogrešnom unosu", () => {
    expect(friendlyError(404)).toContain("Pogrešan unos");
  });

  it("nepoznati status vraća fallback poruku ako je data", () => {
    expect(friendlyError(422, "Validacija pala")).toBe("Validacija pala");
  });

  it("nepoznati status bez fallbacka vraća HTTP N tekst", () => {
    expect(friendlyError(418)).toContain("HTTP 418");
  });
});

// ─── Token storage ────────────────────────────────────────────────────────────

describe("Token storage", () => {
  beforeEach(() => {
    clearTokens();
    sessionStorage.clear();
  });

  it("setTokens pamti access token u memoriji", () => {
    setTokens("my-token", "my-refresh");
    expect(getAccessToken()).toBe("my-token");
  });

  it("setTokens sprema refresh token u sessionStorage", () => {
    setTokens("tok", "ref-tok");
    expect(sessionStorage.getItem("fixit_refresh_token")).toBe("ref-tok");
  });

  it("clearTokens briše access token iz memorije", () => {
    setTokens("tok", "ref");
    clearTokens();
    expect(getAccessToken()).toBeNull();
  });

  it("clearTokens briše sessionStorage ključeve", () => {
    setTokens("tok", "ref");
    clearTokens();
    expect(sessionStorage.getItem("fixit_refresh_token")).toBeNull();
  });

  it("setTokens bez refresh argumenta ne briše postojeći refresh", () => {
    setTokens("first", "ref");
    setTokens("second"); // bez refresh
    expect(sessionStorage.getItem("fixit_refresh_token")).toBe("ref");
  });
});

// ─── User storage (sessionStorage helper) ────────────────────────────────────

describe("saveUserToStorage / loadUserFromStorage", () => {
  beforeEach(() => sessionStorage.clear());

  it("sprema i vraća korisničke podatke", () => {
    const u = { id: 1, email: "a@b.com", uloga: "GRADJANIN" };
    saveUserToStorage(u);
    expect(loadUserFromStorage()).toEqual(u);
  });

  it("vraća null ako nema ništa u sessionStorage", () => {
    expect(loadUserFromStorage()).toBeNull();
  });

  it("vraća null za oštećen JSON", () => {
    sessionStorage.setItem("fixit_user", "{ broken json }}}");
    expect(loadUserFromStorage()).toBeNull();
  });

  it("loadRefreshTokenFromStorage vraća token ili null", () => {
    expect(loadRefreshTokenFromStorage()).toBeNull();
    sessionStorage.setItem("fixit_refresh_token", "tok123");
    expect(loadRefreshTokenFromStorage()).toBe("tok123");
  });
});
