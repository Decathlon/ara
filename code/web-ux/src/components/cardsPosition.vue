<script>
import {
  VtmnSelect,
  VtmnSearch,
  VtmnButton,
  VtmnToast,
  VtmnIcon,
} from "@vtmn/vue";
import { mapState } from "vuex";

export default {
  components: {
    VtmnSelect,
    VtmnSearch,
    VtmnButton,
    VtmnToast,
    VtmnIcon,
  },

  data() {
    return {
      showConfigureModal: false,
      options: [
        { label: "Select", value: "", disabled: true },
        { label: "Feature", value: "Feature" },
        { label: "Severity", value: "Severity" },
        { label: "Team", value: "Team" },
        { label: "Status", value: "Status" },
        { label: "Labels", value: "Labels" },
      ],
      conditions: [
        { label: "Develop", value: "", disabled: true },
        { label: "Master", value: "feature" },
        { label: "Stab", value: "severity" },
        { label: "Day", value: "team" },
        { label: "Night", value: "status" },
      ],
      args: "",
      searchList: false,
      activeSearch: true,
      searchedCondition: "",
      positionSelected: 0,
      identifier: "vtmnSelect",
      conditionArray: [],
      chosenCondition: [],
      counter: 1,
      selectedCondition: {
        1: {},
        2: {},
        3: {},
        4: {},
        5: {},
        6: {},
      },
      storedConditions: [],
      showToast: false,
      showConditionCTA: false,
    };
  },

  methods: {
    saveCondition(label) {
      this.chosenCondition.push(label);
      this.conditionArray.push(label);
      this.searchList = false;
      this.showToast = false;
    },

    openModal() {
      this.chosenCondition = [];
      this.conditionArray = [];
      this.showConfigureModal = true;
    },

    incrementCounter(index) {
      if (this.counter > 6) {
        this.counter = 0;
      } else {
        if (index == this.counter) {
          this.positionSelected = index;
          return true;
        } else {
          return false;
        }
      }
    },

    confirmCondition() {
      this.counter = Number(this.positionSelected) + 1;
      let arrayToSend = {
        conditions: this.conditionArray,
        position: this.positionSelected,
        status: "Pending",
        args: this.args,
      };
      this.$store.dispatch("cardsPosition/checkout", arrayToSend);
      this.showConfigureModal = false;
      this.incrementCounter(Number(this.positionSelected) + 1);
    },

    savePosition() {
      localStorage.setItem(
        "conditionStored",
        JSON.stringify(this.conditionsPositions)
      );
      this.showToast = true;
    },

    editCondition(conditions) {
      this.conditionArray = conditions;
      this.chosenCondition = conditions;
      this.showConfigureModal = true;
    },

    removeCondition(position) {
      if (
        confirm("Are you sure you want to save this thing into the database?")
      ) {
        localStorage.removeItem("conditionStored", position);
      }
    },
  },

  computed: {
    ...mapState("cardsPosition", ["conditionsPositions"]),

    filteredProducts() {
      return this.conditions.filter((p) => {
        return (
          p.label.toLowerCase().indexOf(this.searchedCondition.toLowerCase()) !=
          -1
        );
      });
    },
  },

  created() {
    const tab = JSON.parse(localStorage.getItem("conditionStored"));
    if (tab) {
      for (var i = 0; i < tab.length; i++) {
        this.$store.dispatch("cardsPosition/checkout", tab[i]);
      }
    }
    this.storedConditions.push(tab);
    if (this.storedConditions[0]) {
      this.counter = this.storedConditions[0].length + 1;
    }
  },

  watch: {
    args: function () {
      this.activeSearch = false;
    },
  },
};
</script>

<template>
  <div>
    <div class="cardsSetting">
      <div class="cardsPositions">
        <div
          class="positionSquare vtmn-flex vtmn-float-left"
          v-for="(positions, index) in selectedCondition"
          @mouseenter="showConditionCTA = index"
          @mouseleave="showConditionCTA = ''"
          :key="positions"
          :class="
            conditionsPositions[index - 1]?.conditions
              ? 'filledPosition vtmn-content-center'
              : ''
          "
        >
          <div
            v-if="incrementCounter(index) && !positions.length"
            class="card active"
            @click="openModal(index)"
          >
            <svg class="vtmn-m-auto" width="32" height="32" fill="#000000">
              <use
                xlink:href="/node_modules/@vtmn/icons/dist/vitamix/sprite/vitamix.svg#add-fill"
              />
            </svg>
          </div>

          <ul v-else-if="conditionsPositions.length > 0">
            <li
              v-for="conditionName in conditionsPositions"
              :key="conditionName.position"
              :class="
                showConditionCTA == conditionName.position
                  ? 'showBtn'
                  : 'hideBtn'
              "
            >
              <div v-if="conditionName.position === index">
                <div
                  class="conditionCTA vtmn-absolute vtmn-w-2/3 vtmn-h-8 vtmn-rounded-full vtmn-left-0 vtmn-right-0 vtmn-mx-auto"
                >
                  <VtmnIcon
                    value="edit-fill"
                    class="editConditionIcon vtmn-mx-2"
                    @click="editCondition(conditionName.conditions)"
                  ></VtmnIcon>
                  <VtmnIcon
                    value="delete-bin-fill"
                    class="removeConditionIcon vtmn-mx-2"
                    @click="removeCondition(conditionName.position)"
                  ></VtmnIcon>
                </div>
                <div
                  v-for="(condition, index) in conditionName.conditions"
                  class="conditionContent vtmn-relative vtmn-left-0 vtmn-right-0 vtmn-mx-auto"
                  :key="index"
                >
                  {{ condition }}
                </div>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <div class="ctaBlock">
        <VtmnToast
          v-if="showToast"
          class="cardSaveToast"
          withCloseButton
          withIcon
        >
          <template v-slot:content>
            Cards position successfully saved!
          </template>
        </VtmnToast>

        <VtmnButton @click="savePosition()" class="vtmn-flex vtmn-m-auto"
          >Save configuration</VtmnButton
        >
      </div>
    </div>

    <div>
      <div
        v-if="showConfigureModal"
        class="vtmn-modal"
        id="modal-1"
        role="dialog"
        aria-modal="true"
        aria-labelledby="vtmn-modal-title"
        aria-describedby="vtmn-modal-description"
      >
        <div class="vtmn-modal_content">
          <div class="vtmn-modal_content_title">
            <span id="vtmn-modal-title" class="vtmn-modal_content_title--text"
              >Configure position</span
            >
            <button
              class="vtmn-btn vtmn-btn_variant--ghost vtmn-btn--icon-alone"
            >
              <span
                class="vtmx-close-line"
                aria-hidden="true"
                @click="showConfigureModal = false"
              ></span>
              <span class="vtmn-sr-only" @click="showConfigureModal = false"
                >Close modal</span
              >
            </button>
          </div>
          <div class="vtmn-modal_content_body">
            <p
              id="vtmn-modal-description"
              class="vtmn-modal_content_body--text"
            >
              The execution displayed at this position will be filter by the two
              conditions below.
            </p>

            <div class="positionInput vtmn-mb-6">
              <VtmnSelect
                labelText="Filter type"
                id="vtmn-select"
                v-model="args"
                :options="options"
                :identifier="identifier"
              ></VtmnSelect>
              <div class="searchPart">
                <VtmnSearch
                  :disabled="activeSearch"
                  v-model="searchedCondition"
                  @input="searchList = true"
                />
                <ul class="searchContent" v-if="searchList">
                  <li
                    v-for="condition in filteredProducts"
                    @click="saveCondition(condition.label)"
                    :key="condition.label"
                  >
                    {{ condition.label }}
                  </li>
                </ul>
              </div>
            </div>

            <div v-if="conditionArray.length > 0" class="selectedConditions">
              <p class="filterType">
                {{ args }}
              </p>
              <div
                v-for="conditions in chosenCondition"
                :key="conditions"
                class="vtmn-chip vtmn-chip_variant--input vtmn-flex vtmn-float-left vtmn-ml-4 vtmn-mt-4"
                role="button"
                tabindex="0"
                aria-pressed="true"
              >
                {{ conditions }}
                <button
                  class="vtmn-btn vtmn-btn--icon-alone vtmn-btn_size--small vtmn-btn_variant--ghost-reversed"
                >
                  <span class="vtmn-sr-only">Unselect this chip</span>
                  <span class="vtmx-close-line" aria-hidden="true"></span>
                </button>
              </div>
            </div>
          </div>
          <div class="vtmn-modal_content_actions">
            <button
              @click="showConfigureModal = false"
              class="vtmn-btn vtmn-btn_variant--secondary"
            >
              Cancel
            </button>
            <button
              class="vtmn-btn vtmn-btn_variant--primary"
              @click="confirmCondition"
            >
              Confirm
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cardsSetting {
  display: flex;
  flex-direction: column;
}

.cardsPositions {
  width: 280px;
  margin: 20px auto;
}

.positionSquare {
  background-color: var(--vtmn-semantic-color_border-secondary);
  position: relative;
  width: 120px;
  height: 120px;
  margin: 10px;
  justify-content: center;
  border-radius: 5px;
  cursor: pointer;
  overflow: hidden;
}

.positionSquare.filledPosition {
  display: flex;
  align-items: center;
  background-color: var(--vtmn-semantic-color_background-brand-primary);
  color: var(--vtmn-semantic-color_background-primary);
}

.conditionCTA {
  display: flex;
  background-color: var(--vtmn-semantic-color_background-primary);
}

.showBtn .conditionCTA {
  position: absolute;
  top: 25%;
  transition: 200ms;
  z-index: 9;
}

.hideBtn .conditionContent {
  bottom: 0px;
  transition: 200ms;
}

.showBtn .conditionContent {
  bottom: -90px;
  transition: 200ms;
}

.hideBtn .conditionCTA {
  top: -60px;
  transition: 200ms;
}

.editConditionIcon::before {
  color: var(--vtmn-semantic-color_content-warning);
}

.removeConditionIcon::before {
  color: var(--vtmn-semantic-color_content-inactive);
}

.filledConditions::before {
  content: "-";
  position: absolute;
  top: 0;
  right: 10px;
  cursor: pointer;
}

.card {
  width: 100%;
  display: flex;
  align-items: center;
  transform: scale(1);
  transition: 500ms;
}

.card svg {
  fill: var(--vtmn-semantic-color_content-active);
  position: relative;
}

.card.active::before {
  content: "";
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  width: 60px;
  height: 60px;
  background-color: var(--vtmn-semantic-color_background-primary);
  margin: auto;
  border-radius: 100px;
}

.card:hover {
  background-color: var(--vtmn-semantic-color_background-brand-primary);
  border-radius: 5px;
  transform: scale(1.1);
  transition: 500ms;
}

.positionInput {
  display: flex;
  justify-content: space-around;
}

.positionInput div {
  margin-left: 10px;
}

.vtmn-search {
  margin-top: 24px;
}

.searchPart {
  position: relative;
}

.filterType {
  color: var(--vtmn-semantic-color_content-visited-reversed);
}

.cardSaveToast {
  background-color: var(--vtmn-semantic-color_content-positive);
}
</style>
