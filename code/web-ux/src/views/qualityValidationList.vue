<script setup>
import { ref, onBeforeMount, computed, reactive } from "vue";
import { VtmnIcon, VtmnChip } from "@vtmn/vue";
import { useRoute, useRouter } from "vue-router";
import pagination from "../components/Nav/ThePagination.vue";

const props = defineProps(["configurationCards"]);
const emit = defineEmits(["show-card"]);
let customCards = ref([
  JSON.parse(localStorage.getItem("qualityConfiguration")),
]);
let cardActive = ref("");
const route = useRoute();
const router = useRouter();
let tabSize = ref(5);
let pageNumber = ref();
let actualPage = ref(1);
let sortTab = reactive([]);

const changePage = (newPage) => {
  actualPage.value = newPage;
  router.push({ query: { page: actualPage.value } });
};

const changeSize = (newSize) => {
  tabSize.value = newSize;
};

const indexStart = computed(() => (actualPage.value - 1) * tabSize.value);

const indexEnd = computed(() => indexStart.value + tabSize.value);

const filteredTab = computed(() => {
  return sortTab.slice(indexStart.value, indexEnd.value);
});

onBeforeMount(() => {
  sortTab = props.configurationCards;
  pageNumber.value = Math.ceil(props.configurationCards.length / tabSize.value);

  if (route.query.cardID) {
    cardActive.value = route.query.cardID;
  }

  if (localStorage.getItem("qualityConfiguration")) {
    customCards.value.push(
      JSON.parse(localStorage.getItem("qualityConfiguration"))
    );
  }
});
</script>

<template>
  <div class="vtmn-pt-10 vtmn-mx-10">
    <table class="vtmn-w-full">
      <thead>
        <tr class="table-header vtmn-h-10">
          <th>Component</th>
          <th>
            Version
            <span>
              <VtmnIcon
                value="chevron-down-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
              <VtmnIcon
                value="chevron-up-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
            </span>
          </th>
          <th>
            Build date
            <span>
              <VtmnIcon
                value="chevron-down-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
              <VtmnIcon
                value="chevron-up-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
            </span>
          </th>
          <th>
            Test date
            <span>
              <VtmnIcon
                value="chevron-down-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
              <VtmnIcon
                value="chevron-up-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
            </span>
          </th>
          <th>Quality</th>
          <th>
            Status
            <span>
              <VtmnIcon
                value="chevron-down-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
              <VtmnIcon
                value="chevron-up-fill"
                class="vtmn-m-auto vtmn-cursor-pointer"
                :size="16"
              ></VtmnIcon>
            </span>
          </th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(cards, index) in filteredTab"
          class="vtmn-h-10"
          :key="index"
          :class="[
            index % 2 ? 'grey-row' : 'light-row',
            tabSize <= index ? 'table-limit' : 'table-show',
          ]"
        >
          <td class="vtmn-text-center">
            <span v-for="(condition, index) in cards.conditions" :key="index">
              <template v-if="index > 0">, </template>
              {{ condition }}
            </span>
          </td>
          <td class="vtmn-text-center">0210</td>
          <td class="vtmn-text-center">02/04/2022 15:23</td>
          <td class="vtmn-text-center">02/04/2022 15:23</td>
          <td class="vtmn-text-center">
            <p class="vtmn-mx-2">
              Sanity Ch.:
              <span
                :class="cards.status ? cards.status.toLowerCase() : ''"
                class="vtmn-font-bold"
                >100%</span
              >
              High:
              <span
                :class="cards.status ? cards.status.toLowerCase() : ''"
                class="vtmn-font-bold"
                >99%</span
              >
              Medium:
              <span
                :class="cards.status ? cards.status.toLowerCase() : ''"
                class="vtmn-font-bold"
                >97%</span
              >
              Global:
              <span
                :class="cards.status ? cards.status.toLowerCase() : ''"
                class="vtmn-font-bold"
                >98%</span
              >
            </p>
          </td>
          <td
            class="card-list-status"
            :class="cards.status ? cards.status.toLowerCase() : ''"
          >
            <VtmnChip variant="single-choice" size="small" selected>
              {{ cards.status.toUpperCase() }}
            </VtmnChip>
          </td>
          <td class="vtmn-text-center">
            <VtmnIcon
              value="eye-line"
              class="vtmn-mx-2 vtmn-cursor-pointer"
              @click="emit('show-card', cards)"
              :size="16"
            ></VtmnIcon>
            <VtmnIcon
              value="more-2-line"
              class="vtmn-mx-2 vtmn-cursor-pointer"
              :size="16"
            ></VtmnIcon>
          </td>
        </tr>
      </tbody>
    </table>
    <pagination
      :cardsLength="configurationCards.length"
      :tabLimit="tabSize"
      :pageNumber="pageNumber"
      @page-change="changePage"
      @page-size="changeSize"
    />
  </div>
</template>

<style scoped>
.cardBlock {
  width: 45%;
  position: relative;
}

.cardTabs {
  transition: 500ms;
}

.cardTabs.show {
  background-color: var(--vtmn-semantic-color_background-tertiary);
  top: -48px;
}

.cardTabs.show li {
  flex-basis: 50%;
  color: var(--vtmn-semantic-color_content-inactive);
}

.activeCardTab {
  background-color: var(--vtmn-semantic-color_background-brand-primary);
  color: var(--vtmn-semantic-color_content-action-reversed) !important;
}

h1 {
  position: relative;
  top: 0;
}

.singleCardDisplay h1 {
  position: relative;
  top: -100px;
  transition: 500ms;
}

.singleCardDisplay .cardBlock {
  display: none;
  transition: 500ms;
}

.singleCardDisplay .cardBlock.isActive {
  transform: translateY(0%);
  display: block;
  height: auto;
  min-height: 100vh;
  width: 100%;
  transition: 500ms;
}

.vtmn-typo_title-1 {
  color: var(--vtmn-semantic-color_background-brand-primary);
}

.vtmn-typo_text-1 {
  color: var(--vtmn-semantic-color_content-tertiary);
  position: relative;
}

.vtmn-typo_text-1 svg {
  position: absolute;
  left: -25px;
  top: 0;
  bottom: 0;
  fill: var(--vtmn-semantic-color_content-tertiary);
}

.vtmn-btn {
  background-color: var(--vtmn-semantic-color_content-positive);
}

.card-list-status .vtmn-chip {
  margin: auto;
}

.success {
  color: var(--vtmn-semantic-color_content-positive);
}

.card-list-status.success .vtmn-chip {
  background-color: var(--vtmn-semantic-color_content-positive);
}

.table-show {
  display: table-row;
}

.table-limit {
  display: none;
}
</style>
