<template>
  <div
      v-if="execution.qualitySeverities && execution.qualitySeverities.length && execution.qualityThresholds && Object.keys(execution.qualityThresholds).length">
    <!-- Grid header 1: severity names -->
    <Row type="flex" :gutter="4" justify="space-around">
      <i-col span="4"/>
      <i-col :span="(24 - 4) / execution.qualitySeverities.length"
             v-for="qualitySeverity of execution.qualitySeverities" :key="qualitySeverity.severity.code"
             style="text-align: center;">
        <strong>{{qualitySeverity.severity.shortName}}</strong>
      </i-col>
    </Row>

    <!-- Grid header 2: quality percentages -->
    <Row type="flex" :gutter="4" justify="space-around">
      <i-col span="4"/>
      <i-col :span="(24 - 4) / execution.qualitySeverities.length"
             v-for="qualitySeverity of execution.qualitySeverities" :key="qualitySeverity.severity.code"
             style="margin-bottom: 4px;">
        <router-link :to="routerLink(qualitySeverity.severity.code, qualitySeverity.scenarioCounts)"
                     :replace="routerReplace" :target="isFramed ? '_blank' : ''">
          <nrt-severity-total
              :qualitySeverity="qualitySeverity"
              :thresholds="execution.qualityThresholds ? execution.qualityThresholds[qualitySeverity.severity.code] : {}"
              :blockingValidation="execution.blockingValidation"
              :allRun="execution.qualityStatus !== 'INCOMPLETE'"
              :isRunning="isRunning"/>
        </router-link>
      </i-col>
    </Row>
    <!-- Run information for all country & run type couples -->
    <div v-for="run of execution.runs" :key="run.id">
      <nrt-run :execution="execution" :run="run" :routerReplace="routerReplace"/>
    </div>
  </div>
  <div v-else style="color: #ED3F14;">
    NO DATA
    <job-status-result :job="execution"/>
    <a :href="execution.jobUrl + 'console'" target="_blank">
      <Button icon="md-open" size="small" style="margin-left: 8px;">SHOW LOGS</Button>
    </a>
  </div>
</template>

<script>
  import jobStatusResultComponent from './job-status-result'
  import nrtSeverityTotalComponent from './nrt-severity-total'
  import nrtRunComponent from './nrt-run'

  export default {
    name: 'nrt-cycle-quality',

    computed: {
      isFramed () {
        return this.$route.matched.some(record => record.meta.isFramed)
      }
    },

    methods: {
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
      }
    },

    components: {
      'nrt-severity-total': nrtSeverityTotalComponent,
      'job-status-result': jobStatusResultComponent,
      'nrt-run': nrtRunComponent
    },

    props: ['execution', 'isRunning', 'routerReplace']
  }
</script>

