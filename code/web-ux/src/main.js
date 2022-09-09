import { createApp } from "vue";
import router from "./router";
import store from "./store";
import App from "./App.vue";

// GLOBAL CSS FILE
import "./assets/css/global.css";

// VITAMIN CSS
import "@vtmn/css/dist/index.css";
import "@vtmn/vue/dist/style.css";
import "@vtmn/css-utilities/dist/index.css"; /* import Vitamin utilities CSS classes (required) */
import "@vtmn/css-chip/dist/index.css";

// VITAMIN FONT
import "typeface-roboto";
import "typeface-roboto-condensed";

//VITAMIN COLORS
import "@vtmn/css-design-tokens/dist/index.css";

// VITAMIN ICONS
import "@vtmn/icons/dist/vitamix/font/vitamix.css";

const app = createApp(App);

app.use(router);
app.use(store);

app.mount("#app");
