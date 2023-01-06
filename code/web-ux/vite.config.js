import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vitejs.dev/config/
export default defineConfig({
  define: {
    "process.env": {},
  },
  plugins: [
    vue({
      refresh: true,
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
      find: "@vue/runtime-core",
      replacement: "@vue/runtime-core/dist/runtime-core.esm-bundler.js",
    },
  },
});
