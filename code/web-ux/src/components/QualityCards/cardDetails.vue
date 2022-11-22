<script setup>
import { VtmnButton, VtmnIcon } from "@vtmn/vue";
import { ref, defineEmits, defineProps } from "vue";
import cardProgressBar from "./cardProgressBar.vue";

const props = defineProps(["cardInfo", "cardValue", "cardActive"]);

const sanityGroup = ref(["Sanity Check", "High", "Medium", "Global"]);

const emit = defineEmits(["changeHeight", "cardSelected"]);

let showFirstLines = ref(false);
let showSecondLines = ref(false);
let lineHeight = ref("");

const calcHeight = (line) => {
  if (line === "firstLine") {
    showFirstLines.value = !showFirstLines.value;
    lineHeight.value =
      document.getElementById("firstLineToHide")?.scrollHeight + "px";
    emit(
      "changeHeight",
      document.getElementById("firstLineToHide")?.scrollHeight + "px"
    );
  } else {
    showSecondLines.value = !showSecondLines.value;
    lineHeight.value =
      document.getElementById("secondLineToHide")?.scrollHeight + "px";
    emit(
      "changeHeight",
      document.getElementById("firstLineToHide")?.scrollHeight + "px"
    );
  }
};

const selectedDetail = (data) => {
  emit("cardSelected", data);
};
</script>

<template>
  <div class="detailCard">
    <div
      v-if="props.cardInfo.status == 'Pending'"
      class="vtmn-flex vtmn-items-center vtmn-justify-evenly"
    >
      <p
        class="fullStatus vtmn-flex vtmn-typo_text-1 vtmn-w-1/6 vtmn-justify-center vtmn-items-center vtmn-m-5 vtmn-rounded-lg vtmn-text-white"
        :class="props.cardInfo.status == 'Pending' ? 'statusPending' : ''"
      >
        PENDING
      </p>
      <p class="vtmn-mb-4 vtmn-text-center">
        Waiting for all tests to be processed.
      </p>
      <Vtmn-Button disabled>Finish</Vtmn-Button>
    </div>
    <div v-else>
      <div v-if="props.cardValue?.column" class="columnCard fullCard">
        <table class="vtmn-w-full vtmn-my-4">
          <thead>
            <tr>
              <th
                v-if="!props.cardActive"
                class="vtmn-text-center columnType"
                :class="
                  props.cardActive ? 'vtmn-typo_text-1' : 'vtmn-typo_caption-1'
                "
              >
                Severity
              </th>
              <th v-else></th>
              <th
                v-for="(sanity, index) in sanityGroup"
                class="vtmn-typo_text-1 vtmn-text-center"
                :class="
                  props.cardActive ? 'vtmn-typo_text-1' : 'vtmn-typo_caption-1'
                "
                :key="index"
              >
                {{ sanity }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td
                v-if="!props.cardActive"
                class="vtmn-flex vtmn-justify-center vtmn-my-4"
              >
                <div class="miniStatus statusFailure"><p>Failure</p></div>
              </td>
              <td
                v-else
                class="vtmn-typo_text-1 vtmn-font-bold vtmn-text-center columnType"
              >
                Severity
              </td>
              <td
                v-for="(sanity, index) in sanityGroup"
                class="columnDetails"
                :key="index"
              >
                <card-progress-bar
                  :card-active="cardActive"
                  :index="index"
                  :sanityGroup="sanityGroup"
                  :cardInfo="cardInfo"
                  @cardSelected="selectedDetail"
                />
              </td>
            </tr>
            <tr>
              <td>
                <div
                  v-if="props.cardValue?.line1"
                  class="linePart vtmn-flex vtmn-items-center vtmn-justify-between"
                >
                  <div
                    class="lineCountry vtmn-flex vtmn-items-center"
                    :class="showFirstLines ? 'lineOpen' : ''"
                    @click="calcHeight('firstLine')"
                  >
                    <VtmnIcon :size="16" value="chevron-right-line" />
                    <svg width="21" height="21" fill="#001018">
                      <use
                        xlink:href="/node_modules/@vtmn/assets/dist/assets/sprite/assets.svg#flag-fr"
                      />
                    </svg>
                    <p class="vtmn-typo_caption-1">FR</p>
                  </div>
                </div>
              </td>
              <td v-for="(sanity, index) in sanityGroup" :key="index">
                <div
                  v-if="props.cardValue?.cell"
                  class="cellsCard vtmn-flex vtmn-justify-center"
                >
                  <div
                    class="individualCell vtmn-flex vtmn-items-end vtmn-justify-center vtmn-pb-2"
                  >
                    <p class="checkedIssues">43</p>
                    <p class="warningIssues">5</p>
                    <p class="errorIssues">2</p>
                    <span data-ok="43" data-wa="5" data-no="2"></span>
                  </div>
                </div>
              </td>
            </tr>
            <tr
              class="hideLines"
              id="firstLineToHide"
              :class="showFirstLines ? ' active' : ''"
              :style="showFirstLines ? 'max-height: ' + lineHeight : ''"
            >
              <td>
                <div
                  v-if="props.cardValue?.line1"
                  class="linePart vtmn-flex vtmn-items-center vtmn-justify-between"
                >
                  <div
                    class="lineCountry vtmn-flex vtmn-items-center"
                    :class="showFirstLines ? 'lineOpen' : ''"
                    @click="calcHeight('secondLine')"
                  >
                    <VtmnIcon :size="16" value="chevron-right-line" />
                    <p class="vtmn-typo_caption-1">Integ. Other</p>
                  </div>
                </div>
              </td>
              <td v-for="(sanity, index) in sanityGroup" :key="index">
                <div
                  v-if="props.cardValue?.cell"
                  class="cellsCard vtmn-flex vtmn-justify-center"
                >
                  <div
                    class="individualCell vtmn-flex vtmn-items-end vtmn-justify-center vtmn-pb-2"
                  >
                    <p class="checkedIssues">43</p>
                    <p class="warningIssues">5</p>
                    <p class="errorIssues">2</p>
                    <span data-ok="43" data-wa="5" data-no="2"></span>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
