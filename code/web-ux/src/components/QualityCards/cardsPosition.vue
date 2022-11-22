<script setup>
import { VtmnButton, VtmnToast, VtmnIcon } from "@vtmn/vue";
import { onMounted, ref, defineAsyncComponent } from "vue";
import { useCardsPositionsStore } from "../../stores/cardsPositions";

const positionsModal = defineAsyncComponent(() =>
  import("../../views/PositionsModal.vue")
);
const cardsPositionsStore = useCardsPositionsStore();
const selectedCondition = {
  1: {},
  2: {},
  3: {},
  4: {},
  5: {},
  6: {},
};
const storedConditions = [];
let filterInfo = ref(null);
let activeSearch = ref(null);
let searchedCondition = ref("");
let positionSelected = ref(0);
let chosenCondition = ref([]);
let counter = ref(1);
let editPosition = ref(false);
let showToast = ref(false);

let showConfigureModal = ref(false);
let showConditionCTA = ref(false);
let prevPosition = ref("");

const openModal = (index) => {
  positionSelected.value = Number(index);
  filterInfo.value = "";
  chosenCondition.value = [];
  searchedCondition.value = "";
  showConfigureModal.value = true;
};

const savePosition = () => {
  localStorage.setItem(
    "conditionStored",
    JSON.stringify(cardsPositionsStore.conditionsPositions)
  );
  showToast.value = true;
};

const newPosition = (val) => {
  showConfigureModal.value = false;
  if (val === counter.value) {
    counter.value = val + 1;
  }

  return cardsPositionsStore;
};

const editCondition = (conditions, index) => {
  prevPosition.value = index;
  filterInfo.value = conditions.filterInfo;
  activeSearch.value = false;
  editPosition.value = true;
  positionSelected.value = Number(conditions.position);
  chosenCondition.value = conditions.conditions;
  showConfigureModal.value = true;
};

const removeCondition = (position) => {
  if (confirm("Are you sure you want to remove this condition?")) {
    localStorage.removeItem("conditionStored", position);
  }
};

onMounted(() => {
  const tab = JSON.parse(localStorage.getItem("conditionStored"));
  if (tab) {
    for (var i = 0; i < tab.length; i++) {
      cardsPositionsStore.setCartItems(tab[i]);
    }
  }
  storedConditions.push(tab);
  if (storedConditions[0]) {
    counter.value = storedConditions[0].length + 1;
  }
});
</script>

<template>
  <div>
    <div class="cardsSetting">
      <div class="cardsPositions">
        <div
          class="positionSquare vtmn-flex vtmn-float-left"
          v-for="(positions, index) in selectedCondition"
          @mouseenter="showConditionCTA = index"
          @mouseleave="showConditionCTA = false"
          :key="positions"
          :class="
            cardsPositionsStore.conditionsPositions[index - 1]?.conditions
              ? 'filledPosition vtmn-content-center'
              : ''
          "
        >
          <div
            v-if="counter == index"
            class="card active"
            @click="openModal(index)"
          >
            <VtmnIcon
              value="add-fill"
              variant="information"
              class="vtmn-m-auto"
              :size="32"
            ></VtmnIcon>
          </div>

          <ul v-else-if="cardsPositionsStore.conditionsPositions.length > 0">
            <li
              v-for="conditionName in cardsPositionsStore.conditionsPositions"
              :key="conditionName.position"
              :class="
                showConditionCTA == conditionName.position
                  ? 'showBtn'
                  : 'hideBtn'
              "
            >
              <div v-if="conditionName.position === Number(index)">
                <div
                  class="conditionCTA vtmn-absolute vtmn-w-2/3 vtmn-h-8 vtmn-rounded-full vtmn-left-0 vtmn-right-0 vtmn-mx-auto"
                >
                  <VtmnIcon
                    value="edit-fill"
                    class="editConditionIcon vtmn-mx-2"
                    @click="editCondition(conditionName, counter)"
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

        <VtmnButton
          class="vtmn-flex vtmn-m-auto"
          @click="savePosition()"
          :disabled="!cardsPositionsStore.conditionsPositions.length"
        >
          Save configuration</VtmnButton
        >
      </div>
    </div>

    <positions-modal
      @close-modal="showConfigureModal = false"
      @condition-added="newPosition"
      :showConfigureModal="showConfigureModal"
      :filterInfo="filterInfo"
      :activeSearch="activeSearch"
      :editPosition="editPosition"
      :positionSelected="positionSelected"
      :chosenCondition="chosenCondition"
    />
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
  z-index: -1;
}

.card:hover {
  background-color: var(--vtmn-semantic-color_background-brand-primary);
  border-radius: 5px;
  transform: scale(1.1);
  transition: 500ms;
}

.ctaBlock .vtmn-btn {
  background-color: var(--vtmn-semantic-color_content-warning);
}
</style>
