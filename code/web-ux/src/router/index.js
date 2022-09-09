import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/loginForm.vue";
import Dashboard from "../views/dashboardHome.vue";
import Issues from "../views/issues.vue";
import Features from "../views/features.vue";
import Settings from "../views/settings.vue";
import qualitySettings from "../views/qualitySettings.vue";
import Projects from "../views/projects.vue";
import FAQ from "../views/faq.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "login",
      component: Login,
    },
    {
      path: "/quality-validation",
      name: "quality validation",
      component: Dashboard,
    },
    {
      path: "/issues",
      name: "issues",
      component: Issues,
    },
    {
      path: "/features",
      name: "features",
      component: Features,
    },
    {
      path: "/settings",
      name: "settings",
      component: Settings,
    },
    {
      path: "/settings/qualitySettings",
      name: "qualitySettings",
      component: qualitySettings,
    },
    {
      path: "/projects",
      name: "projects",
      component: Projects,
    },
    {
      path: "/faq",
      name: "faq",
      component: FAQ,
    },
  ],
});

export default router;
