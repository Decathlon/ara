/* eslint-env node */
require("@rushstack/eslint-patch/modern-module-resolution");

module.exports = {
  root: true,
  extends: [
    "plugin:vue/vue3-essential",
    "eslint:recommended",
    "@vue/eslint-config-typescript",
  ],
  env: {
    "vue/setup-compiler-macros": true,
    node: true,
  },
  parserOptions: {
    ecmaVersion: 2020,
  },
};
