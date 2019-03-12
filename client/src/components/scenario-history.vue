<template>
  <div v-if="executedScenario && history.length">
    <div class="subMenu inPopup">
      Cycle:   <Checkbox v-model="filter.cycleName"   @on-change="loadHistory" style="margin-right: 16px;">{{executedScenario.run.execution.name}}</Checkbox>
      Branch:  <Checkbox v-model="filter.branch"      @on-change="loadHistory" style="margin-right: 16px;">{{executedScenario.run.execution.branch}}</Checkbox>
      Country: <Checkbox v-model="filter.countryCode" @on-change="loadHistory" style="margin-right: 16px;">{{executedScenario.run.country.name}}</Checkbox>
      Type:    <strong>{{executedScenario.run.type.name}}</strong>
    </div>
    <div style="position: relative;">
      <div class="subMenu inPopup calendarBar">
        <!-- placement: if centered, it is not smart enough to not display out of screen if the square is at the edge of the screen, so we un-center the tooltip for it having a chance to display fully on-screen -->
        <Tooltip v-for="(execution, index) in history"
                 :key="index"
                 :placement="(index < history.length / 3 ? 'bottom-start' : index > history.length * 2 / 3 ? 'bottom-end' : 'bottom')"
                 :transfer="true"
                 :content="util.formatDate(execution.testDateTime) + ' - ' + execution.branch + '/' + execution.name + ' (' + execution.release + (execution.release && execution.version ? ' ; ' : '') + execution.version + ') : ' + (execution.status.includes('-') ? 'mixed' : execution.status)">
            <span :class="'executionBullet' + (index === currentIndex ? ' current' : '')" :style="'background:' + renderExecutionStatus(execution.status)" @click="currentIndex = index">
            <span v-if="index === 0 || execution.month !== history[index - 1].month || execution.day !== history[index - 1].day" class="date">
              <span style="font-size: 10px;">{{execution.month}}</span><br>
              {{execution.day}}
            </span>
          </span>
        </Tooltip>
      </div>
      <div style="text-align: left;">
        <h1>
          <Button-group v-if="history.length > 1" shape="circle">
            <Button :disabled="currentIndex === 0" @click="currentIndex -= 1" icon="md-skip-backward" title="Previous execution" type="primary" />
            <Button :disabled="currentIndex === history.length - 1" @click="currentIndex += 1" icon="md-skip-forward" title="Next execution" type="primary" />
          </Button-group>
          {{util.formatDate(currentExecution.testDateTime) + ' - ' + currentExecution.branch + '/' + currentExecution.name + ' (' + currentExecution.release + (currentExecution.release && currentExecution.version ? ' ; ' : '') + currentExecution.version + ') : ' + (currentExecution.status.includes('-') ? 'mixed' : currentExecution.status)}}
        </h1>

        <div v-if="currentExecution.discardReason" class="discardHeader">
          This execution has been discarded: <span class="discardReason">{{currentExecution.discardReason}}</span>
        </div>

        <run-features ref="runFeatures" :execution="currentExecution" :runFeatures="currentExecution.runs" :scenarioDetails="true" :hideHistoryButton="true"/>
      </div>
      <Spin fix v-if="loadingHistory"/>
    </div>
    <div style="color: gray; margin-top: 16px;">
      Note: history is based on the scenario name (without the "Functionalities X &amp; Y: " prefix) in a given feature file.<br>
      If a scenario is renamed (beyond the functionalities prefix), the old and new scenario names are now considered two seperate scenarios.<br>
      Same goes if a scenario is moved to another .feature or collection file (or if the file is renamed).
    </div>
  </div>
  <Spin fix v-else-if="loadingHistory"/>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import util from '../libs/util'

  import moment from 'moment'

  import runFeaturesComponent from '../components/run-features'

  export default {
    name: 'scenario-history',

    mixins: [{
      created () {
        this.util = util
      }
    }],

    components: {
      'run-features': runFeaturesComponent
    },

    data () {
      return {
        executedScenario: null,
        filter: {},
        loadingHistory: false,
        history: null,
        currentIndex: 0
      }
    },

    computed: {
      currentExecution () {
        return this.history[this.currentIndex]
      }
    },

    methods: {
      load (executedScenario) {
        this.executedScenario = executedScenario
        this.filter = {
          cycleName: true,
          branch: true,
          countryCode: true
        }
        this.history = []
        this.currentIndex = 0
        this.loadHistory()
      },

      loadHistory () {
        this.loadingHistory = true
        let input = {
          cucumberId: this.executedScenario.cucumberId,
          cycleName: (this.filter.cycleName ? this.executedScenario.run.execution.name : null),
          branch: (this.filter.branch ? this.executedScenario.run.execution.branch : null),
          countryCode: (this.filter.countryCode ? this.executedScenario.run.country.code : null),
          runTypeCode: this.executedScenario.run.type.code
        }
        Vue.http
          .post(api.paths.executedScenarios(this) + '/history', input, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingHistory = false
            this.parseHistory(response.body)
          }, (error) => {
            this.loadingHistory = false
            api.handleError(error)
          })
      },

      renderExecutionStatus (status) {
        let statusArray = status.split('-')
        var color = ['black', 'black', 'black']
        for (var i = 0; i < statusArray.length; i++) {
          if (statusArray[i] === 'passed') {
            color[i] = '#19BE6B'
          } else if (statusArray[i] === 'unhandled') {
            color[i] = '#ED3F14'
          } else if (statusArray[i] === 'handled') {
            color[i] = '#ffcc30'
          }
        }
        if (statusArray.length === 1) {
          return color[0]
        } else if (statusArray.length === 2) {
          return 'linear-gradient(-45deg, ' + color[1] + ' 50%, ' + color[0] + ' 50%)'
        }
        return 'linear-gradient(-45deg, ' + color[2] + ' 33.33%, ' + color[1] + ' 33.33%, ' + color[1] + ' 66.66%, ' + color[0] + ' 66.66%)'
      },

      parseHistory (executedScenarios) {
        let history = this.buildHistoryFrom(executedScenarios)
        this.appendStatusesTo(history)
        this.currentIndex = this.resetCurrentIndex(history)
        this.history = history
      },

      buildHistoryFrom (executedScenarios) {
        let history = []
        let lastExecution = null
        let lastRun = null
        let lastFeature = null

        for (var i in executedScenarios) {
          let executedScenario = executedScenarios[i]

          if (lastExecution === null || lastExecution.id !== executedScenario.run.execution.id) {
            lastExecution = {
              month: moment(executedScenario.run.execution.testDateTime).format('MMM'),
              day: moment(executedScenario.run.execution.testDateTime).format('D'),
              ...executedScenario.run.execution,
              runs: []
            }
            history.push(lastExecution)
            lastRun = null
            lastFeature = null
          }

          if (lastRun === null || lastRun.id !== executedScenario.run.id) {
            lastRun = {
              ...executedScenario.run,
              execution: undefined,
              features: []
            }
            lastExecution.runs.push(lastRun)
            lastFeature = null
          }

          if (lastFeature == null || lastFeature.file !== executedScenario.featureFile) {
            lastFeature = {
              file: executedScenario.featureFile,
              name: executedScenario.featureName,
              scenarios: []
            }
            lastRun.features.push(lastFeature)
          }

          lastFeature.scenarios.push(executedScenario)
        }

        return history
      },

      appendStatusesTo (history) {
        for (let execution of history) {
          var status = []
          if (execution.discardReason) {
            status.push('discarded')
          } else {
            let hasPassed = false
            let hasUnhandled = false
            let hasHandled = false
            for (let run of execution.runs) {
              for (let feature of run.features) {
                if (feature.scenarios.length) {
                  for (let scenario of feature.scenarios) {
                    if (scenario.handling === 'SUCCESS') {
                      hasPassed = true
                    } else if (scenario.handling === 'UNHANDLED') {
                      hasUnhandled = true
                    } else if (scenario.handling === 'HANDLED') {
                      hasHandled = true
                    } else {
                      throw new Error('scenario.handling ' + scenario.handling + ' not managed by client')
                    }
                  }
                }
              }
            }
            if (hasPassed) {
              status.push('passed')
            }
            if (hasHandled) {
              status.push('handled')
            }
            if (hasUnhandled) {
              status.push('unhandled')
            }
          }
          if (status.length === 0) {
            status.push('passed')
          }
          execution.status = status.join('-')
        }
      },

      resetCurrentIndex (newHistory) {
        // If there is a current history displayed and its current execution is still there in the new history, select it
        if (this.history && this.history.length) {
          let oldExecution = this.history[this.currentIndex]
          let newIndex = this.indexOfExecutionIn(oldExecution, newHistory)
          if (newIndex > -1) {
            return newIndex
          }
        }

        // First time the popup loads an history? Or previous execution has been filtered out? Select the execution that triggered the popup
        let index = this.indexOfExecutionIn(this.executedScenario.run.execution, newHistory)
        if (index > -1) {
          return index
        }

        // Last resort: select the latest execution
        return newHistory.length - 1
      },

      indexOfExecutionIn (searchedExecution, history) {
        for (let i = 0; i < history.length; i++) {
          let execution = history[i]
          if (execution.id === searchedExecution.id) {
            return i
          }
        }
        return -1
      },

      closePopups () {
        if (this.$refs.runFeatures) {
          this.$refs.runFeatures.closePopups()
        }
      }
    }
  }
</script>

<style scoped>
  .ivu-checkbox-wrapper {
    font-size: 14px;
    font-weight: bold;
  }

  .calendarBar {
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
    padding: 0 24px;
    height: 100px;
  }

  .executionBullet {
    display: inline-block;
    width: 24px;
    height: 24px;
    margin: 52px 1px 1px 1px;
    background-color: #ED3F14;
    border: 1px solid white;
    border-radius: 5px;
    cursor: pointer;
    position: relative;
    z-index: 1;
  }

  .executionBullet:hover {
    outline: 2px solid #57A3F3;
  }
  .executionBullet.current,
  .executionBullet.current:hover {
    outline: 2px solid #2D8CF0;
    cursor: default;
    z-index: 0;
  }

  .date {
    display: block;
    line-height: 12px;
    font-size: 12px;
    position: absolute;
    top: -32px;
    text-align: left;
    pointer-events: none;
    border-left: 1px solid #E9EAEC;
    padding: 0 0 4px 2px;
    left: -3px;
  }

  .discardHeader {
    margin-top: 16px;
    background-color: #FAF7F7;
  }
</style>
