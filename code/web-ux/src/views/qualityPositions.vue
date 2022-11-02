<script>
import cardsPosition from "../components/cardsPosition.vue";

export default {
  name: "qualityPositions",

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
    <div class="block vtmn-ml-10">
      <div v-if="!pageLoaded" class="vtmn-loader">
        <span class="vtmn-sr-only">Loading</span>
      </div>

      <div class="qualityPosition">
        <p
          class="vtmn-typo_text-1 vtmn-flex vtmn-justify-left vtmn-ml-10 vtmn-mt-6"
        >
          This part allow you to choose wich cards, grouped by labels, you want
          to display and the position and those cards. Click on a position below
          to begin.
        </p>

        <cards-position class="vtmn-pt-6" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.vtmn-typo_text-1 {
  color: var(--vtmn-semantic-color_content-tertiary);
}
</style>
