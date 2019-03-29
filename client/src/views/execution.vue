<template>
  <div>
    <aside v-if="execution && teamsAssignableToProblems.length > 0">
      <div style="margin:0 auto; padding:0;">
        <Button v-bind:style="{display: (this.executionShortener ? 'block': 'none')}" style="margin: 10px 0; width:100%" icon="md-search"
          @click="applyFilters()">
          APPLY FILTERS
        </Button>  
        
        <Button style="display: block; margin: 10px 0; width:100%;" icon="md-backspace" :disabled="!hasFilter"
                @click="removeAllFilters()">REMOVE ALL FILTERS
        </Button>
      </div>

      <div class="criteria">
        <div class="name">
          TEAM
          <a v-if="filter.team" @click="filter.team = ''; recomputeQueryString()" title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <Select v-model="filter.team" filterable placeholder="All" @on-change="recomputeQueryString">
          <Option value="" :label="'\u00A0'"/>
          <Option :value="-404" label="(No team)" style="font-style: italic;">
            (No team)
            <span class="total important">{{total('team', -404).text}}</span>
          </Option>
          <Option v-for="team in teamsAssignableToProblems" :value="team.id" :key="team.id" :label="team.name">
            {{team.name}}
            <span class="total">{{total('team', team.id).text}}</span>
          </Option>
        </Select>
      </div>

      <div class="criteria">
        <div class="name">
          TYPE
          <a v-if="filter.type" @click="filter.type = ''; recomputeQueryString()" title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <RadioGroup v-model="filter.type" @on-change="recomputeQueryString">
          <div v-for="(type, index) in types" :key="index" @click="deselect('type', type.code)">
            <Radio :label="type.code">
              {{type.name}}
              <span class="total">{{total('type', type.code).text}}</span>
            </Radio>
          </div>
        </RadioGroup>
      </div>

      <div class="criteria">
        <div class="name">
          COUNTRY
          <a v-if="filter.country" @click="filter.country = '';recomputeQueryString()"
             title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <RadioGroup v-model="filter.country" @on-change="recomputeQueryString">
          <div v-for="(country, index) in countries" :key="index" @click="deselect('country', country.code)">
            <Radio :label="country.code">
              {{country.name}}
              <span class="total">{{total('country', country.code).text}}</span>
            </Radio>
          </div>
        </RadioGroup>
      </div>

      <div class="criteria">
        <div class="name">
          SEVERITY
          <a v-if="filter.severity" @click="filter.severity = '';recomputeQueryString()"
             title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <RadioGroup v-model="filter.severity" @on-change="recomputeQueryString">
          <div v-for="severity in severities" :key="severity.code" @click="deselect('severity', severity.code)">
            <Radio :label="severity.code">
              {{severity.name}}
              <span class="total">{{total('severity', severity.code).text}}</span>
            </Radio>
          </div>
          <div v-if="total('severity', 'none').text" @click="deselect('severity', 'none')">
            <Radio label="none"> <!-- Show this only if there are scenarios without severity -->
              No Severity!
              <span class="total" style="color: #ED3F14;">{{total('severity', 'none').text}}</span>
            </Radio>
          </div>
        </RadioGroup>
      </div>

      <div class="criteria">
        <div class="name">
          HANDLING
          <a v-if="filter.handling" @click="filter.handling = '';recomputeQueryString()"
             title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <RadioGroup v-model="filter.handling" @on-change="recomputeQueryString">
          <div @click="deselect('handling', 'UNHANDLED')" title="Scenarios having only errors without associated problems or with only reappearing problems">
            <Radio label="UNHANDLED">
              Unhandled
              <span class="total">{{total('handling', 'UNHANDLED').text}}</span>
            </Radio>
          </div>
          <div @click="deselect('handling', 'HANDLED')" title="Scenarios having at least one error with associated and not reappearing problem(s)">
            <Radio label="HANDLED">
              Handled
              <span class="total">{{total('handling', 'HANDLED').text}}</span>
            </Radio>
          </div>
        </RadioGroup>
      </div>

      <div class="criteria">
        <div class="name">
          FEATURE
          <a v-if="filter.feature" @click="filter.feature = '';recomputeQueryString()"
             title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <Input v-model="filter.feature" @on-change="recomputeQueryString"/>
      </div>

      <div class="criteria">
        <div class="name">
          SCENARIO'S NAME
          <a v-if="filter.scenario" @click="filter.scenario = '';recomputeQueryString()"
             title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <Input v-model="filter.scenario" @on-change="recomputeQueryString"/>
      </div>

      <div class="criteria">
        <div class="name">
          FAILED STEP
          <a v-if="filter.step" @click="filter.step = '';recomputeQueryString()" title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <Input v-model="filter.step" @on-change="recomputeQueryString"/>
      </div>

      <div class="criteria">
        <div class="name">
          EXCEPTION
          <a v-if="filter.exception" @click="filter.exception = '';recomputeQueryString()"
             title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <Input v-model="filter.exception" @on-change="recomputeQueryString"/>
      </div>

      <div class="criteria">
        <div class="name">
          OPTIONS
          <a v-if="filter.withSucceed || filter.scenarioDetails"
             @click="filter.withSucceed = ''; filter.scenarioDetails = '';recomputeQueryString()" title="Remove this filter">
            <Icon type="md-backspace"/>
            REMOVE</a>
        </div>
        <Checkbox v-model="filter.withSucceed" true-value="true" false-value="" @on-change="toggleShowSucceedScenarios"
                  title="First enable a few filters for it not to be slow">
          Show succeed scenarios <span style="color: gray;">(slow)</span>
        </Checkbox>
        <Checkbox v-model="filter.scenarioDetails" true-value="true" false-value="" @on-change="recomputeQueryString"
                  title="First enable a few filters for it not to be slow">
          Show scenario details <span style="color: gray;">(very slow)</span>
        </Checkbox>
      </div>
    </aside>

    <main v-if="execution && features">
      <div style="margin: 0 auto;">
        <nrt-cycle :execution="executionHistoryPoint"
                   :hideButtons="true"
                   :routerReplace="true"
                   :problems="problems"
                   :problemTotals="problemTotals()"
                   :activeProblemId="filter.problem"
                   :filteredTeamId="filter.team"
                   v-on:requestExecution="loadExecution(); loadHistory()"
                   v-on:dispatch-selected-problem="dispatchSelectedProblem"/>
      </div>

      <h1 style="text-align: center;">
        <span v-if="counts.total === 0">No {{filter.withSucceed ? '' : 'failed'}} scenario</span>
        <span v-else-if="counts.matching === counts.total && counts.total === 1">Showing the only {{filter.withSucceed ? '' : 'failed'}} scenario</span>
        <span v-else-if="counts.matching === counts.total">Showing all <strong>{{counts.total}}</strong> {{filter.withSucceed ? '' : 'failed'}} scenarios</span>
        <span v-else>Showing <strong>{{counts.matching}}</strong> filtered scenario{{counts.matching === 1 ? '' : 's'}} out of the <strong>{{counts.total}}</strong> {{filter.withSucceed ? '' : 'failed'}} scenario{{counts.total === 1 ? '' : 's'}}</span>
      </h1>

      <run-features :execution="execution" :runFeatures="features" :scenarioDetails="filter.scenarioDetails" :filteredTeamId="filter.team"/>
    </main>

    <Spin fix v-if="loadingExecution"/>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import util from '../libs/util'

  import runFeaturesComponent from '../components/run-features'
  import nrtCycleComponent from '../components/nrt-cycle'

  const containsCode = function (list, code) {
    for (var i in list) {
      if (list[i].code === code) {
        return true
      }
    }
    return false
  }

  const DEFAULT_FILTER = {
    team: '',
    type: '',
    country: '',
    severity: '',
    handling: '',
    feature: '',
    scenario: '',
    step: '',
    exception: '',
    withSucceed: '',
    scenarioDetails: '',
    problem: ''
  }

  export default {
    name: 'execution',

    components: {
      'run-features': runFeaturesComponent,
      'nrt-cycle': nrtCycleComponent
    },

    data () {
      return {
        loadingExecution: false,
        loadedWithSuccesses: false,
        execution: null,

        executionHistoryPoint: null,

        filter: { ...DEFAULT_FILTER },
        totals: {},
        totalsIfClicked: {},
        counts: {
          matching: 0, // scenarios
          total: 0 // scenarios
        },
        features: [],
        executionShortener: null
      }
    },

    computed: {
      teamsAssignableToProblems () {
        return this.$store.getters['teams/teamsAssignableToProblems'](this)
      },

      severities () {
        return this.$store.getters['severities/severities'](this)
      },

      executionId () {
        return this.$route.params.id
      },

      isRunning () {
        if (this.execution) {
          for (var job of [this.execution, ...this.execution.countryDeployments, ...this.execution.runs]) {
            if (job.status === 'PENDING' || job.status === 'RUNNING') {
              return true
            }
          }
        }
      },

      hasFilter () {
        for (var propertyName in this.filter) {
          if (this.filter[propertyName]) {
            return true
          }
        }
        return false
      },

      problems () {
        let problems = []
        for (let run of this.execution.runs) {
          for (let scenario of run.executedScenarios) {
            if (scenario.errors) {
              for (let error of scenario.errors) {
                if (error.problems) {
                  for (let problem of error.problems) {
                    if (!problems.find(p => p.id === problem.id)) {
                      problems.push(problem)
                    }
                  }
                }
              }
            }
          }
        }
        return problems
      },

      types () {
        let types = []
        if (this.execution) {
          for (let i in this.execution.runs) {
            let run = this.execution.runs[i]
            if (!containsCode(types, run.type.code)) {
              types.push(run.type)
            }
          }
        }
        return types
      },

      countries () {
        let countries = []
        if (this.execution) {
          for (let i in this.execution.runs) {
            let run = this.execution.runs[i]
            if (!containsCode(countries, run.country.code)) {
              countries.push(run.country)
            }
          }
        }
        return countries
      }
    },

    methods: {
      applyFilters () {
        this.recomputeQueryString()
        this.filterFeatures()
      },

      dispatchSelectedProblem (problemId) {
        this.filter.problem = problemId
        this.recomputeQueryString()
      },

      loadExecution () {
        this.$store.dispatch('severities/ensureSeveritiesLoaded', this)
        this.fromQueryString()
        let loadSuccesses = this.filter.withSucceed
        let url = api.paths.executions(this) + '/' + this.executionId + (loadSuccesses ? '/with-successes' : '')
        this.loadingExecution = true
        util.ifFeatureEnabled('execution-shortener', function () {
          this.executionShortener = true
        }.bind(this), function () {
          this.executionShortener = false
        }.bind(this))
        Vue.http
          .get(url, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingExecution = false
            this.loadedWithSuccesses = loadSuccesses
            this.execution = response.body
            this.filterFeatures()
          }, (error) => {
            this.loadingExecution = false
            api.handleError(error)
          })
      },

      filterFeatures () {
        this.counts.matching = 0
        this.counts.total = 0
        this.$set(this, 'totals', {})
        this.$set(this, 'totalsIfClicked', {})

        if (this.execution && this.execution.runs) {
          this.loadingExecution = true
          if (this.executionShortener) {
            let url = api.paths.executions(this) + '/' + this.execution.id + '/filtered'
            Vue.http
              .post(url, this.filter, api.REQUEST_OPTIONS)
              .then((response) => {
                this.execution = response.body
                this.filterExecutionOnServer()
                this.loadingExecution = false
              }, (error) => {
                api.handleError(error)
                this.loadingExecution = false
              })
          } else {
            this.filterExecutionOnClient()
            this.loadingExecution = false
          }
        }
      },

      filterExecutionOnServer () {
        this.features = []
        let totalCriterion = this.totalCriterion()
        for (var idx in this.execution.qualitySeverities) {
          let qualitySeverity = this.execution.qualitySeverities[idx]
          if (qualitySeverity.severity && qualitySeverity.severity.name !== 'Global') {
            let sCounts = qualitySeverity.scenarioCounts
            this.counts.total += (this.filter.withSucceed) ? sCounts.total : sCounts.failed
          }
        }
        for (var i in this.execution.runs) {
          let run = this.execution.runs[i]
          if (run && run.executedScenarios) {
            let lastRun = null
            let lastFeature = null
            let lastScenario = null
            for (let j in run.executedScenarios) {
              let executedScenario = run.executedScenarios[j]
              // Filter out scenarios & update counts
              this.counts.matching++
              if (lastRun == null || lastRun.id !== run.id) {
                lastRun = {
                  ...run,
                  executedScenarios: undefined,
                  features: []
                }
                this.features.push(lastRun)
                lastFeature = null
                lastScenario = null
              }

              if (lastFeature == null || lastFeature.file !== executedScenario.featureFile) {
                lastFeature = {
                  file: executedScenario.featureFile,
                  name: executedScenario.featureName,
                  scenarios: []
                }
                lastRun.features.push(lastFeature)
                lastScenario = null
              }

              if (lastScenario == null || lastScenario.id !== executedScenario.id) {
                lastScenario = {
                  ...executedScenario
                }
                lastFeature.scenarios.push(lastScenario)
              }
              // Update totals of the filter pane
              this.addMatchingTotal(totalCriterion, run, executedScenario)
              this.addMatchingTotalIfClicked(totalCriterion, run, executedScenario)
            }
          }
        }
      },

      // Deprecated.
      filterExecutionOnClient () {
        this.features = []
        let totalCriterion = this.totalCriterion()
        for (var i in this.execution.runs) {
          let run = this.execution.runs[i]

          if (run && run.executedScenarios) {
            let lastRun = null
            let lastFeature = null
            let lastScenario = null

            for (let j in run.executedScenarios) {
              let executedScenario = run.executedScenarios[j]
              // Filter out scenarios & update counts
              this.counts.total += (executedScenario.handling === 'SUCCESS' ? (this.filter.withSucceed ? 1 : 0) : 1)
              if (this.matchFilters(this.filter, run, executedScenario)) {
                // Append filtered scenarios to view
                this.counts.matching++
                if (lastRun == null || lastRun.id !== run.id) {
                  lastRun = {
                    ...run,
                    executedScenarios: undefined,
                    features: []
                  }
                  this.features.push(lastRun)
                  lastFeature = null
                  lastScenario = null
                }

                if (lastFeature == null || lastFeature.file !== executedScenario.featureFile) {
                  lastFeature = {
                    file: executedScenario.featureFile,
                    name: executedScenario.featureName,
                    scenarios: []
                  }
                  lastRun.features.push(lastFeature)
                  lastScenario = null
                }

                if (lastScenario == null || lastScenario.id !== executedScenario.id) {
                  lastScenario = {
                    ...executedScenario
                  }
                  lastFeature.scenarios.push(lastScenario)
                }
              }

              // Update totals of the filter pane
              this.addMatchingTotal(totalCriterion, run, executedScenario)
              this.addMatchingTotalIfClicked(totalCriterion, run, executedScenario)
            }
          }
        }
      },

      loadHistory () {
        // TODO NO LOADING INDICATOR FOR NOW
        // TODO IN THE FUTURE, IT WILL BE DONE WITH THE SAME API CALL!
        Vue.http
        .get(api.paths.executions(this) + '/' + this.executionId + '/history', api.REQUEST_OPTIONS)
        .then((response) => {
          this.executionHistoryPoint = response.body
        }, (error) => {
          api.handleError(error)
        })
      },

      totalCriterion () {
        let totalCriterion = {
          team: [],
          type: [],
          country: [],
          severity: [],
          handling: [
            'UNHANDLED',
            'HANDLED'
          ],
          problem: []
        }

        totalCriterion.team.push(-404) // No team
        for (let team of this.teamsAssignableToProblems) {
          totalCriterion.team.push(team.id)
        }

        for (let type of this.types) {
          totalCriterion.type.push(type.code)
        }

        for (let country of this.countries) {
          totalCriterion.country.push(country.code)
        }

        for (let severity of this.severities) {
          totalCriterion.severity.push(severity.code)
        }
        totalCriterion.severity.push('none')

        for (let problem of this.problems) {
          totalCriterion.problem.push(problem.id)
        }

        return totalCriterion
      },

      removeAllFilters () {
        for (var propertyName in this.filter) {
          this.filter[propertyName] = ''
        }
        this.recomputeQueryString()
      },

      deselect (propertyName, value) {
        if (this.filter[propertyName] === value) {
          this.filter[propertyName] = ''
          this.recomputeQueryString()
        }
      },

      matchFilters (filter, run, executedScenario) {
        return (
          (this.filter.withSucceed || executedScenario.handling !== 'SUCCESS') &&

          this.matchFilterType(filter, run) &&
          this.matchFilterCountry(filter, run) &&

          this.matchFilterTeam(filter, executedScenario) &&
          this.matchFilterSeverity(filter, executedScenario) &&
          this.matchFilterHandling(filter, executedScenario) &&
          this.matchFilterFeature(filter, executedScenario) &&
          this.matchFilterScenario(filter, executedScenario) &&
          this.matchFilterStep(filter, executedScenario) &&
          this.matchFilterException(filter, executedScenario) &&
          this.matchFilterProblem(filter, executedScenario)
        )
      },

      matchFilterType (filter, run) {
        let criteria = filter.type
        if (!criteria) {
          return true
        }
        return run.type.code === criteria
      },

      matchFilterCountry (filter, run) {
        let criteria = filter.country
        if (!criteria) {
          return true
        }
        return run.country.code === criteria
      },

      matchFilterTeam (filter, executedScenario) {
        let criteria = filter.team
        if (!criteria) {
          return true
        }
        if (criteria === -404) {
          return executedScenario.teamIds.length === 0
        }

        // Match one of the teams of the scenario?
        if (executedScenario.teamIds.indexOf(criteria) !== -1) {
          return true
        }

        // Match the team of one problem of one error of the scenario?
        return executedScenario.errors && !!executedScenario.errors.find(error =>
          error.problems && !!error.problems.find(problem => problem.blamedTeam && problem.blamedTeam.id === criteria)
        )
      },

      matchFilterSeverity (filter, executedScenario) {
        let criteria = filter.severity
        if (!criteria) {
          return true
        }
        if (criteria === executedScenario.severity) {
          return true
        }
        if (criteria === 'none' && (executedScenario.severity === '' || executedScenario.severity === '_')) {
          return true
        }
        if (criteria === 'medium' && (executedScenario.severity === '' || executedScenario.severity === '_')) {
          return true
        }
        return false
      },

      matchFilterHandling (filter, executedScenario) {
        let criteria = filter.handling
        if (!criteria) {
          return true
        }
        return criteria === executedScenario.handling
      },

      matchFilterFeature (filter, executedScenario) {
        let criteria = filter.feature
        if (!criteria) {
          return true
        }
        return executedScenario.featureName.toLowerCase().indexOf(criteria.toLowerCase()) !== -1
      },

      matchFilterScenario (filter, executedScenario) {
        let criteria = filter.scenario
        if (!criteria) {
          return true
        }
        return executedScenario.name.toLowerCase().indexOf(criteria.toLowerCase()) !== -1
      },

      matchFilterStep (filter, executedScenario) {
        let criteria = filter.step
        if (!criteria) {
          return true
        }
        return executedScenario.errors && !!executedScenario.errors.find(error =>
          error.step.toLowerCase().indexOf(criteria.toLowerCase()) !== -1
        )
      },

      matchFilterException (filter, executedScenario) {
        let criteria = filter.exception
        if (!criteria) {
          return true
        }
        return executedScenario.errors && !!executedScenario.errors.find(error =>
          error.exception.toLowerCase().indexOf(criteria.toLowerCase()) !== -1
        )
      },

      matchFilterProblem (filter, executedScenario) {
        let criteria = filter.problem
        if (!criteria) {
          return true
        }
        criteria = parseInt(criteria)
        return executedScenario.errors && !!executedScenario.errors.find(error =>
          error.problems && !!error.problems.find(problem => problem.id === criteria)
        )
      },

      total (criteria, value) {
        if (this.totals[criteria] && this.totals[criteria][value]) {
          let totalIfClicked = (this.totalsIfClicked[criteria] && this.totalsIfClicked[criteria][value]
            ? this.totalsIfClicked[criteria][value] : 0)
          let total = this.totals[criteria][value]
          return {
            totalIfClicked,
            total,
            text: '(' + (totalIfClicked === total ? '' : totalIfClicked + ' / ') + total + ')'
          }
        } else {
          return {
            totalIfClicked: 0,
            total: 0,
            text: ''
          }
        }
      },

      addMatchingTotal (totalCriterion, run, executedScenario) {
        this.addMatchingTotalTo(totalCriterion, run, executedScenario, this.totals, {})
      },

      addMatchingTotalIfClicked (totalCriterion, run, executedScenario) {
        this.addMatchingTotalTo(totalCriterion, run, executedScenario, this.totalsIfClicked, this.filter)
      },

      addMatchingTotalTo (totalCriterion, run, executedScenario, totalToUpdate, baseFilter) {
        for (let criteria in totalCriterion) {
          for (let i in totalCriterion[criteria]) {
            let value = totalCriterion[criteria][i]
            let filter = { ...baseFilter }
            filter[criteria] = value
            if (this.matchFilters(filter, run, executedScenario)) {
              if (!totalToUpdate[criteria]) {
                this.$set(totalToUpdate, criteria, {})
              }
              if (!totalToUpdate[criteria][value]) {
                this.$set(totalToUpdate[criteria], value, 0)
              }
              totalToUpdate[criteria][value]++
            }
          }
        }
      },

      toggleShowSucceedScenarios (checked) {
        this.recomputeQueryString()
        if (checked && !this.loadedWithSuccesses) {
          this.loadExecution()
        }
      },

      recomputeQueryString () {
        let query = {}
        for (var propertyName in this.filter) {
          if (this.filter[propertyName]) {
            query[propertyName] = this.filter[propertyName]
          }
        }
        if (!this.executionShortener) {
          this.loadExecution()
        }
        this.$router.replace({ params: { id: this.executionId }, query })
      },

      fromQueryString () {
        let query = this.$route.query
        if (query) {
          for (var propertyName in this.filter) {
            if (query[propertyName]) {
              this.filter[propertyName] = (propertyName === 'team' || propertyName === 'problem' ? parseInt(query[propertyName], 10)
                : query[propertyName])
            } else {
              this.filter[propertyName] = ''
            }
          }
        }
      },

      problemTotals () {
        let problemTotals = {}
        for (let problem of this.problems) {
          problemTotals[problem.id] = this.total('problem', '' + problem.id)
        }
        return problemTotals
      }
    },

    mounted () {
      this.loadExecution()
      this.loadHistory()
    },

    watch: {
      '$route' (to, from) {
        this.filter = { ...DEFAULT_FILTER }
        this.fromQueryString()

        let switchedExecution = to.params.id !== from.params.id
        if (switchedExecution) {
          this.loadExecution()
          this.loadHistory()
        } else if (this.filter.withSucceed && !this.loadedWithSuccesses && !this.loadingExecution) {
          this.loadExecution()
        }
      }
    }
  }
</script>

<style>
  aside {
    position: fixed;
    top: 60px;
    left: 0;
    bottom: 0;
    width: 250px;
    background: white;
    border-right: 1px solid #DDDEE1;
    z-index: 1;
    padding: 16px;
    overflow: auto;
  }

  main {
    margin-left: 251px;
  }

  .criteria {
    margin-top: 12px;
  }

  .criteria .name {
    color: lightgray;
    font-weight: bold;
  }

  .criteria .name a {
    color: #495060;
    font-weight: normal;
    border: 1px solid transparent;
    padding: 1px 3px;
  }

  .criteria .name a:hover {
    color: #57A3F3;
    border: 1px solid #57A3F3;
    border-radius: 3px;
  }

  .criteria .total {
    color: lightgray;
  }

  .criteria .total.important {
    color: #ED3F14;
  }

  .criteria .ivu-select-item-selected .total {
    color: white;
  }

  .criteria .ivu-select-item-selected .total.important {
    color: #FFB2B2;
  }

  .criteria .ivu-radio-group {
    display: block;
  }

  .criteria .ivu-radio-group-item,
  .criteria .ivu-checkbox-wrapper {
    display: block;
    margin: 0 -2px;
    padding: 1px 2px;
    border-radius: 3px;
  }

  .criteria .ivu-radio-group-item:hover,
  .criteria .ivu-checkbox-wrapper:hover {
    background-color: #F5F7F9;
  }

  .criteria .ivu-radio-wrapper-checked,
  .criteria .ivu-checkbox-wrapper-checked {
    color: #2D8CF0;
  }

  .ivu-dropdown-item a {
    display: block;
  }

  .discardHeader.qualityHeader {
    color: black !important;
    border-color: #E8E8E8 !important; /* Grayish border */
  }

  .quality {
    margin: 0 16px;
  }

  .quality strong {
    font-size: 1.5em;
  }
</style>
