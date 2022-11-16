import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/loginForm.vue";
import Dashboard from "../views/dashboardHome.vue";
import cardDetails from "../views/qualityDetails.vue";
import Issues from "../views/issues.vue";
import Features from "../views/features.vue";
import qualitySettings from "../views/qualitySettings.vue";
import qualityPositions from "../views/qualityPositions.vue";
import qualityConfiguration from "../views/qualityConfiguration.vue";
import qualityCompletion from "../views/qualityCompletion.vue";
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
      name: "qualityValidation",
      component: Dashboard,
    },
    {
      path: "/quality-details",
      name: "qualityDetails",
      component: cardDetails,
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
      component: qualitySettings,
      children: [
        {
          path: "qualityconfiguration",
          name: "qualityConfiguration",
          component: qualityConfiguration,
        },
        {
          path: "qualitypositions",
          name: "qualityPositions",
          component: qualityPositions,
        },
        {
          path: "completion&success",
          name: "qualityCompletion",
          component: qualityCompletion,
        },
      ],
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
