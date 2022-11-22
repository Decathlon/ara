<script setup lang="ts">
import {
  VtmnRadioButton,
  VtmnIcon,
  VtmnButton,
  VtmnSelect,
  VtmnSearch,
  VtmnChip,
} from "@vtmn/vue";
import {
  ref,
  reactive,
  onMounted,
  watch,
  computed,
  defineAsyncComponent,
} from "vue";

const modalForm = defineAsyncComponent(
  () => import("../components/Modal/TheModalForm.vue")
);
let showModal = ref(false);
let searchList = ref(false);
const execType = reactive([
  { label: "Select", value: "", disabled: true },
  { label: "Teams", value: "Teams" },
  { label: "Features", value: "Features" },
  { label: "Labels", value: "Labels" },
  { label: "Severities", value: "Severities" },
  { label: "Discarded", value: "Discarded" },
  { label: "Success", value: "Success" },
  { label: "Regressions", value: "Regressions" },
]);
let tableExec = reactive([]);
let executionFilterType = ref("");
let typeSelected = ref("");
const labelsType = reactive([
  { label: "France", value: "France" },
  { label: "Belgium", value: "Belgium" },
  { label: "Integ. Other", value: "Integ. Other" },
  { label: "Day", value: "team" },
  { label: "Night", value: "status" },
]);
let searchedCondition = ref("");
let sameType = ref(false);

const saveCondition = (data) => {
  searchList.value = false;
  if (tableExec.length > 0) {
    for (var i = 0; i <= tableExec.length; i++) {
      if (tableExec[i]?.type === typeSelected.value) {
        tableExec[i].push(data);
      }
    }
  } else if (data) {
    tableExec.push({
      value: [data],
      type: typeSelected,
    });
  }
};

const removeFilter = (filter, filterToRemove) => {
  for (var k = 0; k < tableExec.length; k++) {
    if (tableExec[k].type === filter.type) {
      const indexToRemove = tableExec[k].indexOf(filterToRemove);

      if (indexToRemove) {
        tableExec[k].splice(indexToRemove, 1);
      }
    }
  }
};

const confirmFilter = () => {
  showModal.value = false;
  localStorage.setItem("executionFilters", JSON.stringify(tableExec));
};

const filteredLabels = computed(() => {
  return labelsType.filter((p) => {
    return (
      p.label.toLowerCase().indexOf(searchedCondition.value.toLowerCase()) != -1
    );
  });
});

const filterSelected = computed(() => {
  return JSON.parse(localStorage.getItem("executionFilters")).length;
});

onMounted(() => {
  if (localStorage.getItem("executionFilters")) {
    tableExec = JSON.parse(localStorage.getItem("executionFilters"));
  }
});

watch(typeSelected, (typeSelected) => {
  for (var j = 0; j < tableExec.length; j++) {
    if (tableExec[j].type === typeSelected) {
      return true;
    }
  }

  if (sameType || !tableExec) {
    tableExec.push({
      value: [],
      type: typeSelected,
    });
  }
});
</script>

<template>
  <div>
    <button
      class="filterBtn vtmn-flex vtmn-justify-center vtmn-items-center vtmn-rounded-md vtmn-ml-4"
      @click="showModal = true"
    >
      <VtmnIcon value="filter-line" variant="reversed" />
      <span
        v-if="filterSelected > 0"
        class="filterCount vtmn-flex vtmn-justify-center vtmn-items-center vtmn-left-3/4 vtmn-top-7 vtmn-typo_text-3"
      >
        {{ filterSelected }}
      </span>
    </button>

    <Teleport to="body">
      <modal-form :show="showModal" @close="showModal = false">
        <template #header>
          <h3 class="vtmn-typo_title-3">Filter executions</h3>
        </template>

        <template #body>
          <div>
            <div class="vtmn-flex vtmn-justify-evenly">
              <VtmnRadioButton
                :identifier="'filter-test-executions'"
                labelText="Filter by test"
                name="executions-choice"
                @click="executionFilterType = 'testsExecutions'"
              />
              <VtmnRadioButton
                :identifier="'filter-conditions-executions'"
                labelText="Filter by conditions"
                name="executions-choice"
                :checked="tableExec.length > 0"
                @click="executionFilterType = 'conditionsExecutions'"
              />
            </div>

            <div
              class="modalCTA vtmn-my-8"
              :class="
                executionFilterType || filterSelected ? 'activeFilter' : ''
              "
            >
              <div class="vtmn-w-1/4 vtmn-my-4">
                <VtmnSelect
                  labelText="Filter Type"
                  v-model="typeSelected"
                  :identifier="'type-select'"
                  :options="execType"
                ></VtmnSelect>
              </div>

              <div class="vtmn-w-2/4 vtmn-my-6">
                <VtmnSearch
                  placeholder="Search filters..."
                  v-model="searchedCondition"
                  @focus="searchList = true"
                  :disabled="!typeSelected"
                />
                <ul v-if="searchList" class="searchContent vtmn-z-10">
                  <li
                    v-for="condition in filteredLabels"
                    @click="saveCondition(condition.label)"
                    :key="condition.label"
                  >
                    {{ condition.label }}
                  </li>
                </ul>
              </div>

              <div>
                <div v-for="(items, index) in tableExec" :key="index">
                  <p class="filterTitle vtmn-typo_text-1">{{ items.type }}</p>
                  <div class="selectedFilters vtmn-flex">
                    <VtmnChip
                      v-for="(label, index) in items.value"
                      variant="input"
                      size="small"
                      selected
                      @cancel="removeFilter(items, label)"
                      :key="index"
                    >
                      {{ label }}
                    </VtmnChip>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <template #footer>
          <div class="vtmn-flex vtmn-w-1/4 vtmn-m-auto vtmn-justify-between">
            <VtmnButton class="cancelBtn" @click="showModal = false"
              >Cancel</VtmnButton
            >

            <VtmnButton
              class="confirmBtn"
              @click="confirmFilter"
              :disabled="!tableExec"
            >
              Confirm
            </VtmnButton>
          </div>
        </template>
      </modal-form>
    </Teleport>
  </div>
</template>

<style scoped>
.filterBtn {
  position: relative;
  background-color: #d9dde1;
  width: 50px;
  height: 50px;
}

.modalCTA {
  opacity: 0;
}

.modalCTA.activeFilter {
  opacity: 1;
  transition: 200ms;
}

.filterTitle {
  color: var(--vtmn-semantic-color_content-visited-reversed);
}

.filterCount {
  position: absolute;
  background-color: var(--vtmn-semantic-color_background-brand-primary);
  color: var(--vtmn-semantic-color_content-action-reversed);
  width: 20px;
  height: 20px;
  border-radius: 100px;
}

.selectedFilters div {
  margin: 10px 10px;
}
</style>
