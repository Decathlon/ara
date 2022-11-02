<script>
import {
  VtmnRadioButton,
  VtmnIcon,
  VtmnButton,
  VtmnSelect,
  VtmnSearch,
  VtmnChip,
} from "@vtmn/vue";
import modalForm from "./modalForm.vue";

export default {
  components: {
    VtmnRadioButton,
    VtmnIcon,
    modalForm,
    VtmnButton,
    VtmnSelect,
    VtmnSearch,
    VtmnChip,
  },

  data() {
    return {
      showModal: false,
      searchList: false,
      execType: [
        { label: "Select", value: "", disabled: true },
        { label: "Teams", value: "Teams" },
        { label: "Features", value: "Features" },
        { label: "Labels", value: "Labels" },
        { label: "Severities", value: "Severities" },
        { label: "Discarded", value: "Discarded" },
        { label: "Success", value: "Success" },
        { label: "Regressions", value: "Regressions" },
      ],
      tableExec: [],
      executionFilterType: "",
      typeSelected: "",
      labelsType: [
        { label: "France", value: "France" },
        { label: "Belgium", value: "Belgium" },
        { label: "Integ. Other", value: "Integ. Other" },
        { label: "Day", value: "team" },
        { label: "Night", value: "status" },
      ],
      teamsType: [
        { label: "Get / DELIEVERY Lille", value: "Delievery" },
        { label: "Get / OMARE", value: "Omare" },
        { label: "Get / Supply & log", value: "SupplyLog" },
      ],
      searchedCondition: "",
      sameType: false,
    };
  },

  methods: {
    saveCondition(data) {
      this.searchList = false;
      if (this.tableExec.length > 0) {
        for (var i = 0; i <= this.tableExec.length; i++) {
          if (this.tableExec[i]?.type === this.typeSelected) {
            this.tableExec[i].value.push(data);
          }
        }
      } else if (data) {
        this.tableExec.push({
          value: [data],
          type: this.typeSelected,
        });
      }
    },

    removeFilter(filter, filterToRemove) {
      for (var k = 0; k < this.tableExec.length; k++) {
        if (this.tableExec[k].type === filter.type) {
          const indexToRemove = this.tableExec[k].value.indexOf(filterToRemove);

          if (indexToRemove) {
            this.tableExec[k].value.splice(indexToRemove, 1);
          }
        }
      }
    },

    confirmFilter() {
      this.showModal = false;
      localStorage.setItem("executionFilters", JSON.stringify(this.tableExec));
    },
  },

  computed: {
    filteredProducts() {
      return this.labelsType.filter((p) => {
        return (
          p.label.toLowerCase().indexOf(this.searchedCondition.toLowerCase()) !=
          -1
        );
      });
    },

    filterSelected() {
      return JSON.parse(localStorage.getItem("executionFilters")).length;
    },
  },

  created() {
    if (localStorage.getItem("executionFilters")) {
      this.tableExec = JSON.parse(localStorage.getItem("executionFilters"));
    }
  },

  watch: {
    typeSelected: function () {
      for (var j = 0; j < this.tableExec.length; j++) {
        if (this.tableExec[j].type === this.typeSelected) {
          return (this.similar = true);
        }
      }

      if (this.sameType || !this.tableExec) {
        this.tableExec.push({
          value: [],
          type: this.typeSelected,
        });
      }
    },
  },
};
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
                    v-for="condition in filteredProducts"
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
