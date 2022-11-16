<script setup>
import { VtmnButton, VtmnSelect, VtmnSearch, VtmnChip } from "@vtmn/vue";
import { ref, defineProps, defineEmits } from "vue";
import { useCardsPositionsStore } from "../stores/cardsPositions";
import modalForm from "../components/modalForm.vue";

const cardsPositionsStore = useCardsPositionsStore();
const identifier = "vtmnSelect";
let searchList = ref(false);
let searchedCondition = ref("");
let showToast = ref(false);
let filterType = ref("");
const selectedCondition = {
  1: {},
  2: {},
  3: {},
  4: {},
  5: {},
  6: {},
};
let searchFilter = ref(true);
let searchError = ref(false);
const emit = defineEmits(["condition-added"]);

const props = defineProps({
  showConfigureModal: { type: Boolean, required: false },
  filterInfo: { type: String, required: false },
  activeSearch: { type: Boolean, required: false },
  editPosition: { type: Boolean, required: false },
  positionSelected: { type: Number, required: false },
  chosenCondition: { type: Array, required: false },
});

const newPosition = ref({
  chosenCondition: [],
  filterInfo: "",
});

const options = [
  { label: "Select", value: "", disabled: true },
  { label: "Feature", value: "Feature" },
  { label: "Severity", value: "Severity" },
  { label: "Team", value: "Team" },
  { label: "Status", value: "Status" },
  { label: "Labels", value: "Labels" },
];

const conditions = [
  { label: "Develop", value: "", disabled: true },
  { label: "Master", value: "feature" },
  { label: "Stab", value: "severity" },
  { label: "Day", value: "team" },
  { label: "Night", value: "status" },
];

const filteredProducts = () => {
  return conditions.filter((type) =>
    type.label.toLowerCase().includes(searchedCondition.value.toLowerCase())
  );
};
const saveCondition = (label) => {
  if (props.chosenCondition) {
    newPosition.value.chosenCondition = props.chosenCondition;
  }

  if (newPosition.value.chosenCondition.find((element) => element === label)) {
    searchError.value = true;
    searchList.value = false;
  } else {
    newPosition.value.chosenCondition.push(label);
    newPosition.value.filterInfo = filterType.value;
    searchError.value = false;
    searchList.value = false;
    showToast.value = false;
  }
};

const confirmCondition = () => {
  let arrayToSend = {
    conditions: newPosition.value.chosenCondition,
    position: props.positionSelected,
    filterInfo: newPosition.value.filterInfo,
    status: "Success",
  };
  selectedCondition[props.positionSelected] = arrayToSend;
  cardsPositionsStore.setCartItems(arrayToSend);
  emit("condition-added", props.positionSelected);
};
</script>

<template>
  <Teleport to="body">
    <modal-form :show="showConfigureModal" @close="$emit('closeModal')">
      <template #header>
        <h3 class="vtmn-typo_title-3">Configure position</h3>
      </template>

      <template #body>
        <p
          id="vtmn-modal-description"
          class="vtmn-modal_content_body--text vtmn-typo_text-2 modalDesc"
        >
          The execution displayed at this position will be filter by the two
          conditions below.
        </p>

        <div class="vtmn-flex vtmn-justify-around vtmn-mb-6">
          <div class="selectModal vtmn-w-full">
            <VtmnSelect
              labelText="Filter type"
              id="vtmn-select"
              v-model="filterType"
              @change="searchFilter = false"
              :options="options"
              :identifier="identifier"
            ></VtmnSelect>
          </div>
          <div class="searchModal vtmn-relative vtmn-flex vtmn-w-full">
            <VtmnSearch
              :disabled="props.activeSearch ? props.activeSearch : activeSearch"
              v-model="searchedCondition"
              class="vtmn-m-0"
              :class="searchError ? ' inputError' : ''"
              @focus="searchList = true"
            />
            <ul
              v-if="searchList"
              class="searchContent vtmn-absolute vtmn-flex vtmn-flex-col vtmn-z-10"
            >
              <li
                v-for="condition in filteredProducts()"
                @click="saveCondition(condition.label)"
                :key="condition"
              >
                {{ condition.label }}
              </li>
            </ul>
          </div>
        </div>

        <div
          v-if="
            chosenCondition.length > 0
              ? chosenCondition.length > 0
              : newPosition.chosenCondition.length > 0
          "
          class="selectedConditions"
        >
          <p class="filterType">
            {{
              filterInfo
                ? filterInfo
                : newPosition.filterInfo
                ? newPosition.filterInfo
                : filterInfo
            }}
          </p>
          <div class="vtmn-flex vtmn-float-left vtmn-my-4 vtmn-w-full">
            <div
              v-for="(conditions, index) in newPosition.chosenCondition.length >
              0
                ? newPosition.chosenCondition
                : chosenCondition"
              class="vtmn-mr-4"
              :key="index"
            >
              <VtmnChip variant="input" size="medium" selected>
                {{ conditions }}
              </VtmnChip>
            </div>
          </div>
        </div>
      </template>

      <template #footer>
          <VtmnButton
            size="medium"
            class="cancelModalBtn vtmn-mx-2"
            @click="$emit('closeModal')"
            >Cancel</VtmnButton
          >
          <VtmnButton
            size="medium"
            class="confirmModalBtn vtmn-mx-2"
            :class="
              chosenCondition.length || newPosition.chosenCondition.length
                ? ' active'
                : ''
            "
            @click="confirmCondition"
            :disabled="
              !chosenCondition.length === 0 ||
              !newPosition.chosenCondition.length === 0
            "
          >
            Confirm
          </VtmnButton>
      </template>
    </modal-form>
  </Teleport>
</template>

<style scoped>
.modalOpacity::before {
  content: "";
  background-color: var(--vtmn-semantic-color_content-inactive);
  position: fixed;
  width: 100%;
  height: 100vh;
  display: block;
  top: 0;
  left: 0;
  opacity: 0.5;
  z-index: 300;
}

.vtmn-search {
  margin-top: 24px;
}

.selectModal .vtmn-select_container {
  width: 90%;
}

.searchModal .vtmn-search {
  width: 90%;
}

.filterType {
  color: var(--vtmn-semantic-color_content-visited-reversed);
}

.inputError::before {
  content: "This condition has already been choosen.";
  position: absolute;
  bottom: -25px;
  color: var(--vtmn-semantic-color_content-negative);
}
</style>
