import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import type { UserConfig as VitestUserConfigInterface } from 'vitest/config';

const vitestConfig: VitestUserConfigInterface = {
   test: {}
 };
 
 export default defineConfig({
   plugins: [vue()],
   test: {
      globals: true,
      includeSource: ["src/**/*.ts"],
      reporters: ["verbose"],
      environment: "jsdom",
   }
 });