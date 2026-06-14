import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // REST API
      "/api": {
        target: "http://127.0.0.1:8080",
        changeOrigin: true,
        secure: false,
      },
      // Socket.IO – proksiraj sve /socket.io/ zahtjeve na port 9001
      // Ovo rješava CORS jer browser vidi samo localhost:5173.
      // ISPRAVKA: dodan rewriteWsOrigin: true kako Vite ne bi
      // modificirao Origin header tokom WebSocket upgrade-a,
      // što može uzrokovati odbijanje konekcije od strane netty-socketio.
      "/socket.io": {
        target: "http://127.0.0.1:9001",
        changeOrigin: true,
        secure: false,
        ws: true,             // WebSocket upgrade
        rewriteWsOrigin: true, // ISPRAVKA: sačuvaj originalni Origin header
      },
    },
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: "./src/__tests__/setup.js",
  },
});