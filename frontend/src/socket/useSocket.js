// frontend/src/socket/useSocket.js
// netty-socketio 2.0.6 koristi EIO=3 protokol (socket.io v2/v3).
// socket.io-client v4 šalje EIO=4 koji server ne prepoznaje → parse error.
// Rješenje: socket.io-client v2.5 koji govori EIO=3 – kompatibilan s netty-socketio 2.x.
//
// Vite proxy preusmjerava /socket.io/* na localhost:9001 -> nema CORS.

import { useEffect, useRef } from "react";
import io from "socket.io-client";
import { useAuth } from "../context/AuthContext";

let _socket = null;
let _currentUserId = null;

export function useSocket() {
  const { user } = useAuth();
  const socketRef = useRef(null);

  useEffect(() => {
    if (!user?.id) {
      if (_socket) {
        _socket.disconnect();
        _socket = null;
        _currentUserId = null;
      }
      socketRef.current = null;
      return;
    }

    if (_socket && _currentUserId === user.id) {
      socketRef.current = _socket;
      return;
    }

    if (_socket) {
      _socket.disconnect();
      _socket = null;
    }

    _currentUserId = user.id;

    // socket.io-client v2: userId se šalje kao query parametar.
    // Nema "auth" opcije u v2 – direktno kroz "query".
    // Koristimo samo "websocket" transport da izbjegnemo polling/upgrade
    // race condition kroz Vite proxy.
    _socket = io(window.location.origin, {
      path: "/socket.io",
      query: { userId: String(user.id) },
      reconnection: true,
      reconnectionDelay: 3000,
      reconnectionDelayMax: 15000,
      reconnectionAttempts: 20,
      transports: ["websocket"],
    });

    socketRef.current = _socket;

    _socket.on("connect", () =>
      console.log("[SocketIO] connected ✓ id:", _socket.id)
    );
    _socket.on("disconnect", (r) =>
      console.log("[SocketIO] disconnected:", r)
    );
    _socket.on("connect_error", (e) =>
      console.error("[SocketIO] error:", e.message, e)
    );

    return () => { socketRef.current = null; };
  }, [user?.id]);

  return socketRef;
}