<template>
  <div
       :class="'tdStyle ' + (isGlobal || !blockingValidation || isRunning ? 'globalStyle' : isFailed || !allRun ? 'failedStyle' : 'successStyle')"
       :title="!isGlobal && !blockingValidation ? 'For information only: not blocking' : ''">
    <div v-if="allRun" class="percentStyle">{{counts && counts.total > 0 ? qualitySeverity.percent + '&nbsp;%' : '-'}}</div>
    <span v-if="!allRun" :class="'smallStyle ' + (blockingValidation && !isGlobal && !isRunning ? 'failedSmallStyle' : '')">INCOMPLETE</span>
    <span v-else :class="'smallStyle ' + (!isGlobal && isFailed && blockingValidation ? 'failedSmallStyle' : '')">
      <span class="okStyle">{{counts ? counts.passed : 0}} OK</span>
      <span class="koStyle">{{counts ? counts.failed : 0}} KO</span>
      <span v-if="!isGlobal && blockingValidation">
        <span :class="'thresholdStyle ' + (highlightWarning ? 'warningStyle' : '')"
              :title="(highlightWarning && !isFailed ? 'WARNING: QUALITY IS NEAR THE MINIMUM QUALITY THRESHOLD! ' : '') + 'Build is not eligible when less than ' + thresholds.failure + ' % of these scenarios pass'">
          <Icon type="md-hand"></Icon> {{thresholds.failure}} %
        </span>
      </span>
    </span>
  </div>
</template>

<script>
  export default {
    props: [ 'qualitySeverity', 'thresholds', 'blockingValidation', 'allRun', 'isRunning' ],

    computed: {
      counts () {
        return this.qualitySeverity.scenarioCounts
      },

      isFailed () {
        return this.qualitySeverity.status === 'FAILED'
      },

      highlightWarning () {
        return this.qualitySeverity.status === 'WARNING' || this.isFailed
      },

      isGlobal () {
        // TODO Server should output it (transient...)
        return this.qualitySeverity.severity.code === '*'
      }
    }
  }
</script>

<style lang="less" scoped>
  .tableStyle { border-spacing: 4px; width: 100%; }
  .tdStyle { border-radius: 5px; line-height: 1em; text-align: center; vertical-align: top; padding: 8px; }
  .tdStyle:hover { background-color: #ebF7ff !important; }
  .severityStyle { font-weight: bold; font-size: 14px; padding-top: 0; padding-bottom: 0; }
  .successStyle { background-color: #F5F7F9; color: #19be6b; }
  .failedStyle { background-color: #ed3f14; color: white; }
  .failedStyle:hover { background-color: #FF3100 !important; }
  .globalStyle { color: black; }
  .percentStyle { font-size: 22px; line-height: 22px; padding-bottom: 4px; font-weight: bold; }
  .smallStyle { font-size: 10px; color: gray; }
  .warningStyle { background-color: #ffcc30; padding: 2px 4px; margin: -2px 0 !important; color: black; border-radius: 4px; }
  .failedSmallStyle { color: white; }
  .okStyle        { display: inline-block; margin: 0 2px; }
  .koStyle        { display: inline-block; margin: 0 2px; }
  .thresholdStyle { display: inline-block; margin: 0 2px; }
</style>
