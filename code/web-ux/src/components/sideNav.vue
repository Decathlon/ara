<script>
export default {
  props: {
    sideOpen: {
      type: Boolean,
      required: true,
    },
  },

  data() {
    return {
      menuElements: [
        { name: "Dashboard" },
        { name: "Issues" },
        { name: "Features" },
        { name: "Settings" },
        { name: "Projects" },
        { name: "FAQ" },
      ],
    };
  },
};
</script>

<template>
  <div class="side-nav vtmn-block vtmn-w-min vtmn-float-left">
    <ul class="vtmn-list vtmn-h-screen" role="listbox">
      <li
        v-for="item in menuElements"
        class="vtmn-list_item-size--small"
        role="option"
        tabindex="0"
        :key="item"
      >
        <a
          @click="this.$router.push(`/${item.name.toLowerCase()}`)"
          :class="
            this.$route.name === item.name.toLowerCase() ? 'active' : null
          "
          class="icon-tile vtmn-flex vtmn-flex-1"
          href=""
        >
          <span
            class="vtmn-m-3 vtmn-mb-3 vtmn-mt-3"
            :class="
              item.name === 'Dashboard'
                ? 'vtmx-bar-chart-line'
                : item.name === 'Issues'
                ? 'vtmx-lightbulb-line'
                : item.name === 'Features'
                ? 'vtmx-list-settings-line'
                : item.name === 'Settings'
                ? 'vtmx-settings-line'
                : item.name === 'Projects'
                ? 'vtmx-counter-line'
                : item.name === 'FAQ'
                ? 'vtmx-question-line'
                : null
            "
            role="presentation"
          ></span>
          <p
            class="vtmn-typo_text-2 vtmn-flex vtmn-items-center vtmn-ml-6 vtmn-mr-10"
            v-if="sideOpen"
          >
            {{ item.name }}
          </p>
        </a>
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
</style>
