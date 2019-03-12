<template>
  <div v-if="error">
    <div class="tags">{{error.executedScenario.featureTags}}</div>
    <h3>FEATURE: {{error.executedScenario.featureName}}</h3>
    <div class="tags">{{error.executedScenario.tags}}</div>
    <h3>SCENARIO: {{error.executedScenario.name}} <span :class="'severityStyle' + (!error.executedScenario.severity || error.executedScenario.severity === '_' ? ' none' : '')">{{prettySeverity(error.executedScenario.severity)}}</span></h3>
    <div class="links">
      <Button v-if="error.executedScenario.screenshotUrl" @click="showErrorScreenshot" class="sideButton" title="Screenshot">
        <div style="height: 100px; overflow: hidden;">
          <img :src="error.executedScenario.screenshotUrl" width="200" />
        </div>
      </Button>
      <Button v-if="error.executedScenario.videoUrl" @click="showErrorVideo" class="sideButton">
        <Icon type="logo-youtube"></Icon> VIDEO
      </Button>
      <Button @click="showErrorHistory" class="sideButton">
        <Icon type="md-time"></Icon> HISTORY
      </Button>

      <error-debug-dropdown :executedScenario="error.executedScenario" :error="error" buttonName="DEBUG INFORMATION" buttonClass="sideButton"/>
    </div>
    <div class="card" style="margin-right: 240px; overflow: auto; margin-bottom: 8px;">
      <!-- display table for line backgrounds to extend all the width, even when overflowing -->
      <pre><code :style="'display: table; min-width: 100%;'" v-html="formattedScenario(error.executedScenario, error)"></code></pre>
    </div>

    <error-popups ref="errorPopups" />
  </div>
</template>

<script>
  import errorPopupsComponent from './error-popups'
  import errorDebugDropdownComponent from './error-debug-dropdown'

  import util from '../libs/util'
  import scenarioUtil from '../libs/scenario-util'

  export default {
    name: 'error-scenario',

    mixins: [
      {
        methods: scenarioUtil
      }
    ],

    props: [ 'error' ],

    components: {
      'error-popups': errorPopupsComponent,
      'error-debug-dropdown': errorDebugDropdownComponent
    },

    methods: {
      prettySeverity (severityCode) {
        return util.prettySeverity(severityCode, this)
      },

      showErrorScreenshot () {
        this.$refs.errorPopups.showScreenshot(this.error.executedScenario)
      },

      showErrorVideo () {
        this.$refs.errorPopups.showVideo(this.error.executedScenario)
      },

      showErrorHistory () {
        this.$refs.errorPopups.showHistory(this.error.executedScenario)
      },

      closePopups () {
        if (this.$refs.errorPopups) {
          this.$refs.errorPopups.closePopups()
        }
      }
    },

    mounted () {
      this.$store.dispatch('severities/ensureSeveritiesLoaded', this)
    }
  }
</script>

<style scoped>
  .links {
    float: right;
    margin: -8px 0 0 0;
    text-align: right;
  }

  .severity {
    font-weight: normal;
    color: lightgray;
    border: 1px solid lightgray;
    border-radius: 4px;
    padding: 1px 4px;
  }
  .severity.none {
    color: #ED3F14;
    border-color: #ED3F14;
  }

  .tags {
    display: block;
    font-size: 11px;
    font-weight: normal;
    color: gray;
  }
</style>
