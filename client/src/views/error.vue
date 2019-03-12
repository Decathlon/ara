<template>
  <div v-if="error && error.executedScenario && error.executedScenario.run">
    <div v-if="error.executedScenario.run.execution && error.executedScenario.run.execution.discardReason" class="discardHeader">
      This execution has been discarded: <span class="discardReason">{{error.executedScenario.run.execution.discardReason}}</span>
    </div>

    <h2 style="margin-top: 0;">ERROR</h2>
    <table border class="table">
      <thead>
        <tr>
          <th>Test Date</th>

          <th>Cycle</th>
          <th>Branch</th>
          <th>Release</th>
          <th>Version</th>

          <th>Country</th>
          <th>Type</th>
          <th>Platform</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><router-link :to="{ name: 'execution', params: { id: error.executedScenario.run.execution.id }}">{{util.formatDate(error.executedScenario.run.execution.testDateTime)}}</router-link></td>

          <td>{{error.executedScenario.run.execution.name}}</td>
          <td>{{error.executedScenario.run.execution.branch}}</td>
          <td>{{error.executedScenario.run.execution.release}}</td>
          <td>{{error.executedScenario.run.execution.version}}</td>

          <td>{{error.executedScenario.run.country.name}}</td>
          <td>{{error.executedScenario.run.type.name}}</td>
          <td>{{error.executedScenario.run.platform}}</td>
        </tr>
      </tbody>
    </table>

    <error-scenario :error="error" ref="errorScenario" />

    <div class="card" style="clear: both; max-height: 200px; overflow: auto;">
      <pre><code>{{error.exception}}</code></pre>
    </div>

    <pattern-editor ref="patternEditor" :initialForm="patternForm" />

    <h2 style="clear: both;">APPEND TO AN EXISTING PROBLEM...</h2>
    <Button type="primary" @click="showingProblems = true; $refs.problems.requestProblems(); $refs.problems.focus()">APPEND TO PROBLEM</Button>
    <Modal v-model="showingProblems" @on-cancel="showingProblems = false"
           title="Append to Existing Problem" okText="Close" cancelText=""
           width="auto" class="problemsModal noFooter">
      <problems ref="problems" selectButtonText="APPEND TO" :inPopup="true" v-on:select="appendToProblem"/>
      <div style="clear: both;"></div>
    </Modal>

    <h2 style="clear: both;">... OR CREATE A NEW PROBLEM</h2>
    <div style="width: 500px;">
      <problem-properties-editor ref="problemProperties" okText="AGGREGATE AS NEW PROBLEM" :isClosed="false" v-on:submit="createProblem"/>
    </div>

    <Spin fix v-if="loadingError"/>
  </div>
  <div v-else>
    <Spin fix v-if="loadingError"/>
  </div>
</template>

<script>
  import Vue from 'vue'

  import errorScenarioComponent from '../components/error-scenario'
  import patternEditorComponent from '../components/pattern-editor'
  import problemsComponent from '../components/problems'
  import problemPropertiesEditorComponent from '../components/problem-properties-editor'

  import api from '../libs/api'
  import util from '../libs/util'
  import exceptionUtil from '../libs/exception-util'

  export default {
    name: 'errors',

    mixins: [{
      created () {
        this.util = util
      }
    }],

    components: {
      'error-scenario': errorScenarioComponent,
      'pattern-editor': patternEditorComponent,
      'problems': problemsComponent,
      'problem-properties-editor': problemPropertiesEditorComponent
    },

    data () {
      return {
        loadingError: false,
        error: {},
        patternForm: null,
        showingProblems: false
      }
    },

    computed: {
      currentErrorId () {
        return this.$route.params.id
      }
    },

    methods: {
      callOnNextCycle (callBack) {
        setTimeout(callBack.bind(this), 0)
      },

      loadError () {
        let errorId = this.currentErrorId
        this.loadingError = true
        Vue.http
          .get(api.paths.errors(this) + '/' + errorId, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingError = false
            let error = this.error = response.body

            this.patternForm = {
              useRelease: false,
              useCountry: false,
              useType: true,
              usePlatform: false,
              useFeatureName: false,
              useFeatureFile: false,
              useScenarioName: false,
              useStep: false,
              useStepDefinition: true,
              useException: true,

              scenarioNameStartsWith: false,
              stepStartsWith: false,
              stepDefinitionStartsWith: false,

              release: error.executedScenario.run.execution.release,
              country: error.executedScenario.run.country,
              type: error.executedScenario.run.type,
              platform: error.executedScenario.run.platform,
              featureName: error.executedScenario.featureName,
              featureFile: error.executedScenario.featureFile,
              scenarioName: error.executedScenario.name,
              step: error.step,
              stepDefinition: error.stepDefinition,
              exception: exceptionUtil.simplifyException(this.error.exception)
            }

            // We just initialized the data 'error': let VueJS create the view with v-if=error, so that refs will be initialized
            this.callOnNextCycle(() => {
              this.$refs.problemProperties.doInit({})
            })
          }, (error) => {
            this.loadingError = false
            api.handleError(error)
          })
      },

      createProblem (problem, onStartCallback, onDoneCallback) {
        this.$refs.patternEditor.checkPattern((pattern) => {
          problem.patterns = [ pattern ]
          onStartCallback()
          Vue.http
            .post(api.paths.problems(this), problem, api.REQUEST_OPTIONS)
            .then((response) => {
              onDoneCallback()
              this.$Message.success({ closable: true, content: 'Problem created: <b>' + util.escapeHtml(problem.name) + '</b>' })
              this.$refs.patternEditor.requestMatchingErrors() // Update the view to see matching errors now have an associated problem
              this.$refs.problemProperties.doInit({})
            }, (error) => {
              onDoneCallback()
              api.handleError(error)
            })
        })
      },

      appendToProblem (problemId, problemName, onStartCallback, onDoneCallback) {
        this.$refs.patternEditor.checkPattern((pattern) => {
          onStartCallback()
          Vue.http
            .post(api.paths.problems(this) + '/' + problemId + '/append-pattern', pattern, api.REQUEST_OPTIONS)
            .then((response) => {
              onDoneCallback()
              this.showingProblems = false
              this.$Message.success({ closable: true, content: 'Criteria assigned to the problem <b>' + util.escapeHtml(problemName) + '</b>' })
              this.$refs.patternEditor.requestMatchingErrors() // Update the view to see matching errors now have an associated problem
              this.$refs.problemProperties.doInit({})
            }, (error) => {
              onDoneCallback()
              api.handleError(error)
            })
        })
      }
    },

    mounted () {
      this.loadError()
    },

    watch: {
      '$route' (to, from) {
        if (to.params.projectCode !== from.params.projectCode || to.params.id !== from.params.id) {
          this.loadError()
          if (this.$refs.errorScenario) {
            this.$refs.errorScenario.closePopups()
          }
        }
      }
    }
  }
</script>

<style>
  .problemsModal .ivu-modal {
    margin: 0 16px;
  }
</style>
