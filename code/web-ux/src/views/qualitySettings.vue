<script>
import cardsPosition from "../components/cardsPosition.vue";

export default {
  name: "quality-settings",

  components: {
    cardsPosition,
  },

  data() {
    return {
      pageLoaded: false,
      configureCards: false,
    };
  },

  mounted() {
    this.pageLoaded = true;

    if (this.$route.query.firstConnexion === "true") {
      this.configureCards = true;
    }
  },
};
</script>

<template>
  <div>
    <div class="block vtmn-pt-10 vtmn-ml-10">
      <h1 class="vtmn-text-center vtmn-typo_title-1">Quality validation</h1>

      <div v-if="!pageLoaded" class="vtmn-loader">
        <span class="vtmn-sr-only">Loading</span>
      </div>

      <div
        class="settingsNav vtmn-flex vtmn-justify-left vtmn-ml-10 vtmn-mt-10"
      >
        <ul>
          <li class="vtmn-mx-6">Cards configuration</li>
          <li
            class="settingsTab vtmn-mx-6"
            :class="this.configureCards === true ? 'active' : 'non-active'"
          >
            Cards position
          </li>
          <li class="vtmn-mx-6">Completion and success</li>
        </ul>
      </div>

      <div class="qualityPosition">
        <p
          class="vtmn-typo_text-1 vtmn-flex vtmn-justify-left vtmn-ml-10 vtmn-mt-6"
        >
          This part allow you to choose wich cards, grouped by labels, you want
          to display and the position and those cards. Click on a position below
          to begin.
        </p>

        <cards-position />
      </div>
    </div>
  </div>
</template>

<style scoped>
.vtmn-typo_title-1 {
  color: var(--vtmn-semantic-color_background-brand-primary);
}

.settingsNav ul {
  display: flex;
}

.settingsNav ul li {
  padding: 10px 15px;
  border-radius: 100px;
  color: var(--vtmn-semantic-color_border-secondary);
}

.settingsTab.active {
  color: #ffffff;
  background-color: var(--vtmn-semantic-color_content-active);
}

.vtmn-typo_text-1 {
  color: var(--vtmn-semantic-color_content-tertiary);
}
</style>
