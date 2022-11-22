<script setup>
import { ref, onBeforeMount, defineAsyncComponent } from "vue";
import { VtmnBreadcrumb, VtmnBreadcrumbItem } from "@vtmn/vue";
import { useRouter, useRoute } from "vue-router";

const cardStarter = defineAsyncComponent(() =>
  import("../components/QualityCards/CardStarter.vue")
);
const cardFull = defineAsyncComponent(() =>
  import("../components/QualityCards/CardFull.vue")
);
let configurationCards = ref([
  JSON.parse(localStorage.getItem("conditionStored")),
]);
let customCards = ref([
  JSON.parse(localStorage.getItem("qualityConfiguration")),
]);
let cardActive = ref("");
const router = useRouter();
const route = useRoute();

const selectedCard = (data) => {
  localStorage.setItem(
    "executionFilters",
    JSON.stringify([{ value: ["Sanity Check"], type: "Labels" }])
  );
  window.scrollTo({
    top: 0,
    behavior: "smooth",
  });
  window.history.replaceState(
    history.state,
    "",
    "/quality-validation/?cardID=" + data.position
  );
  cardActive.value = data.position;
};

const qualityView = () => {
  cardActive.value = "";
  router.push("/quality-validation");
};

onBeforeMount(() => {
  if (route.query.cardID) {
    cardActive.value = route.query.cardID;
  }

  if (localStorage.getItem("conditionStored")) {
    configurationCards.value = JSON.parse(
      localStorage.getItem("conditionStored")
    );
  }

  if (localStorage.getItem("qualityConfiguration")) {
    customCards.value.push(
      JSON.parse(localStorage.getItem("qualityConfiguration"))
    );
  }
});
</script>

<template>
  <div class="vtmn-pt-10 vtmn-ml-10">
    <div>
      <VtmnBreadcrumb aria-label="Breadcrumb">
        <VtmnBreadcrumbItem icon="home-line" @click="qualityView()">
          Home
        </VtmnBreadcrumbItem>
      </VtmnBreadcrumb>
      <div :class="cardActive ? 'singleCardDisplay' : ''">
        <h1 class="vtmn-text-center vtmn-typo_title-1">Quality validation</h1>

        <div v-if="!configurationCards[0]" class="vtmn-w-1/3 vtmn-m-auto">
          <card-starter class="vtmn-mt-10" />
        </div>

        <ul
          v-else-if="customCards[0]"
          class="vtmn-flex vtmn-flex-wrap vtmn-justify-center vtmn-content-start"
          :style="
            !cardActive
              ? 'height: ' + configurationCards.length * 40 + 'vh'
              : ''
          "
        >
          <li
            v-for="cards in configurationCards"
            class="cardBlock vtmn-m-5"
            :class="cardActive == cards.position ? ' isActive' : ''"
            :key="cards.position"
          >
            <ul
              class="cardTabs vtmn-w-full vtmn-top-0 vtmn-absolute vtmn-flex vtmn-justify-around vtmn-rounded-t-md vtmn-text-center"
              :class="cardActive == cards.position ? ' show' : ' '"
            >
              <li
                class="vtmn-rounded-t-md vtmn-py-2 vtmn-typo_title-4 activeCardTab"
              >
                Executions
              </li>
              <li class="vtmn-rounded-t-md vtmn-py-2 vtmn-typo_title-4">
                Completions
              </li>
            </ul>

            <card-full
              :cardInfo="cards"
              :cardValue="customCards[0]"
              :cardActive="cardActive == cards.position"
              @cardSelected="selectedCard"
            />
          </li>
        </ul>

        <div
          v-else-if="configurationCards[0]"
          class="vtmn-flex vtmn-flex-wrap vtmn-justify-center"
        >
          <div
            v-for="cards in configurationCards"
            class="cardBlock vtmn-mr-8"
            :key="cards.position"
          >
            <card-starter class="vtmn-mt-10" :cardInfo="cards" />
          </div>
        </div>

        <div
          v-if="!configurationCards[0]"
          class="vtmn-flex vtmn-items-center vtmn-flex-col vtmn-mt-10"
        >
          <p class="vtmn-typo_text-1">
            <svg width="24" height="24">
              <use
                xlink:href="/node_modules/@vtmn/icons/dist/vitamix/sprite/vitamix.svg#information-fill"
              />
            </svg>
            Configure cards position in settings to display other cards
          </p>

          <button
            class="vtmn-btn vtmn-btn_size--large vtmn-mt-6"
            @click="
              router.push({
                path: 'settings/qualityPositions/:firstConnexion',
                name: 'qualityPositions',
                query: { firstConnexion: true },
              })
            "
          >
            Configure cards
          </button>
        </div>
      </div>
    </div>
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
</style>
