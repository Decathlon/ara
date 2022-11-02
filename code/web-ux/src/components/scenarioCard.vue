<script>
import { VtmnChip, VtmnIcon } from "@vtmn/vue";

export default {
  components: {
    VtmnChip,
    VtmnIcon,
  },

  data() {
    return {
      scenarioCount: [
        {
          scenar1: "some",
          status: "SUCCESS",
        },
        {
          scenar2: "some",
          status: "ERROR",
        },
      ],
      showPopMenu: false,
    };
  },
};
</script>

<template>
  <div
    class="scenarioBlock vtmn-mb-6 vtmn-w-11/12 vtmn-m-auto vtmn-shadow-100 vtmn-rounded-b"
  >
    <div class="scenarioHeader vtmn-rounded vtmn-px-2 vtmn-py-1">
      <p class="vtmn-typo_text-1">
        Feature: Get [DELIVERY] - ATS V2 - Simulator
      </p>
    </div>

    <div
      v-for="(scenario, index) in scenarioCount"
      class="vtmn-flex vtmn-p-6 vtmn-justify-around vtmn-items-center vtmn-flex-wrap vtmn-relative"
      :class="index > 0 ? 'activeDivider' : ''"
      :key="index"
    >
      <p class="vtmn-flex vtmn-typo_text-2">
        <VtmnIcon
          class="vtmn-mr-2"
          value="counter-line"
          variant="information"
          :size="24"
        />
        Simulator - full case
      </p>
      <p class="vtmn-flex vtmn-typo_text-2">
        <VtmnIcon
          class="vtmn-mr-2"
          value="temp-hot-line"
          variant="information"
          :size="24"
        />
        Medium
      </p>
      <p class="vtmn-flex vtmn-typo_text-2">
        <VtmnIcon
          class="vtmn-mr-2"
          value="group-line"
          variant="information"
          :size="24"
        />
        Get / DELIVERY Lille
      </p>
      <div class="vtmn-flex">
        <VtmnChip class="vtmn-mr-2" variant="filter" size="small" selected>
          France Integ. Other
        </VtmnChip>
        <VtmnChip variant="action" size="small"> +3 </VtmnChip>
      </div>
      <p class="vtmn-typo_text-">02/15/2022 - 12:55</p>
      <p
        class="scenarioStatus success vtmn-typo_text-2 vtmn-px-3 vtmn-py-1px vtmn-rounded-full"
        :class="
          scenario.status == 'SUCCESS' ? 'success' : 'ERROR' ? 'error' : ''
        "
      >
        {{ scenario.status }}
      </p>
      <div class="vtmn-relative">
        <VtmnIcon
          class="vtmn-mr-2 vtmn-cursor-pointer"
          value="more-2-line"
          @click="showPopMenu = index"
          :size="24"
        />
        <ul
          class="popMenu vtmn-absolute vtmn-right-0 vtmn-py-2 vtmn-px-4 vtmn-z-10 vtmn-rounded vtmn-items-around"
          @click="showPopMenu = false"
          :class="showPopMenu === index ? 'active' : ''"
        >
          <li class="vtmn-flex vtmn-py-2">
            <VtmnIcon class="vtmn-mr-2" value="camera-line" :size="24" />
            <p>Screenshot</p>
          </li>
          <li class="vtmn-flex vtmn-py-2">
            <VtmnIcon class="vtmn-mr-2" value="film-line" :size="24" />
            <p>Video</p>
          </li>
          <li class="vtmn-flex vtmn-py-2">
            <VtmnIcon class="vtmn-mr-2" value="history-line" :size="24" />
            <p>History</p>
          </li>
          <li class="vtmn-flex vtmn-py-2">
            <VtmnIcon class="vtmn-mr-2" value="information-line" :size="24" />
            <p>Details</p>
          </li>
          <li class="vtmn-flex vtmn-py-2">
            <VtmnIcon class="vtmn-mr-2" value="forbid-line" :size="24">
            </VtmnIcon>
            <p>Discard</p>
          </li>
        </ul>
      </div>
      <div
        v-if="scenario.status == 'ERROR'"
        class="errorBlock vtmn-mt-6 vtmn-w-full"
      >
        <p class="vtmn-typo_text-1 errorTitle">Regressions</p>
        <div
          class="errorDetails vtmn-py-4 vtmn-px-6 vtmn-rounded-bl-lg vtmn-rounded-tl-lg"
        >
          <p class="vtmn-typo_text-2">
            the shipping group sfs is in "PENDING_CARRIER" state
          </p>
          <p class="vtmn-typo_text-2 vtmn-w-2/6 errorDesc">
            Exception:Number of attempts reached when applying the predicate
            class
            com.decathlon.cube.functionaltests.api.util.IsItemPropertyValueEqualToExpectedValuePredicate
            on param 1775562423sg140022
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.scenarioHeader {
  background-color: var(--vtmn-semantic-color_background-brand-primary);
}

.scenarioHeader p {
  color: var(--vtmn-semantic-color_content-action-reversed);
}

.scenarioBlock {
  background-color: var(--vtmn-semantic-color_background-primary);
}

.activeDivider::before {
  content: "";
  border-top: 1px solid #dddddd;
  width: 90%;
  position: absolute;
  top: 0;
}

.scenarioStatus.success {
  background-color: var(--vtmn-semantic-color_content-positive);
  color: var(--vtmn-semantic-color_content-action-reversed);
}

.scenarioStatus.error {
  background-color: var(--vtmn-semantic-color_background-accent);
  color: var(--vtmn-semantic-color_content-primary);
}

.popMenu {
  display: none;
  background-color: var(--vtmn-semantic-color_content-action-reversed);
  border: 1px solid;
  border-color: var(--vtmn-semantic-color_border-inactive);
}

.popMenu li:not(:first-child) {
  border-top: 1px solid #cccccc;
}

.popMenu.active {
  display: block;
}

.errorBlock .errorTitle {
  color: var(--vtmn-semantic-color_content-inactive);
}

.errorBlock .errorDetails {
  background-color: var(--vtmn-semantic-color_background-secondary);
  border-left: 10px solid;
  border-color: var(--vtmn-semantic-color_background-accent);
}

.errorBlock .errorDetails .errorDesc {
  color: var(--vtmn-semantic-color_content-inactive);
}
</style>
