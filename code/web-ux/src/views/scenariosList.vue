<script>
import { VtmnSearch, VtmnChip } from "@vtmn/vue";
import executionsModal from "../components/executionsModal.vue";
import scenarioCard from "../components/scenarioCard.vue";

export default {
  components: {
    VtmnSearch,
    executionsModal,
    VtmnChip,
    scenarioCard,
  },

  data() {
    return {
      showModal: false,
      appliedFilters: ["1", "2", "3"],
    };
  },

  computed: {
    activeFilters() {
      return JSON.parse(localStorage.getItem("executionFilters"));
    },
  },
};
</script>

<template>
  <div>
    <div class="vtmn-w-1/3 vtmn-m-auto vtmn-flex">
      <VtmnSearch placeholder="Search a scneario..." />
      <executions-modal />
    </div>

    <div class="appliedFilters vtmn-flex vtmn-justify-center vtmn-m-5">
      <VtmnChip
        v-for="(labels, index) in activeFilters"
        variant="filter"
        size="small"
        selected
        :key="index"
      >
        <span v-for="(label, index) in labels.value" :key="index">
          <template v-if="index > 0">, </template>
          {{ label }}
        </span>
      </VtmnChip>
    </div>

    <p
      class="scenarioCatchLine vtmn-text-center vtmn-mt-6 vtmn-mb-4 vtmn-typo_text-2"
    >
      Showing <span class="vtmn-font-bold">3</span> filtered scenarios out of
      the <span class="vtmn-font-bold">6</span> failed scenarios
    </p>

    <div v-for="(scenario, index) in appliedFilters" :key="index">
      <scenario-card />
    </div>
  </div>
</template>

<style scoped>
.appliedFilters div {
  margin: 0 5px;
}

.scenarioCatchLine {
  color: var(--vtmn-semantic-color_content-inactive);
}

.scenarioCatchLine span {
  color: var(--vtmn-semantic-color_content-warning);
}
</style>
