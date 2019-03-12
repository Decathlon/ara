<template>
  <div>
    <div class="spinnable">
      <table border class="table">
        <thead>
          <tr>
            <th></th>
            <th>Cycle</th>
            <th>Branch</th>
            <th>Release</th>
            <th>Version</th>
            <th>Build Date</th>
            <th>Test Date</th>
            <th>Job</th>
            <th>Status</th>
            <th>Discard Reason</th>
            <th>Quality</th>

            <th>Scenarios</th>

            <th style="width: 1px; text-align: center;">Actions</th>
          </tr>
        </thead>
        <tbody v-if="executions">
          <tr v-for="execution in executions.content" :key="execution.id" :class="(execution.discardReason ? 'discarded' : '') + ' ' + (execution.status === 'RUNNING' ? 'running' : '')">
            <td style="width: 160px; text-align: center;">
              <router-link :to="{ name: 'execution', params: { id: execution.id } }">
                <Button :type="execution.status === 'RUNNING' || execution.discardReason ? 'default' : 'primary'" size="small" icon="md-bug" style="margin: 4px;">SHOW ERRORS</Button>
              </router-link>
            </td>
            <td>{{execution.name}}</td>
            <td>{{execution.branch}}</td>
            <td>{{execution.release}}</td>
            <td>{{execution.version}}</td>
            <td>{{util.formatDate(execution.buildDateTime)}}</td>
            <td>{{util.formatDate(execution.testDateTime)}}</td>
            <td><a :href="execution.jobUrl" target="_blank"><Icon type="md-open" /> JOB</a></td>
            <td style="white-space: nowrap;">
              <Icon v-if="execution.status === 'RUNNING'" type="ios-loading" size="16" class="animated-spin-icon" style="margin-right: 4px;"/>
              {{execution.status}}
            </td>
            <td><span v-if="execution.discardReason" class="discardReason">{{execution.discardReason}}</span></td>
            <td>
              <strong>{{execution.qualityStatus}}</strong>
              <span v-if="execution.qualityStatus && execution.qualityStatus !== 'INCOMPLETE'" v-for="qualitySeverity in execution.qualitySeverities" :key="qualitySeverity.severity.code" style="margin: 0 4px; display: inline-block;">
                {{qualitySeverity.severity.shortName}}: <strong>{{qualitySeverity.scenarioCounts && qualitySeverity.scenarioCounts.total > 0 ? qualitySeverity.percent + '&nbsp;%' : '-'}}</strong>
              </span>
            </td>

            <td style="min-width: 200px;">
              <nrt-progress
                v-if="execution.qualityStatus && execution.qualityStatus !== 'INCOMPLETE'"
                style="margin-bottom: 0;"
                :execution="execution"
                :counts="execution.scenarioCounts" />
            </td>

            <td style="width: 1px; padding: 8px;">
              <execution-actions-button :execution="execution" v-on:change="loadExecutions" />
            </td>
          </tr>
        </tbody>
      </table>
      <Spin fix v-if="loadingExecutions" />
    </div>
    <Page
      style="float: right;"
      :total="executions.totalElements"
      :current="executionsPaging.page + 1"
      :page-size="executionsPaging.size"
      show-total
      show-sizer
      size="small"
      @on-change="onPageChange"
      @on-page-size-change="onPageSizeChange" />

      <div style="text-align: center; clear: both; padding-top: 16px;">
        <router-link :to="{ name: 'executions' }">
          <Button icon="md-arrow-round-back">GO BACK TO EXECUTIONS DASHBOARD</Button>
        </router-link>
      </div>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import util from '../libs/util'

  import nrtProgressComponent from '../components/nrt-progress'
  import executionActionsButtonComponent from '../components/execution-actions-button'

  const DEFAULT_PAGE_SIZE = 10

  export default {
    name: 'raw-executions',

    mixins: [{
      created () {
        this.util = util
      }
    }],

    components: {
      'nrt-progress': nrtProgressComponent,
      'execution-actions-button': executionActionsButtonComponent
    },

    data () {
      return {
        loadingExecutions: false,
        executions: [],
        executionsPaging: {
          page: 0,
          size: DEFAULT_PAGE_SIZE
        }
      }
    },

    methods: {
      loadExecutions () {
        let url = api.pageUrl(api.paths.executions(this), this.executionsPaging)
        this.loadingExecutions = true
        Vue.http
          .get(url, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingExecutions = false
            this.executions = response.body
            let pageCount = this.executions.totalPages
            let lastPage = (pageCount === 0 ? 0 : pageCount - 1)
            if (this.executions.number > lastPage) {
              // There was a lot of page, we were on one of the last pages, and after reloading, the number of page is now small: go to last one
              this.executionsPaging.page = lastPage
              this.loadExecutions()
            }
          }, (error) => {
            this.loadingExecutions = false
            api.handleError(error)
          })
      },

      onPageChange (pageNumber) {
        this.executionsPaging.page = pageNumber - 1
        this.recomputeQueryString()
        this.loadExecutions()
      },

      onPageSizeChange (pageSize) {
        this.executionsPaging.page = 0
        this.executionsPaging.size = pageSize
        this.recomputeQueryString()
        this.loadExecutions()
      },

      recomputeQueryString () {
        let query = {}
        if (this.executionsPaging.page > 0) {
          query['page'] = this.executionsPaging.page + 1
        }
        if (this.executionsPaging.size !== DEFAULT_PAGE_SIZE) {
          query['pageSize'] = this.executionsPaging.size
        }
        this.$router.replace({ query })
      },

      fromQueryString () {
        let query = this.$route.query
        if (query) {
          if (query['page']) {
            this.executionsPaging.page = parseInt(query['page'], 10) - 1
          }
          if (query['pageSize']) {
            this.executionsPaging.size = parseInt(query['pageSize'], 10)
          }
        }
      }
    },

    mounted () {
      this.fromQueryString()
      this.loadExecutions()
    },

    watch: {
      '$route' () {
        this.fromQueryString()
        this.loadExecutions()
      }
    }
  }
</script>

<style scoped>
  tr.discarded td {
    color: lightgray;
  }
  tr.running td {
    color: lightgray;
  }
</style>
