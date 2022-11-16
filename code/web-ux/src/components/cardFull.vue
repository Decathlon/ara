<script>
import { VtmnButton, VtmnChip, VtmnProgressbar, VtmnIcon } from "@vtmn/vue";
import cardScenarios from "../views/scenariosList.vue";

export default {
  props: {
    cardInfo: {
      type: Object,
      required: false,
    },
    cardDetails: {
      type: Object,
      required: false,
    },
    cardActive: {
      type: Boolean,
      required: false,
    },
  },

  components: {
    VtmnProgressbar,
    VtmnButton,
    VtmnChip,
    VtmnIcon,
    cardScenarios,
  },

  data() {
    return {
      sanityGroup: ["Sanity Check", "High", "Medium", "Global"],
      showFirstLines: false,
      showSecondLines: false,
      lineHeight: "",
    };
  },

  methods: {
    calcHeight(line) {
      if (line === "firstLine") {
        this.showFirstLines = !this.showFirstLines;
        this.lineHeight =
          document.getElementById("firstLineToHide")?.scrollHeight + "px";
        this.$emit(
          "changeHeight",
          document.getElementById("firstLineToHide")?.scrollHeight + "px"
        );
      } else {
        this.showSecondLines = !this.showSecondLines;
        this.lineHeight =
          document.getElementById("secondLineToHide")?.scrollHeight + "px";
        this.$emit(
          "changeHeight",
          document.getElementById("firstLineToHide")?.scrollHeight + "px"
        );
      }
    },
  },
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
              <VtmnIcon :size="16" value="more-fill" />
            </span>
          </div>
          <div
            class="cardBranch vtmn-flex vtmn-justify-evenly vtmn-w-full vtmn-mt-6 vtmn-mb-8"
          >
            <div
              v-if="!cardActive"
              class="navCards vtmn-flex vtmn-items-center vtmn-text-xl"
            >
              <span>&#171;</span>
              <span>&#8249;</span>
            </div>

            <div
              class="cardHeader vtmn-flex vtmn-w-full vtmn-justify-evenly vtmn-items-center"
            >
              <div
                v-if="cardActive"
                class="vtmn-flex vtmn-items-center vtmn-justify-evenly vtmn-w-1/6"
              >
                <img
                  src="../../src/assets/img/failure.svg"
                  width="64"
                  alt="Ara failure icon"
                />
                <p class="vtmn-typo_title-3 failQuality">94 %</p>
              </div>

              <div
                class="vtmn-flex vtmn-items-center vtmn-justify-evenly"
                :class="!cardActive ? 'vtmn-w-2/5' : 'vtmn-w-2/12'"
              >
                <VtmnChip
                  v-for="(cardLabel, index) in cardInfo?.conditions"
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

              <div v-if="cardActive">
                <VtmnIcon variant="warning" :size="32" value="lock-fill" />
              </div>
            </div>

            <div
              v-if="!cardActive"
              class="navCards vtmn-flex vtmn-items-center vtmn-text-xl"
            >
              <span>&#8250;</span>
              <span>&#187;</span>
            </div>
          </div>
        </div>

        <div class="detailCard">
          <div
            v-if="cardInfo.status == 'Pending'"
            class="vtmn-flex vtmn-items-center vtmn-justify-evenly"
          >
            <p
              class="fullStatus vtmn-flex vtmn-typo_text-1 vtmn-w-1/6 vtmn-justify-center vtmn-items-center vtmn-m-5 vtmn-rounded-lg vtmn-text-white"
              :class="cardInfo.status == 'Pending' ? 'statusPending' : ''"
            >
              PENDING
            </p>
            <p class="vtmn-mb-4 vtmn-text-center">
              Waiting for all tests to be processed.
            </p>
            <Vtmn-Button disabled>Finish</Vtmn-Button>
          </div>
          <div v-else>
            <div v-if="cardDetails?.column" class="columnCard fullCard">
              <table class="vtmn-w-full vtmn-my-4">
                <thead class="">
                  <tr>
                    <th
                      v-if="!cardActive"
                      class="vtmn-text-center columnType"
                      :class="
                        cardActive ? 'vtmn-typo_text-1' : 'vtmn-typo_caption-1'
                      "
                    >
                      Severity
                    </th>
                    <th v-else></th>
                    <th
                      v-for="(sanity, index) in sanityGroup"
                      class="vtmn-typo_text-1 vtmn-text-center"
                      :class="
                        cardActive ? 'vtmn-typo_text-1' : 'vtmn-typo_caption-1'
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
                      v-if="!cardActive"
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
                      <div
                        class="vtmn-flex vtmn-justify-center vtmn-items-baseline"
                        :class="cardActive ? ' vtmn-mt-8' : 'vtmn-mt-4'"
                      >
                        <VtmnProgressbar
                          v-if="!cardActive"
                          variant="circular"
                          size="small"
                          status="determinate"
                          loadingText=""
                          :class="
                            index === sanityGroup.length - 1
                              ? 'lastProgressBar'
                              : ''
                          "
                          :value="Math.floor(Math.random() * 100) + 1"
                          @click="$emit('cardSelected', cardInfo)"
                        />

                        <VtmnProgressbar
                          v-else
                          variant="circular"
                          size="medium"
                          status="determinate"
                          loadingText=""
                          :class="
                            index === sanityGroup.length - 1
                              ? 'lastProgressBar'
                              : ''
                          "
                          :value="Math.floor(Math.random() * 100) + 1"
                          @click="$emit('cardSelected', cardInfo)"
                        />
                      </div>
                      <div
                        class="vtmn-flex vtmn-justify-center vtmn-mt-2 vtmn-mb-4"
                      >
                        <p
                          class="validTasks vtmn-flex vtmn-items-center vtmn-m-1 vtmn-px-1"
                          :class="
                            cardActive
                              ? 'vtmn-typo_text-1'
                              : 'vtmn-typo_caption-1'
                          "
                        >
                          <span>49</span>
                          <i
                            class="vtmx-shield-check-fill vtmn-justify-end vtmn-pl-1"
                          ></i>
                        </p>
                        <p
                          class="errorTasks vtmn-flex vtmn-items-center vtmn-m-1 vtmn-px-1"
                          :class="
                            cardActive
                              ? 'vtmn-typo_text-1'
                              : 'vtmn-typo_caption-1'
                          "
                        >
                          <span>3</span>
                          <i
                            class="vtmx-close-circle-fill vtmn-justify-end vtmn-pl-1"
                          ></i>
                        </p>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div
                        v-if="cardDetails?.line1"
                        class="linePart vtmn-flex vtmn-items-center vtmn-justify-between"
                      >
                        <div
                          class="lineCountry vtmn-flex vtmn-items-center"
                          :class="showFirstLines ? 'lineOpen' : ''"
                          @click="calcHeight('firstLine')"
                        >
                          <i class="vtmx-chevron-right-line"></i>
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
                        v-if="cardDetails?.cell"
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
                        v-if="cardDetails?.line1"
                        class="linePart vtmn-flex vtmn-items-center vtmn-justify-between"
                      >
                        <div
                          class="lineCountry vtmn-flex vtmn-items-center"
                          :class="showFirstLines ? 'lineOpen' : ''"
                          @click="calcHeight('secondLine')"
                        >
                          <i class="vtmx-chevron-right-line"></i>
                          <p class="vtmn-typo_caption-1">Integ. Other</p>
                        </div>
                      </div>
                    </td>
                    <td v-for="(sanity, index) in sanityGroup" :key="index">
                      <div
                        v-if="cardDetails?.cell"
                        class="cellsCard vtmn-flex vtmn-justify-center"
                      >
                        <div
                          class="individualCell vtmn-flex vtmn-items-end vtmn-justify-center vtmn-pb-2"
                        >
                          <p
                            class="checkedIssues"
                            :class="cardActive ? 'vtmn-typo_text-3' : ''"
                          >
                            43
                          </p>
                          <p
                            class="warningIssues"
                            :class="cardActive ? 'vtmn-typo_text-3' : ''"
                          >
                            5
                          </p>
                          <p
                            class="errorIssues"
                            :class="cardActive ? 'vtmn-typo_text-3' : ''"
                          >
                            2
                          </p>
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
      </div>
    </div>
    <card-scenarios v-if="cardActive" />
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
