<template>
  <div>
    <div style="display: flex;">
      <div v-if="isFramed" style="flex: 0 0 auto;">
        <router-link :to="{ name: 'redirecter' }" id="home-logo" target="_blank">
          <Tooltip placement="bottom-start" :transfer="true">
            <div slot="content">
              ARA - AGILE REGRESSION ANALYZER<br>
              Fighting Against Regressions All Together
            </div>
            <img src="../assets/favicon.png" width="32" height="32"
                 style="vertical-align: middle;"></Tooltip></router-link><!-- No space between!
     --><projects-select style="flex: 1 0 auto; margin: 0 8px"/>
      </div>
      <div style="flex: 1 0 auto;">
        <Alert show-icon v-if="executionCommunication && executionCommunication.message"
               style="margin-bottom: 16px; padding-top: 7px; padding-bottom: 7px;"> <!-- Paddings: when framed and one line, be same height as combobox and button -->
          <span v-if="executionCommunication.type === 'HTML'" v-html="executionCommunication.message"/>
          <span v-else>{{executionCommunication.message}}</span>
        </Alert>
      </div>
      <div v-if="latestExecutions" style="flex: 0 0 auto; margin-bottom: 16px;">
        <Button icon="md-refresh" @click="loadLatestExecutions" style="margin-left: 8px;">REFRESH</Button>
      </div>
    </div>

    <div v-if="latestExecutions">
      <div style="white-space: nowrap; margin-right: -8px;">
        <div v-for="branch in latestExecutions" :key="branch.name"
             :style="'display: inline-block; ' +
                     'vertical-align: top; ' +
                     'width: calc(' + (100 / latestExecutions.length) + '% - 8px); ' +
                     'min-width: 700px; ' +
                     'margin-right: 8px;'">
          <nrt-cycle v-for="execution in branch.cycles" :key="execution.name" :execution="execution" v-on:requestExecution="(executionId) => requestExecution(execution, executionId)" />
        </div>
      </div>

      <div style="text-align: center; margin-top: 16px;">
        <router-link :to="{ name: 'raw-executions' }" :target="isFramed ? '_blank' : ''">
          <Button icon="md-nuclear">SHOW RAW EXECUTIONS</Button>
        </router-link>
      </div>

      <h2 style="margin-top: 16px; text-align: left;">IGNORED SCENARIOS</h2>
      <nrt-ignored-scenarios style="margin-top: 8px;"/>
    </div>

    <Spin fix v-if="loadingLatestExecutions" />
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  import projectsSelect from '../components/projects-select.vue'
  import nrtCycle from '../components/nrt-cycle'
  import nrtIgnoredScenarios from '../components/nrt-ignored-scenarios'

  export default {
    name: 'executions',

    components: {
      projectsSelect,
      nrtCycle,
      nrtIgnoredScenarios
    },

    data () {
      return {
        loadingLatestExecutions: false,
        latestExecutions: null,
        executionCommunication: null
      }
    },

    computed: {
      isFramed () {
        return this.$route.matched.some(record => record.meta.isFramed)
      }
    },

    methods: {
      loadLatestExecutions () {
        this.loadExecutionsCommunication()
        this.loadingLatestExecutions = true
        Vue.http
          .get(api.paths.executions(this) + '/latest', api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingLatestExecutions = false
            this.latestExecutions = this.toBranchCycleHierarchy(response.body)
          }, (error) => {
            this.loadingExecutions = false
            api.handleError(error)
          })
      },

      toBranchCycleHierarchy (executions) {
        let branches = []

        // executions is a flat list of executions: turn it to a hierarchy of branches and cycles
        // (branches are already sorted by the user-defined branchPosition, and then cycles are sorted by name by the server)
        for (let i in executions) {
          let execution = executions[i]
          execution.loading = false
          let branch = this.getOrCreateBranch(branches, execution.branch)
          branch.cycles.push(execution)
        }

        return branches
      },

      getOrCreateBranch (branches, branchName) {
        for (let i in branches) {
          let branch = branches[i]
          if (branch.name === branchName) {
            return branch
          }
        }

        let branch = {
          name: branchName,
          cycles: []
        }
        branches.push(branch)
        return branch
      },

      requestExecution (execution, executionId) {
        execution.loading = true
        Vue.http
          .get(api.paths.executions(this) + '/' + executionId + '/history', api.REQUEST_OPTIONS)
          .then((response) => {
            execution.loading = false
            for (const prop of Object.keys(execution)) {
              this.$set(execution, prop, undefined)
            }
            for (const prop of Object.keys(response.body)) {
              this.$set(execution, prop, response.body[prop])
            }
          }, (error) => {
            execution.loading = false
            api.handleError(error)
          })
      },

      loadExecutionsCommunication () {
        Vue.http
          .get(api.paths.communications(this) + '/executions', api.REQUEST_OPTIONS)
          .then((response) => {
            this.executionCommunication = response.body
          }, (error) => {
            api.handleError(error)
          })
      }
    },

    mounted () {
      this.loadLatestExecutions()
    },

    watch: {
      '$route' () {
        this.loadLatestExecutions()
      }
    }
  }
</script>

<style scoped>
</style>
