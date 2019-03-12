<template>
  <div v-if="execution">
    <!-- Title -->
    <h2 style="padding: 0 17px; margin-top: 0;">
      <span style="float: right;">{{execution.release}}</span>
      <strong>{{execution.branch.toUpperCase()}}:</strong> {{execution.name}}
    </h2>

    <!-- Card -->
    <Card :bordered="true" dis-hover style="height: 100%; margin-bottom: 8px; padding: 0px;">
      <!-- Dates... -->
      <div class="versions">
        <Row type="flex" :gutter="4" justify="space-around">
          <i-col span="5">
            <Button-group v-if="!hideButtons" shape="circle" style="float: left; margin-right: 8px;">
              <Button
                  :disabled="!execution.previousId"
                  @click="emitRequestExecution(execution.previousId)"
                  icon="md-skip-backward"
                  title="Previous execution"
                  type="primary"/>
              <Button
                  :disabled="!execution.nextId"
                  @click="emitRequestExecution(execution.nextId)"
                  icon="md-skip-forward"
                  title="Next execution"
                  type="primary"/>
            </Button-group>

            {{isRunning ? 'started' : 'tested'}} {{testDateAgo}}<br>
            <span class="small-details">{{testDate}}</span>
          </i-col>
          <i-col span="19" style="text-align: right;">
            <div style="float: right; margin-left: 8px;">
              <execution-actions-button :execution="execution" v-on:change="emitRequestExecution(execution.id)"/>
            </div>
            <span v-if="execution.buildDateTime">version from {{buildDateAgo}}<br></span>
            <span class="small-details"><em style="margin-right: 8px;">{{execution.version}}</em> {{buildDate}}</span>
          </i-col>
        </Row>
      </div>

      <!-- Progress-bar during build + deploy + test -->
      <div class="test-progress" v-if="isRunning" style="line-height: 1em !important; margin: 12px 0;">
        <Alert type="info" style="margin-bottom: 0; text-transform: uppercase; padding-right: 16px !important;">
          <nrt-progress-bar :startDateTime="execution.testDateTime" :estimatedDuration="execution.estimatedDuration">
            <span>
              Running...
              <Button icon="md-refresh" @click="emitRequestExecution(execution.id)" size="small"
                      style="margin-left: 8px;">REFRESH</Button>
            </span>
          </nrt-progress-bar>
        </Alert>
      </div>

      <!-- Discard reason -->
      <div v-if="execution.discardReason" style="text-align: center; margin-bottom: 8px;">
        <span class="discardReason">Discarded: {{execution.discardReason}}</span>
      </div>
      <Tabs v-if="problems" value="quality" style="margin-right: -16px; margin-left: -16px;">
        <TabPane :label="qualityTabLabel" name="quality">
          <nrt-cycle-quality :execution="execution" :routerReplace="routerReplace" @isRunning="isRunning"/>
        </TabPane>
        <TabPane :label="problemsTabLabel" name="problems">
          <nrt-cycle-problems style="position: absolute;"
                              :problems="problems"
                              :problemTotals="problemTotals"
                              :activeProblemId="activeProblemId"
                              :filteredTeamId="filteredTeamId"
                              v-on:dispatch-selected-problem="dispatchSelectedProblem"/>
        </TabPane>
      </Tabs>
      <nrt-cycle-quality v-else :execution="execution" @isRunning="isRunning"/>
      <Spin fix v-if="execution.loading"/>
    </Card>
  </div>
</template>

<script>
  import moment from 'moment'
  import util from '../libs/util'

  import executionActionsButtonComponent from '../components/execution-actions-button'
  import nrtSeverityTotalComponent from './nrt-severity-total'
  import nrtRunComponent from './nrt-run'
  import jobStatusResultComponent from './job-status-result'
  import nrtCycleQuality from '../components/nrt-cycle-quality'
  import nrtCycleProblems from '../components/nrt-cycle-problems'
  import nrtProgressBar from './nrt-progress-bar'

  export default {
    props: ['execution', 'hideButtons', 'routerReplace', 'problems', 'problemTotals', 'filteredTeamId', 'activeProblemId'],

    components: {
      'execution-actions-button': executionActionsButtonComponent,
      'nrt-severity-total': nrtSeverityTotalComponent,
      'nrt-run': nrtRunComponent,
      'job-status-result': jobStatusResultComponent,
      'nrt-cycle-quality': nrtCycleQuality,
      'nrt-cycle-problems': nrtCycleProblems,
      'nrt-progress-bar': nrtProgressBar
    },

    computed: {
      isRunning () {
        return this.execution.status === 'PENDING' || this.execution.status === 'RUNNING'
      },
      buildDate () {
        return this.execution.buildDateTime ? util.formatDate(this.execution.buildDateTime) : ''
      },
      buildDateAgo () {
        return this.execution.buildDateTime ? moment(this.execution.buildDateTime).fromNow() : ''
      },
      testDate () {
        return this.execution.testDateTime ? util.formatDate(this.execution.testDateTime) : ''
      },
      testDateAgo () {
        return this.execution.testDateTime ? moment(this.execution.testDateTime).fromNow() : ''
      },

      qualityTabLabel () {
        let status = this.execution.qualityStatus
        if (status !== 'INCOMPLETE' && this.execution.qualitySeverities) {
          let point = this.execution.qualitySeverities.find(severity => severity.severity.code === '*')
          if (point) {
            status = point.percent + ' %'
          }
        }
        return 'QUALITY (' + status + ')'
      },

      problemsTabLabel () {
        return (h) => {
          let children = null
          if (this.activeProblemId) {
            let icon = h('Icon', {
              props: {
                type: 'md-backspace'
              }
            })
            children = h('Button', {
              props: {
                type: 'default',
                size: 'small',
                slot: 'extra'
              },
              attrs: {
                style: 'display: inline-block; margin-left: 8px; padding: 0 3px;',
                title: 'Remove this filter'
              },
              on: {
                click: this.removeFilterProblem
              }
            }, [ icon, 'REMOVE' ])
          }
          return h('div', ['PROBLEMS (' + this.problems.length + ')', children])
        }
      }
    },

    methods: {
      removeFilterProblem () {
        this.$emit('dispatch-selected-problem', null)
      },

      dispatchSelectedProblem (problemId) {
        this.$emit('dispatch-selected-problem', problemId)
      },

      routerLink (severityCode, counts) {
        return {
          name: 'execution',
          params: {
            id: this.execution.id
          },
          query: {
            severity: (severityCode === '*' ? undefined : severityCode),
            withSucceed: (counts.passed === counts.total ? 'true' : undefined)
          }
        }
      },

      emitRequestExecution (executionId) {
        this.$emit('requestExecution', executionId)
      }
    }
  }
</script>

<style lang="less" scoped>
  .versions {
    font-size: 12px;
    text-transform: uppercase;
    margin-bottom: 16px;
  }

  .versions .small-details {
    font-size: 10px;
    color: #9ea7b4;
  }

  .ivu-tabs-nav-wrap {
    margin: 0 -16px;
  }
  .ivu-tabs-tabpane {
    padding: 0 16px;
  }

  /* Duplicated from nrt-severity-total.vue */
  .tdStyle {
    border-radius: 5px;
    line-height: 1em;
    text-align: center;
    vertical-align: top;
    padding: 8px;
  }

  .globalStyle {
    color: black;
  }

  .percentStyle {
    font-size: 22px;
    line-height: 22px;
    padding-bottom: 4px;
    font-weight: bold;
  }

  .smallStyle {
    font-size: 10px;
    color: gray;
  }
</style>

<style lang="less">
  .ivu-tabs {
    margin-bottom: -16px;
  }

  .ivu-tabs-bar {
    margin-bottom: 0;
  }

  .ivu-tabs-nav-scroll {
    text-align: center;
  }

  .ivu-tabs-nav {
    float: none !important;
    display: inline-block;
    margin: auto 8px;
  }

  .ivu-tabs-tab .ivu-btn .ivu-icon {
    margin-right: 2px;
  }

  .ivu-tabs-tab .ivu-btn {
    border-color: transparent;
  }

  .ivu-tabs-tab .ivu-btn:hover {
    border-color: #57A3F3;
  }

  .ivu-tabs-tabpane {
    padding: 0;
  }

  .ivu-tabs-tabpane > div {
    padding: 16px 0 16px 0;
  }
</style>
