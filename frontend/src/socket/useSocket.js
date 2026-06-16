// frontend/src/socket/useSocket.js


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

    
    _socket = io(window.location.origin, {
      path: "/socket.io",
      query: { userId: String(user.id) },
      reconnection: true,
      reconnectionDelay: 3000,
      reconnectionDelayMax: 15000,
      reconnectionAttempts: 20,
      transports: ["polling", "websocket"], // polling prvi — potreban za handshake kroz Vite proxy
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