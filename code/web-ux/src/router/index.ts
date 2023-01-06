import { createRouter, createWebHistory } from "vue-router";
import Login from "../components/LoginForm.vue";
import qualityValidation from "../views/qualityValidation.vue";
import qualityValidationList from "../views/qualityValidationList.vue";
import qualityValidationGraph from "../views/qualityValidationGraph.vue";
import cardDetails from "../views/QualityDetails.vue";
import Issues from "../views/IssuesPart.vue";
import Features from "../views/FeaturesPart.vue";
import qualitySettings from "../views/QualitySettings.vue";
import qualityPositions from "../views/QualityPositions.vue";
import qualityConfiguration from "../views/QualityConfiguration.vue";
import qualityCompletion from "../views/QualityCompletion.vue";
import Projects from "../views/ProjectsPart.vue";
import FAQ from "../views/FaqPart.vue";
import Labels from "../views/LabelsPart.vue";
import Regressions from "../views/RegressionsPart.vue";
import featuresSettings from "../views/FeaturesSettings.vue";
import notificationSettings from "../views/NotificationSettings.vue";

const router = createRouter({
  linkActiveClass: "active",
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
      component: qualityValidation,
      children: [
        {
          path: "list",
          name: "list",
          component: qualityValidationList,
        },
        {
          path: "graph",
          name: "graph",
          component: qualityValidationGraph,
        }
      ]
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
      component: qualitySettings,
      redirect: "/settings/qualitysettings",
      children: [
        {
          path: "qualitysettings",
          name: "qualitySettings",
          redirect: "/settings/qualitysettings/qualityconfiguration",
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
              path: "qualitycompletion",
              name: "qualityCompletion",
              component: qualityCompletion,
            },
          ],
        },
        {
          path: "featuressettings",
          name: "featuresSettings",
          component: featuresSettings,
        },
        {
          path: "notificationsettings",
          name: "notificationSettings",
          component: notificationSettings,
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
    {
      path: "/labels",
      name: "labels",
      component: Labels,
    },
    {
      path: "/regressions",
      name: "regressions",
      component: Regressions,
    },
  ],
});

export default router;
