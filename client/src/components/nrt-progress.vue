<template>
  <router-link :to="routerLink" :replace="routerReplace" v-if="counts && counts.total > 0" :target="isFramed ? '_blank' : ''">
    <Alert
      :title="tooltip"
      type="info"
      :style="(small ? '' : 'border: 1px solid #E3E8EE;') + (!run || run.includeInThresholds ? '' : 'opacity: 0.5;')">

      <div v-if="!small" style="min-height: 16px;">
        <span class="textPassed" v-if="counts.passed > 0">{{counts.passed}}</span>
        <span class="textFailed">
          <span class="textProblem" v-if="counts.handled > 0">
            {{counts.handled}}
            <span v-if="counts.unhandled > 0"> + </span>
          </span>
          <span v-if="counts.unhandled > 0">{{counts.unhandled}}</span>
        </span>
      </div>

      <div :class="'progressBar' + (counts.total > 0 ? ' failed' : '') + (small ? ' small' : '')">
        <div :style="'width: ' + (100 * counts.passed / counts.total) + '%'"></div>
        <div :style="'width: ' + (100 * counts.handled / counts.total) + '%;'"></div>
      </div>
    </Alert>
  </router-link>
</template>

<script>
  export default {
    props: [ 'execution', 'run', 'counts', 'severityCode', 'teamId', 'small', 'routerReplace' ],

    computed: {
      isFramed () {
        return this.$route.matched.some(record => record.meta.isFramed)
      },

      routerLink () {
        return {
          name: 'execution',
          params: {
            id: this.execution.id
          },
          query: {
            type: (this.run ? this.run.type.code : undefined),
            country: (this.run ? this.run.country.code : undefined),
            severity: (this.severityCode === '*' ? undefined : this.severityCode),
            team: this.teamId,
            withSucceed: (this.counts.passed === this.counts.total ? 'true' : undefined)
          }
        }
      },

      tooltip () {
        let tooltip = []
        let count

        count = this.counts.passed
        if (count > 0) {
          tooltip.push(count + ' succeed scenario' + (count === 1 ? '' : 's'))
        }

        count = this.counts.handled
        if (count > 0) {
          tooltip.push(count + ' handled failed scenario' + (count === 1 ? '' : 's') + ' with associated and not reappearing problem(s)')
        }

        count = this.counts.unhandled
        if (count > 0) {
          tooltip.push(count + ' unhandled failed scenario' + (count === 1 ? '' : 's') + ' without associated problems or with only reappearing problems')
        }

        count = this.counts.total
        if (count > 0) {
          let allFailed = this.counts.handled + this.counts.unhandled
          let failedScenarios = (allFailed > 0 ? allFailed + ' failed scenario' + (allFailed === 1 ? '' : 's') + ' / ' : '')
          tooltip.push('')
          tooltip.push('Total: ' + failedScenarios + count + ' scenario' + (count === 1 ? '' : 's'))
        }

        return tooltip.join('\n')
      }
    }
  }
</script>

<style lang="less" scoped>
  .ivu-alert { margin-bottom: 4px; background: none; padding: 8px 16px; border: 1px solid transparent; }
  .ivu-alert:hover { background-color: #ebF7ff !important; }
  .textPassed { color: #19be6b/*light green*/; float: left; }
  .textFailed { color: #ed3f14/*dark red*/; float: right; font-weight: bold; }
  .textProblem { color: #ffcc30; font-weight: bold;  }
  .progressBar { clear: both; border-radius: 3px; }
  .progressBar, .progressBar div { height: 5px; }
  .progressBar.small, .progressBar.small div { height: 3px; }
  .progressBar.failed { background-color: #ed3f14/*dark red*/; }
  .progressBar div { background-color: #19be6b/*light green*/; float: left; }
  .progressBar div + div { background-color: #ffcc30; }
</style>
