<script>
export default {
  data() {
    return {
      xTabPosition: "",
      yTabPosition: "",
      widthTabPosition: "",
      heightTabPosition: "",
    };
  },

  methods: {
    getPosition() {
      this.selectedTab;
      this.xTabPosition = event.target.getBoundingClientRect().x + "px";
      this.yTabPosition = event.target.getBoundingClientRect().y + "px";
      this.widthTabPosition = event.target.getBoundingClientRect().width + "px";
    },
  },

  computed: {
    selectedTab() {
      return this.$route.name;
    },
  },

  mounted() {
    this.xTabPosition =
      document.getElementsByClassName(" activeTab")[0].getBoundingClientRect()
        .x + "px";
    this.yTabPosition =
      document.getElementsByClassName(" activeTab")[0].getBoundingClientRect()
        .y + "px";
    this.widthTabPosition =
      document.getElementsByClassName(" activeTab")[0].getBoundingClientRect()
        .width + "px";
    this.heightTabPosition =
      document.getElementsByClassName(" activeTab")[0].getBoundingClientRect()
        .height + "px";
  },
};
</script>

<template>
  <div>
    <div class="block vtmn-pt-10 vtmn-ml-10">
      <h1 class="vtmn-text-center vtmn-typo_title-1">Quality validation</h1>

      <div
        class="settingsNav vtmn-flex vtmn-justify-left vtmn-ml-10 vtmn-mt-10"
      >
        <ul class="settingsTab">
          <router-link
            to="/settings/qualityconfiguration"
            :class="
              this.selectedTab === 'qualityConfiguration'
                ? 'activeTab'
                : this.selectedTab !== 'qualityConfiguration' &&
                  this.selectedTab === 'settings'
                ? 'activeTab'
                : ''
            "
            @click="getPosition()"
          >
            <li class="vtmn-mx-6">Cards configuration</li>
          </router-link>
          <router-link
            to="/settings/qualitypositions"
            :class="this.selectedTab === 'qualityPositions' ? 'activeTab' : ''"
            @click="getPosition()"
          >
            <li class="vtmn-mx-6">Cards position</li>
          </router-link>
          <router-link
            to="/settings/completion&success"
            :class="this.selectedTab === 'qualityCompletion' ? 'activeTab' : ''"
            @click="getPosition()"
          >
            <li class="vtmn-mx-6">Completion and success</li>
            <li class="backgroundTab"></li>
          </router-link>
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
  color: var(--vtmn-semantic-color_border-secondary);
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
