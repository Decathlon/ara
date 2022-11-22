<script setup>
import cardsPosition from "../components/QualityCards/CardsPosition.vue";
import { ref, onUpdated } from "vue";
import { useRoute } from "vue-router";

let configureCards = ref(false);
const route = useRoute();

onUpdated(() => {
  if (route.query.firstConnexion === "true") {
    configureCards.value = true;
  }
});
</script>

<template>
  <div>
    <div class="block">
      <div class="qualityPosition">
        <p
          class="vtmn-typo_text-1 vtmn-flex vtmn-justify-left vtmn-ml-10 vtmn-pl-8 vtmn-mt-6"
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
