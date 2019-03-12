<template>
  <div>
    <div classs="run" v-for="run in runFeatures" :key="run.country.code + ' ' + run.type.code">
      <h2 style="padding-left: 9px;">{{run.country.name + ' ' + run.type.name}}</h2>
      <div v-if="run.features" v-for="feature in run.features" :key="feature.file">
        <div class="featureStyle">
          <div class="featureTitleStyle">{{feature.name}}</div>
          <div v-if="feature.scenarios" v-for="(scenario, scenarioIndex) in feature.scenarios" :key="scenarioIndex" class="scenarioSeparatorStyle">
            <div class="scenarioStyle" :style="handlingStyle(scenario)">
              <Button-group shape="circle" size="small" style="float: right; margin: 3px 3px 2px 8px;">
                <Button v-if="scenario.screenshotUrl" @click="showScreenshot(scenario)" icon="md-image" title="Screenshot"/>
                <Button v-if="scenario.videoUrl" @click="showVideo(scenario)" icon="logo-youtube"
                        title="Video"/>
                <Button v-if="!hideHistoryButton" @click="showHistory(run, scenario)" icon="md-time"
                        title="History"/>
                <error-debug-dropdown :executedScenario="scenarioHierarchy(scenario, run)" buttonTitle="Debug information"
                                      style="display: inline-block;" size="small"/>
              </Button-group>
              <div class="scenarioTitleStyle">
                {{scenario.name}}
                <span :class="'severityStyle' + (!scenario.severity || scenario.severity === '_' ? ' none' : '')">{{prettySeverity(scenario.severity)}}</span>
                <span v-if="!scenario.teamIds.length"
                      :class="'none team' + (filteredTeamId === -404 ? ' filtered' : '')"
                      title="This scenario has no assigned functionalities, thus no team">
                  <Icon v-if="filteredTeamId === -404" type="ios-funnel"/>
                  (No team)
                </span>
                <span v-for="teamId in scenario.teamIds" :key="teamId"
                      :class="'team' + (filteredTeamId === teamId ? ' filtered' : '')"
                      title="Team of one of the functionalities assigned to this scenario">
                  <Icon v-if="filteredTeamId === teamId" type="ios-funnel"/>
                  {{teamsById[teamId]}}
                </span>
              </div>

              <div v-if="scenarioDetails" style="padding: 0 8px 8px 8px;">
                <pre style="overflow: auto; min-width: 100%; max-height: calc(100vh - 60px - 32px);"><code
                    :style="'display: table; min-width: 100%; padding: 0;'"
                    v-html="formattedScenario(scenario)"></code></pre>
              </div>

              <div v-if="scenario.errors.length > 0" style="margin-bottom: -5px;">
                <div v-for="error in scenario.errors" :key="error.id" class="stepStyle">
                  {{error.step}}
                  <a v-if="!scenarioDetails" @click="showScenario(scenario, error)">SHOW STEP IN SCENARIO</a>
                  <div class="errorStyle">
                    {{getErrorSummary(error.exception)}}
                    <a @click="showException(scenario, error)">SHOW COMPLETE EXCEPTION</a>
                    <div v-if="error.problems && error.problems.length">
                      <problem-tags :problems="error.problems" :filteredTeamId="filteredTeamId" :scenarioHandling="scenario.handling"/>
                      <router-link style="problem reidentify" :to="{ name: 'error', params: { id: error.id }}">
                        RE-IDENTIFY ERROR AS A NEW PROBLEM?
                      </router-link>
                    </div>
                    <div v-else
                         :style="scenario.handling === 'UNHANDLED' ? '' : 'opacity: 0.5;'"
                         :title="scenario.handling === 'UNHANDLED' ? '' : 'Not mandatory: another error is handled by a problem for this scenario'">
                      <router-link class="problem unknown" :to="{ name: 'error', params: { id: error.id }}">
                        IDENTIFY ERROR AS A PROBLEM
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="stepStyle">
                <div v-if="!scenarioDetails"><a @click="showScenario(scenario, null)">SHOW SCENARIO</a></div>
                <span class="problem success">SUCCESS</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <error-popups ref="errorPopups"/>
  </div>
</template>

<script>
  import util from '../libs/util'
  import scenarioUtil from '../libs/scenario-util'
  import exceptionUtil from '../libs/exception-util'

  import problemTagsComponent from '../components/problem-tags'

  export default {
    name: 'run-features',

    props: ['execution', 'runFeatures', 'scenarioDetails', 'filteredTeamId', 'hideHistoryButton'],

    mixins: [
      {
        methods: scenarioUtil
      },
      {
        methods: exceptionUtil
      }
    ],

    computed: {
      teamsAssignableToFunctionalities () {
        return this.$store.getters['teams/teamsAssignableToFunctionalities'](this)
      },

      teamsById () {
        let teamsById = {}
        if (this.teamsAssignableToFunctionalities) {
          for (let team of this.teamsAssignableToFunctionalities) {
            teamsById[team.id] = team.name
          }
        }
        return teamsById
      }
    },

    components: {
      'problem-tags': problemTagsComponent
    },

    methods: {
      prettySeverity (severityCode) {
        return util.prettySeverity(severityCode, this)
      },

      showScreenshot (executedScenario) {
        this.$refs.errorPopups.showScreenshot(executedScenario)
      },

      showVideo (executedScenario) {
        this.$refs.errorPopups.showVideo(executedScenario)
      },

      showHistory (run, executedScenario) {
        this.$refs.errorPopups.showHistory({
          ...executedScenario,
          errors: undefined,
          run: {
            ...run,
            executedScenarios: undefined,
            execution: {
              ...this.execution,
              runs: undefined
            }
          }
        })
      },

      showScenario (executedScenario, error) {
        this.$refs.errorPopups.showScenario(executedScenario, error)
      },

      showException (executedScenario, error) {
        this.$refs.errorPopups.showException(executedScenario, error)
      },

      closePopups () {
        if (this.$refs.errorPopups) {
          this.$refs.errorPopups.closePopups()
        }
      },

      scenarioHierarchy (scenario, run) {
        return {
          ...scenario,
          run: {
            ...run,
            execution: this.execution
          }
        }
      },

      handlingStyle (executedScenario) {
        let color = (executedScenario.handling === 'HANDLED' ? '#FFCC30'
                     : executedScenario.handling === 'UNHANDLED' ? '#ED3F14'
                     : executedScenario.handling === 'SUCCESS' ? '#19BE6B'
                     : 'black')
        return 'border-left: 5px solid ' + color + ';'
      }
    },

    beforeCreate: function () {
      this.$options.components.ErrorDebugDropdown = require('./error-debug-dropdown.vue').default
      this.$options.components.ErrorPopups = require('./error-popups.vue').default
    },

    mounted () {
      this.$store.dispatch('severities/ensureSeveritiesLoaded', this)
    }
  }
</script>

<style scoped>
  .featureStyle {
    border: 1px solid #e3e8ee;
    border-top-left-radius: 4px;
    border-top-right-radius: 4px;
    margin: 1em 0 0 0;
    padding: 0;
    background-color: #9cbed8;
    border-bottom: none;
  }

  .featureTitleStyle {
    display: block;
    text-decoration: none;
    font-weight: bold;
    color: white;
    margin: 0;
    padding: 5px 8px;
  }

  .scenarioStyle {
    background-color: white;
    border: 5px solid white;
    border-bottom: 4px solid white;
  }

  .scenarioSeparatorStyle {
    border-bottom: 1px solid #e3e8ee;
  }

  .scenarioStyle:hover {
    background-color: #F7FCFF;
    border-color: #F7FCFF;
  }

  .scenarioTitleStyle {
    font-weight: bold;
    padding: 3px 8px 4px 8px;
  }

  .videoStyle {
    background-color: #337ab7;
    border: 1px solid #2E6DA3;
    color: white;
    padding: 1px 3px;
    font-weight: bold;
    text-decoration: none;
    border-radius: 2px;
  }

  .stepStyle {
    padding: 0 8px 4px 31px;
  }

  .stepStyle:last-of-type {
    padding-bottom: 8px;
  }

  .errorStyle {
    color: gray;
  }

  .team {
    background-color: #E3E8EA;
    border-radius: 3px;
    color: #2c3e50;
    padding: 1px 3px;
    margin: 0 3px;
    font-weight: normal;
  }
  .team.none {
    background-color: #F7B9AD;
  }
  .filtered {
    background-color: yellow !important;
    color: black !important;
  }
</style>
