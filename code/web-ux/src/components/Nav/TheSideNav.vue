<script setup>
import { ref, reactive } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const props = defineProps(["sideOpen"]);
const menuElements = reactive([
  { name: "Quality validation" },
  { name: "Issues" },
  { name: "Features" },
  {
    name: "Settings",
    subElements: [
      "Quality validation settings",
      "Feature settings",
      "Notification settings",
    ],
  },
  { name: "Projects" },
  { name: "Labels" },
  { name: "Regressions" },
  { name: "FAQ" },
]);
let subActive = ref(false);
let showSub = ref(false);
</script>

<template>
  <div class="side-nav vtmn-block vtmn-w-min vtmn-float-left vtmn-z-20">
    <ul
      class="vtmn-list vtmn-h-screen sideLine"
      :class="props.sideOpen ? 'openedSide' : 'closedSide'"
      role="listbox"
    >
      <li
        v-for="(item, index) in menuElements"
        class="vtmn-list_item-size--small"
        role="option"
        tabindex="0"
        :key="index"
      >
        <router-link
          @click="
            item.name === 'Settings' && props.sideOpen
              ? (showSub = !showSub)
              : (showSub = false)
          "
          :to="
            item.name != 'Settings'
              ? { path: `/${item.name.replace(/\s+/g, '-').toLowerCase()}` }
              : route.path.includes('settings')
              ? { path: route.path }
              : { path: `/${item.name.replace(/\s+/g, '-').toLowerCase()}` }
          "
          :class="showSub ? 'showSub' : 'hideSub'"
          class="icon-tile vtmn-flex vtmn-flex-1 vtmn-flex-col"
        >
          <div class="vtmn-flex">
            <span
              class="vtmn-m-3 vtmn-mb-3 vtmn-mt-3"
              :class="
                item.name === 'Quality validation'
                  ? 'vtmx-bar-chart-line'
                  : item.name === 'Issues'
                  ? 'vtmx-lightbulb-line'
                  : item.name === 'Features'
                  ? 'vtmx-list-settings-line'
                  : item.name === 'Settings'
                  ? 'vtmx-settings-line'
                  : item.name === 'Projects'
                  ? 'vtmx-counter-line'
                  : item.name === 'Labels'
                  ? 'vtmx-mist-line'
                  : item.name === 'Regressions'
                  ? 'vtmx-return-line'
                  : item.name === 'FAQ'
                  ? 'vtmx-question-line'
                  : null
              "
              role="presentation"
            ></span>
            <p
              class="vtmn-typo_text-2 vtmn-flex vtmn-items-center vtmn-ml-6 vtmn-mr-6 vtmn-w-max"
            >
              {{ item.name }}
            </p>
            <span
              class="vtmn-m-3 vtmn-mb-3 vtmn-mt-3 vtmn-w-6"
              :class="item.name === 'Settings' ? 'vtmx-chevron-up-line' : ''"
            ></span>
          </div>
          <ul
            v-if="item.name === 'Settings' && props.sideOpen"
            class="vtmn-typo_text-2 vtmn-flex-col vtmn-flex"
          >
            <router-link
              active-class="vtmn-font-bold"
              @click="subActive = true"
              :to="{ name: 'qualityConfiguration' }"
            >
              <li class="vtmn-my-2 vtmn-justify-center">Quality settings</li>
            </router-link>
            <router-link
              active-class="vtmn-font-bold"
              @click="subActive = true"
              :to="{ name: 'featuresSettings' }"
            >
              <li class="vtmn-my-2 vtmn-justify-center">Feature settings</li>
            </router-link>
            <router-link
              active-class="vtmn-font-bold"
              @click="subActive = true"
              :to="{ name: 'notificationSettings' }"
            >
              <li class="vtmn-my-2 vtmn-justify-center">
                Notification settings
              </li>
            </router-link>
          </ul>
        </router-link>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.icon-tile p {
  color: var(--vtmn-semantic-color_background-primary);
}

.icon-tile {
  color: var(--vtmn-semantic-color_background-primary);
  font-size: 24px;
}

.icon-tile.active,
.icon-tile.active p {
  background-color: var(--vtmn-semantic-color_background-primary);
  color: var(--vtmn-semantic-color_background-brand-primary);
}

.icon-tile:hover,
.icon-tile:hover p {
  color: var(--vtmn-semantic-color_background-brand-primary);
  background-color: var(--vtmn-semantic-color_background-primary);
}

.vtmn-list {
  background-color: var(--vtmn-semantic-color_background-brand-primary);
  padding-top: 5rem;
}

.vtmn-typo_text-2 {
  color: var(--vtmn-semantic-color_content-active);
}

.sideLine {
  overflow: hidden;
}

.closedSide {
  width: 55px;
  transition: 500ms;
}

.openedSide {
  width: 250px;
  transition: 500ms;
}

.side-nav {
  position: fixed;
}

.hideSub .vtmx-chevron-up-line::before {
  transform-origin: center;
  transform: rotate(0deg);
  transition: 500ms;
}

.hideSub ul {
  background-color: var(--vtmn-semantic-color_background-brand-secondary);
  height: 0px;
  transition: 500ms;
}

.showSub .vtmx-chevron-up-line::before {
  transform-origin: center;
  transform: rotate(180deg);
  transition: 500ms;
}

.showSub ul {
  background-color: var(--vtmn-semantic-color_background-brand-secondary);
  height: 120px;
  transition: 500ms;
}

.showSub ul li:hover {
  font-weight: 900;
}
</style>
