<script>
import cardStarter from "../components/cardStarter.vue";

export default {
  components: {
    cardStarter,
  },

  data() {
    return {
      breadCrumb: this.$route.fullPath,
      firstConnexion: false,
      configurationCards: [],
    };
  },

  created() {
    if (localStorage.getItem("conditionStored")) {
      this.configurationCards = JSON.parse(
        localStorage.getItem("conditionStored")
      );
    }
  },
};
</script>

<template>
  <div class="block vtmn-pt-10 vtmn-ml-10">
    <div class="block">
      <nav aria-label="Breadcrumb" class="vtmn-breadcrumb">
        <ol>
          <li>
            <a href="">{{ breadCrumb }}</a>
          </li>
        </ol>
      </nav>
      <h1 class="vtmn-text-center vtmn-typo_title-1">Quality validation</h1>

      <div v-if="configurationCards.length === 0">
        <card-starter class="vtmn-mt-10" />
      </div>

      <div
        v-else-if="configurationCards.length > 0"
        class="vtmn-flex vtmn-flex-wrap"
      >
        <div
          v-for="cards in configurationCards"
          class="cardBlock vtmn-mr-8"
          :key="cards.position"
        >
          <card-starter class="vtmn-mt-10" :cardInfo="cards" />
        </div>
      </div>

      <p
        v-if="!configurationCards"
        class="vtmn-typo_text-1 vtmn-m-auto vtmn-w-max vtmn-mt-10"
      >
        <svg width="24" height="24">
          <use
            xlink:href="/node_modules/@vtmn/icons/dist/vitamix/sprite/vitamix.svg#information-fill"
          />
        </svg>
        Configure cards position in settings to display other cards
      </p>

      <div
        class="block vtmn-flex vtmn-flex-row vtmn-flex-wrap vtmn-justify-center vtmn-mt-6"
        v-if="!configurationCards"
      >
        <button
          class="vtmn-btn vtmn-btn_size--large"
          @click="
            this.$router.push({
              path: 'settings/qualitySettings/:firstConnexion',
              name: 'qualitySettings',
              query: { firstConnexion: true },
            })
          "
        >
          Configure cards
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cardBlock {
  width: 45%;
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
