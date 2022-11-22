import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/loginForm.vue";
import Dashboard from "../views/dashboardHome.vue";
import cardDetails from "../views/qualityDetails.vue";
import Issues from "../views/issuesPart.vue";
import Features from "../views/features.vue";
import qualitySettings from "../views/qualitySettings.vue";
import qualityPositions from "../views/qualityPositions.vue";
import qualityConfiguration from "../views/qualityConfiguration.vue";
import qualityCompletion from "../views/qualityCompletion.vue";
import Projects from "../views/projectsPart.vue";
import FAQ from "../views/faqPart.vue";

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
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
      redirect: "/settings/qualityconfiguration",
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
