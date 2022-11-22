<script setup>
import { VtmnButton, VtmnSelect, VtmnToast } from "@vtmn/vue";
import { ref } from "vue";

let displayRadios = ref(false);
let columnCheck = ref(false);
let linesCheck = ref(false);
let cellsCheck = ref(false);
let selectCount = ref(0);
let confToSave = ref({});
let showToast = ref(false);

const columnsTable = [
  { label: "Select", value: "", disabled: true },
  { label: "Feature", value: "feature" },
  { label: "Severity", value: "severity" },
  { label: "Teams", value: "teams" },
];
const linesTable = [
  { label: "Select", value: "", disabled: true },
  { label: "Country", value: "country" },
  { label: "Type", value: "type" },
  { label: "Team", value: "team" },
  { label: "Label", value: "label" },
];
const cellsTable = [
  { label: "Select", value: "", disabled: true },
  { label: "Country", value: "country" },
  { label: "Type", value: "type" },
  { label: "Team", value: "team" },
  { label: "Label", value: "label" },
];

const saveConf = () => {
  localStorage.setItem(
    "qualityConfiguration",
    JSON.stringify(confToSave.value)
  );
  showToast.value = true;
};
</script>

<template>
  <div>
    <div class="block vtmn-ml-10">
      <div class="vtmn-ml-10 vtmn-mt-6">
        <p class="vtmn-typo_text-1 vtmn-flex vtmn-justify-left">
          This part allow you to show, hide and configure different part of a
          card like columns, lines or cells.
        </p>

        <div class="block vtmn-mt-8">
          <div class="vtmn-toggle">
            <div class="vtmn-toggle_switch">
              <input
                type="checkbox"
                id="your-choice"
                @click="displayRadios = !displayRadios"
              />
              <span aria-hidden="true"></span>
            </div>
            <label for="your-choice">Custom mod</label>
          </div>
        </div>

        <div class="radioBlock" :class="displayRadios ? 'customEnabled' : ''">
          <div class="block vtmn-mt-6">
            <input
              class="vtmn-checkbox"
              type="checkbox"
              id="choice1"
              @change="columnCheck = !columnCheck"
            />
            <label for="choice1">Columns</label>

            <div
              class="columnSelect configSelect"
              :class="columnCheck ? 'activeColumn' : ''"
            >
              <VtmnSelect
                labelText="Column type"
                id="vtmn-select"
                :options="columnsTable"
                identifier="columnType"
                v-model="confToSave.column"
                @change="columnCheck ? (selectCount += 1) : (selectCount -= 1)"
              ></VtmnSelect>
            </div>
          </div>

          <div class="block vtmn-mt-6">
            <input
              class="vtmn-checkbox"
              type="checkbox"
              id="choice2"
              @change="linesCheck = !linesCheck"
            />
            <label for="choice2">Lines</label>

            <div class="lineSelect" :class="linesCheck ? 'activeLine' : ''">
              <div class="configSelect">
                <VtmnSelect
                  labelText="Label group 1"
                  id="vtmn-select"
                  :options="linesTable"
                  identifier="firstLabelGroup"
                  v-model="confToSave.line1"
                  @change="linesCheck ? (selectCount += 1) : (selectCount -= 1)"
                ></VtmnSelect>

                <VtmnSelect
                  labelText="Label group 2 (Optional)"
                  id="vtmn-select"
                  :options="linesTable"
                  identifier="secondLabelGroup"
                  v-model="confToSave.line2"
                  @change="linesCheck ? (selectCount += 1) : (selectCount -= 1)"
                ></VtmnSelect>
              </div>
            </div>
          </div>

          <div class="block vtmn-mt-6">
            <input
              class="vtmn-checkbox"
              type="checkbox"
              id="choice3"
              @change="cellsCheck = !cellsCheck"
            />
            <label for="choice3">Cells</label>

            <div class="cellSelect" :class="cellsCheck ? 'activeCell' : ''">
              <div class="configSelect">
                <VtmnSelect
                  labelText="Cell"
                  id="vtmn-select"
                  :options="cellsTable"
                  identifier="cellsValue"
                  v-model="confToSave.cell"
                  @change="cellsCheck ? (selectCount += 1) : (selectCount -= 1)"
                ></VtmnSelect>
              </div>
            </div>
          </div>

          <div>
            <VtmnToast
              v-if="showToast"
              class="cardSaveToast"
              withCloseButton
              withIcon
            >
              <template v-slot:content>
                Cards configuration successfully saved!
              </template>
            </VtmnToast>

            <VtmnButton
              class="vtmn-flex vtmn-m-auto saveConfiguration"
              :disabled="!selectCount > 0"
              @click="saveConf"
              >Save configuration</VtmnButton
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vtmn-typo_text-1 {
  color: var(--vtmn-semantic-color_content-tertiary);
}

.radioBlock {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  opacity: 0;
  height: 0%;
  line-height: 0;
  transition: 200ms;
}

.radioBlock.customEnabled {
  opacity: 100;
  height: 100%;
  line-height: 3;
  transition: 200ms;
}

.columnSelect,
.lineSelect,
.cellSelect {
  overflow: hidden;
  height: 0;
  transition: 200ms;
}

.columnSelect.activeColumn,
.cellSelect.activeCell {
  height: 80px;
  transition: 200ms;
}

.lineSelect.activeLine {
  height: 180px;
  transition: 200ms;
}

.configSelect div {
  margin: 0 35px;
}

.configSelect div:nth-child(2) {
  margin-top: 20px;
}
</style>
