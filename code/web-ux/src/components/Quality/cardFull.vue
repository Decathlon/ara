<script setup>
import { VtmnChip, VtmnIcon } from "@vtmn/vue";
import cardDetails from "./cardDetails.vue";
import { defineAsyncComponent } from "vue";

const cardScenarios = defineAsyncComponent(() =>
  import("../../views/scenariosList.vue")
);
const props = defineProps(["cardInfo", "cardValue", "cardActive"]);
const emit = defineEmits(["cardSelected"]);

const showSelectedCard = (data) => {
  emit("cardSelected", data);
};
</script>

<template>
  <div class="vtmn-w-full vtmn-m-14">
    <div class="cardFull">
      <div class="qualityInfos">
        <div
          class="block cardSimple vtmn-flex vtmn-flex-row vtmn-flex-wrap vtmn-w-full vtmn-justify-center vtmn-m-auto vtmn-rounded-t"
        >
          <div class="vtmn-flex vtmn-w-full vtmn-justify-end">
            <div
              class="vtmn-flex vtmn-w-full vtmn-space-x-10 vtmn-justify-end vtmn-space-x-reverse vtmn-mr-2"
            >
              <span
                class="cardProfile vtmn-p-2 vtmn-typo_caption-1 vtmn-rounded-b-xl"
                >Global</span
              >
              <span
                class="cardId vtmn-p-2 vtmn-typo_caption-1 vtmn-rounded-b-xl"
                >De187</span
              >
            </div>
            <span class="vtmn-p-2">
              <VtmnIcon value="more-fill" :size="16" />
            </span>
          </div>
          <div
            class="cardBranch vtmn-flex vtmn-justify-evenly vtmn-w-full vtmn-mb-6"
          >
            <div
              v-if="!props.cardActive"
              class="navCards vtmn-flex vtmn-items-center vtmn-text-xl"
            >
              <span class="vtmn-cursor-pointer"> &#171; </span>
              <span class="vtmn-cursor-pointer"> &#8249; </span>
            </div>

            <div
              class="cardHeader vtmn-flex vtmn-w-full vtmn-justify-evenly vtmn-items-center"
            >
              <div
                v-if="props.cardActive"
                class="vtmn-flex vtmn-items-center vtmn-justify-evenly vtmn-w-1/6"
              >
                <img
                  src="../../../src/assets/img/failure.svg"
                  width="64"
                  alt="Ara failure icon"
                />
                <p class="vtmn-typo_title-3 failQuality">94 %</p>
              </div>

              <div
                class="vtmn-flex vtmn-items-center vtmn-justify-evenly"
                :class="!props.cardActive ? 'vtmn-w-2/5' : 'vtmn-w-2/12'"
              >
                <VtmnChip
                  v-for="(cardLabel, index) in props.cardInfo?.conditions"
                  variant="filter"
                  size="small"
                  icon="vtmx-check-fill"
                  :selected="index < 2"
                  :key="index"
                >
                  {{ 2 > index ? cardLabel : "+" + (index - 1) }}
                </VtmnChip>
              </div>

              <div class="cardTime vtmn-self-center">
                <p
                  class="cardLastTestTime icon-tile vtmn-flex vtmn-items-center vtmn-flex-row-reverse vtmn-relative vtmn-typo_text-3 vtmn-font-medium"
                >
                  18min ago
                  <span class="vtmx-time-fill vtmn-text-2xl vtmn-mr-2"></span>
                </p>
              </div>
              <div class="cardTime vtmn-self-center">
                <p
                  class="cardCreationDate icon-tile vtmn-flex vtmn-items-center vtmn-flex-row-reverse vtmn-relative vtmn-typo_text-3 vtmn-font-medium"
                >
                  Feb 15, 2022 - 13:40
                  <span
                    class="vtmx-calendar-2-fill vtmn-text-2xl vtmn-mr-2"
                  ></span>
                </p>
              </div>

              <div v-if="props.cardActive">
                <VtmnIcon variant="warning" :size="32" value="lock-fill" />
              </div>
            </div>

            <div
              v-if="!props.cardActive"
              class="navCards vtmn-flex vtmn-items-center vtmn-text-xl"
            >
              <span class="vtmn-cursor-pointer">&#8250;</span>
              <span class="vtmn-cursor-pointer">&#187;</span>
            </div>
          </div>
        </div>

        <card-details
          :cardInfo="cardInfo"
          :cardValue="cardValue"
          :cardActive="cardActive"
          @cardSelected="showSelectedCard"
        />
      </div>
    </div>
    <card-scenarios v-if="props.cardActive" />
  </div>
</template>

<style scoped>
table td {
  cursor: pointer;
}

.cardCreationDate {
  color: var(--vtmn-semantic-color_content-tertiary);
}

.cardLastTestTime {
  color: var(--vtmn-semantic-color_content-visited-reversed);
}

.navCards span {
  background-color: #cbcbcb;
  margin: 5px;
  width: 28px;
  text-align: center;
  border-radius: 100px;
  color: #ffffff;
}

.fullStatus {
  height: 175px;
}

.cardFull {
  box-shadow: var(--vtmn-shadow_100);
  border-radius: var(--vtmn-radius_200);
}

.hideLines td div {
  max-height: 0px;
  overflow: hidden;
  transition: max-height 0.3s;
}

.hideLines.active td div {
  max-height: 100px;
  transition: max-height 0.3s;
}

.failQuality {
  color: var(--vtmn-semantic-color_content-negative);
}
</style>
