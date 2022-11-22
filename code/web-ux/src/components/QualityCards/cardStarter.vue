<script setup>
import { defineProps } from "vue";

const props = defineProps(["cardInfo"]);
</script>

<template>
  <div class="vtmn-w-full vtmn-m-14">
    <div class="qualityCard">
      <div class="qualityInfos">
        <div
          class="block cardSimple vtmn-flex vtmn-flex-row vtmn-flex-wrap vtmn-w-full vtmn-justify-center vtmn-m-auto vtmn-rounded-t"
        >
          <span
            class="cardProfile vtmn-left-8 vtmn-p-2 vtmn-typo_caption-1 vtmn-p-2 vtmn-rounded-b-xl"
            >Global</span
          >
          <span
            class="cardId vtmn-right-8 vtmn-p-1 vtmn-typo_caption-1 vtmn-p-2 vtmn-rounded-b-xl"
            >De187</span
          >
          <div
            class="cardBranch vtmn-flex vtmn-justify-center vtmn-w-full vtmn-mt-10 vtmn-mb-8"
          >
            <div
              v-for="(cardLabel, index) in props.cardInfo?.conditions"
              :key="index"
              class="vtmn-chip vtmn-chip_variant--filter vtmn-mr-2"
              role="button"
              tabindex="0"
              aria-pressed="true"
            >
              <span v-for="(label, index) in cardLabel" :key="index">{{
                label ? label : "Temp"
              }}</span>
            </div>

            <div
              v-if="props.cardInfo?.conditions.length > 3"
              class="vtmn-chip vtmn-chip_variant--single-choice"
              role="button"
              tabindex="0"
              aria-pressed="false"
            >
              {{ props.cardInfo ? "+" + (props.cardInfo.conditions.length - 3) : "" }}
            </div>
          </div>

          <div class="cardTime vtmn-mb-6">
            <p
              class="cardCreationDate vtmn-float-left vtmn-mr-10 icon-tile vtmn-relative vtmn-typo_text-1"
            >
              Feb 15, 2022 - 13:40
              <span class="vtmx-calendar-date-fill"></span>
            </p>
            <p
              class="cardLastTestTime vtmn-float-right icon-tile vtmn-relative vtmn-typo_text-1"
            >
              Tested 18min ago
              <span class="vtmx-time-fill"></span>
            </p>
          </div>
        </div>

        <div
          class="block cardStatus statusPending vtmn-flex vtmn-flex-row vtmn-flex-wrap vtmn-w-full vtmn-justify-center vtmn-m-auto vtmn-rounded-b"
        >
          <p
            class="vtmn-flex vtmn-typo_text-1 vtmn-w-full vtmn-justify-center vtmn-m-5"
          >
            PENDING
          </p>
          <p class="vtmn-mb-4">Waiting for all tests to be processed.</p>
        </div>
      </div>

      <div class="qualityStatus"></div>
    </div>
  </div>
</template>

<style scoped>
.qualityCard {
  width: 100%;
}

.cardCreationDate span,
.cardLastTestTime span {
  position: absolute;
  left: -30px;
  top: 0;
  bottom: 0;
}

.cardCreationDate {
  color: var(--vtmn-semantic-color_content-tertiary);
}

.cardLastTestTime {
  color: var(--vtmn-semantic-color_content-visited-reversed);
}
</style>
