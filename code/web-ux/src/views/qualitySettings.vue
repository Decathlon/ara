<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
let xTabPosition = ref("");
let widthTabPosition = ref("");
let heightTabPosition = ref("");

const getPosition = () => {
  xTabPosition.value = event.target.getBoundingClientRect().x + "px";
  widthTabPosition.value = event.target.getBoundingClientRect().width + "px";
};

const selectedTab = computed(() => route.name);

onMounted(() => {
  xTabPosition.value =
    document.getElementsByClassName(" activeTab")[0].getBoundingClientRect().x +
    "px";
  widthTabPosition.value =
    document.getElementsByClassName(" activeTab")[0].getBoundingClientRect()
      .width + "px";
  heightTabPosition.value =
    document.getElementsByClassName(" activeTab")[0].getBoundingClientRect()
      .height + "px";
});
</script>

<template>
  <div>
    <div class="block vtmn-pt-10">
      <h1 class="vtmn-text-center vtmn-typo_title-1">Quality validation</h1>

      <div
        class="settingsNav vtmn-flex vtmn-justify-left vtmn-ml-10 vtmn-mt-10"
      >
        <ul class="settingsTab vtmn-ml-10">
          <router-link
            to="/settings/qualityconfiguration"
            :class="
              selectedTab === 'qualityConfiguration'
                ? 'activeTab'
                : selectedTab !== 'qualityConfiguration' &&
                  selectedTab === 'settings'
                ? 'activeTab'
                : ''
            "
            @click="getPosition()"
          >
            <li class="vtmn-mx-6">Cards configuration</li>
          </router-link>
          <router-link
            to="/settings/qualitypositions"
            :class="selectedTab === 'qualityPositions' ? 'activeTab' : ''"
            @click="getPosition()"
          >
            <li class="vtmn-mx-6">Cards position</li>
          </router-link>
          <router-link
            to="/settings/completion&success"
            :class="selectedTab === 'qualityCompletion' ? 'activeTab' : ''"
            @click="getPosition()"
          >
            <li class="vtmn-mx-6">Completion and success</li>
          </router-link>
          <li class="backgroundTab"></li>
        </ul>
      </div>

      <router-view></router-view>
    </div>
  </div>
</template>

<style scoped>
.vtmn-typo_title-1 {
  color: var(--vtmn-semantic-color_background-brand-primary);
}

.settingsNav ul {
  display: flex;
}

.settingsNav ul li {
  padding: 10px 15px;
  border-radius: 100px;
  color: var(--vtmn-semantic-color_border-primary);
}

.settingsTab a {
  cursor: pointer;
}

.activeTab li {
  color: #ffffff !important;
}

.backgroundTab {
  z-index: -1;
  position: absolute;
  background-color: var(--vtmn-semantic-color_content-active);
  width: v-bind(widthTabPosition);
  height: v-bind(heightTabPosition);
  left: v-bind(xTabPosition);
  top: v-bind(yTabPosition);
  transition: 200ms;
}
</style>
