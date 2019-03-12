<template>
  <div v-if="problem && !loadingProblem && !loadingError">
    <h2 style="margin-top: 0;">EDIT PATTERN {{patternIndex + 1}} OF <problem-tag :problem="problem" :link="true"/></h2>

    <pattern-editor ref="patternEditor" :initialForm="patternForm" :editMode="true" />

    <Row>
      <i-col span="3">&nbsp;</i-col>
      <i-col span="21">
        <Button type="primary" @click="save" :enabled="pattern && pattern.id" :loading="saving">SAVE PATTERN MODIFICATION</Button>
      </i-col>
    </Row>
  </div>
  <Spin fix v-else-if=" loadingProblem || loadingError"/>
</template>

<script>
  import Vue from 'vue'

  import problemComponent from '../components/problem-tag'
  import patternEditorComponent from '../components/pattern-editor'

  import api from '../libs/api'
  import util from '../libs/util'
  import exceptionUtil from '../libs/exception-util'

  export default {
    name: 'problem-pattern',

    components: {
      'problem-tag': problemComponent,
      'pattern-editor': patternEditorComponent
    },

    data () {
      return {
        loadingProblem: false,
        loadingError: false,
        problem: null,
        pattern: null,
        patternIndex: 0,
        error: null,
        patternForm: null,
        saving: false
      }
    },

    computed: {
      problemId () {
        let id = this.$route.params.problemId
        return (id ? parseInt(id, 10) : null)
      },
      patternId () {
        let id = this.$route.params.patternId
        return (id ? parseInt(id, 10) : null)
      }
    },

    methods: {
      loadProblem () {
        this.loadingProblem = true
        Vue.http
          .get(api.paths.problems(this) + '/' + this.problemId, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingProblem = false
            this.problem = response.body
            let patterns = this.problem.patterns
            for (var i = 0; i < patterns.length; i++) {
              if (patterns[i].id === this.patternId) {
                this.pattern = patterns[i]
                this.patternIndex = i
                break
              }
            }
            this.initPatternForm()
          }, (error) => {
            this.loadingProblem = false
            api.handleError(error)
          })
      },

      loadError () {
        let url = api.pageUrl(api.paths.problems(this) + '/' + this.problemId + '/errors', { page: 0, size: 1 })
        this.loadingError = true
        Vue.http
          .get(url, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingError = false
            let errors = response.body.content
            this.error = (errors.length ? errors[0] : null)
            this.initPatternForm()
          }, (error) => {
            this.loadingError = false
            api.handleError(error)
          })
      },

      initPatternForm () {
        let pattern = this.pattern
        let error = this.error

        if (pattern && !this.loadingError) {
          this.patternForm = {
            useRelease: !!(pattern.release),
            useCountry: !!(pattern.country),
            useType: !!(pattern.type),
            usePlatform: !!(pattern.platform),
            useFeatureName: !!(pattern.featureName),
            useFeatureFile: !!(pattern.featureFile),
            useScenarioName: !!(pattern.scenarioName),
            useStep: !!(pattern.step),
            useStepDefinition: !!(pattern.stepDefinition),
            useException: !!(pattern.exception),

            scenarioNameStartsWith: pattern.scenarioNameStartsWith,
            stepStartsWith: pattern.stepStartsWith,
            stepDefinitionStartsWith: pattern.stepDefinitionStartsWith,

            release: (pattern.release || (error && error.executedScenario.run.execution.release)),
            country: (pattern.country || (error && error.executedScenario.run.country)),
            type: (pattern.type || (error && error.executedScenario.run.type)),
            platform: (pattern.platform || (error && error.executedScenario.run.platform)),
            featureName: (pattern.featureName || (error && error.executedScenario.featureName)),
            featureFile: (pattern.featureFile || (error && error.executedScenario.featureFile)),
            scenarioName: (pattern.scenarioName || (error && error.executedScenario.name)),
            step: (pattern.step || (error && error.step)),
            stepDefinition: (pattern.stepDefinition || (error && error.stepDefinition)),
            exception: (pattern.exception || (error && exceptionUtil.simplifyException(this.error.exception)))
          }
        }
      },

      save () {
        let self = this
        this.$refs.patternEditor.checkPattern((pattern) => {
          let patternDto = this.$refs.patternEditor.toPattern()
          this.saving = true
          Vue.http
            .put(api.paths.problemPatterns(this) + '/' + this.pattern.id, patternDto, api.REQUEST_OPTIONS)
            .then((response) => {
              self.saving = false
              // If go(-1) has no previous page to go to, we cannot verify that, so offer a link for the user to manually go to all problems
              this.$Message.success({ closable: true, content: 'Pattern updated for the problem <b>' + util.escapeHtml(this.problem.name) + '</b>' })
              self.$router.go(-1)
            }, (error) => {
              self.saving = false
              api.handleError(error)
            })
        })
      }
    },

    mounted () {
      this.loadProblem()
      this.loadError()
    },

    watch: {
      '$route' () {
        this.loadProblem()
        this.loadError()
      }
    }
  }
</script>
