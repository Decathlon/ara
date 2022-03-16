module.exports = {
  content: ["./src/**/*.{html,js,vue}"], // Make sure 'vue' is added here since this is a Vue project
  theme: {
    extend: {},
  },
  plugins: [require("@tailwindcss/forms")],
  presets: [require("@vtmn/css-tailwind-preset")], //TO USE VITAMIN WITH TAILWIND
};
