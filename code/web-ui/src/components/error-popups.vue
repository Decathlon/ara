<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (C) 2019 by the ARA Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ 	 http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
<template>
  <div>
    <Modal v-model="screenshotIsVisible" :title="'Screenshot' + (executedScenario ? ' for scenario: ' + executedScenario.name : '')" :width="900" style="text-align: center;" class="noFooter">
      <div v-if="executedScenario && currentPopup === 'screenshot'">
        <div style="height: 0px; display: inline-block; float: left; position: relative; top: -58px; left: 8px;">
          <a :href="$sanitizeUrl(executedScenario.screenshotUrl)" rel="noopener" target="_blank">
            <Button><Icon type="md-open"/> SHOW FULLSCREEN</Button>
          </a>
        </div>
        <!-- 852 = 1136 * 75/100 -->
        <!-- Shadow because sometimes, the screenshot is blank and white -->
        <img :src="executedScenario.screenshotUrl" width="852" style="box-shadow: 0 0 8px lightgray;" />
      </div>
    </Modal>

    <Modal v-model="videoIsVisible" :title="'Video' + (executedScenario ? ' for scenario: ' + executedScenario.name : '')" :width="900" style="text-align: center;" class="noFooter">
      <div v-if="executedScenario && currentPopup === 'video'">
        <div style="height: 0px; display: inline-block; float: left; position: relative; top: -58px; left: 2px;">
          <a :href="$sanitizeUrl(executedScenario.videoUrl)" rel="noopener" target="_blank">
            <Button><Icon type="md-open"/> SHOW FULLSCREEN</Button>
          </a>
        </div>
        <!-- 864 = 1152 * 75/100  |  576 = 768 * 75/100 -->
        <video width="864" height="576" :src="executedScenario.videoUrl" controls autoplay style="box-shadow: 0 0 8px lightgray;" />
      </div>
    </Modal>

    <Modal v-model="historyIsVisible" :title="'History' + (executedScenario ? ' for scenario: ' + executedScenario.name : '')" width="90%" style="text-align: center;" class="noFooter">
      <scenario-history ref="history"/>
    </Modal>

    <Modal v-model="exceptionIsVisible" :title="'Exception' + (executedScenario ? ' for scenario: ' + executedScenario.name : '')" width="70%" class="noFooter">
      <div v-if="error && currentPopup === 'exception'">
        <pre style="box-shadow: 0 0 8px lightgray; max-height: 500px; overflow: auto;"><code>{{error.exception}}</code></pre>
      </div>
    </Modal>

    <Modal v-model="scenarioIsVisible" title="Scenario Execution" width="80%" class="noFooter">
      <div v-if="executedScenario && currentPopup === 'scenario'">
        <div class="tags">{{executedScenario.featureTags}}</div>
        <h3>FEATURE: {{executedScenario.featureName}}</h3>
        <div class="tags">{{executedScenario.tags}}</div>
        <h3>SCENARIO: {{executedScenario.name}} <span :class="'severityStyle' + (!executedScenario.severity || executedScenario.severity === '_' ? ' none' : '')">{{prettySeverity(executedScenario.severity)}}</span></h3>
        <div style="overflow: auto;">
          <!-- display table for line backgrounds to extend all the width, even when overflowing -->
          <pre><code style="display: table; min-width: 100%; padding: 0;" v-html="formattedScenario(executedScenario, error)"></code></pre>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script>
  import scenarioHistoryComponent from '../components/scenario-history'

  import util from '../libs/util'
  import scenarioUtil from '../libs/scenario-util'

  export default {
    name: 'error-popups',

    mixins: [
      {
        methods: scenarioUtil
      }
    ],

    components: {
      'scenario-history': scenarioHistoryComponent
    },

    data () {
      return {
        executedScenario: null,
        error: null,

        screenshotIsVisible: false,
        videoIsVisible: false,
        historyIsVisible: false,
        exceptionIsVisible: false,
        scenarioIsVisible: false,

        currentPopup: ''
      }
    },

    methods: {
      prettySeverity (severityCode) {
        return util.prettySeverity(severityCode, this)
      },

      showScreenshot (executedScenario) {
        this.executedScenario = executedScenario
        this.screenshotIsVisible = true
        this.currentPopup = 'screenshot'
      },

      showVideo (executedScenario) {
        this.executedScenario = executedScenario
        this.videoIsVisible = true
        this.currentPopup = 'video'
      },

      showHistory (executedScenario) {
        this.executedScenario = executedScenario
        this.historyIsVisible = true
        this.$refs.history.load(executedScenario)
        this.currentPopup = 'history'
      },

      showException (executedScenario, error) {
        this.executedScenario = executedScenario
        this.error = error
        this.exceptionIsVisible = true
        this.currentPopup = 'exception'
      },

      showScenario (executedScenario, error) {
        this.executedScenario = executedScenario
        this.error = error
        this.scenarioIsVisible = true
        this.currentPopup = 'scenario'
      },

      closePopups () {
        if (this.historyIsVisible && this.$refs.history) {
          this.$refs.history.closePopups()
        }
        this.executedScenario = null
        this.error = null
        this.scenarioIsVisible = false
        this.videoIsVisible = false
        this.historyIsVisible = false
        this.exceptionIsVisible = false
        this.scenarioIsVisible = false
        this.currentPopup = ''
      }
    },

    mounted () {
      this.$store.dispatch('severities/ensureSeveritiesLoaded', this)
    }
  }
</script>

<style scoped>
  .tags {
    display: block;
    font-size: 11px;
    font-weight: normal;
    color: gray;
  }
</style>
