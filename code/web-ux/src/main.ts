import { createApp } from "vue";
import router from "./router";
import { createPinia } from "pinia";
import App from "./App.vue";

// VITAMIN CSS
import "@vtmn/css/dist/index.css";
import "@vtmn/vue/dist/style.css";
// import "@vtmn/css-utilities/dist/index.css"; /* import Vitamin utilities CSS classes (required) */
import "@vtmn/css-chip";

// VITAMIN FONT
import "typeface-roboto";
import "typeface-roboto-condensed";

//VITAMIN COLORS
import "@vtmn/css-design-tokens/dist/index.css";

// VITAMIN ICONS
import "@vtmn/icons/dist/vitamix/font/vitamix.css";

// GLOBAL CSS FILE
import "./assets/css/global.css";

const pinia = createPinia();
const app = createApp(App);

app.use(router);
app.use(pinia);

app.mount("#app");
