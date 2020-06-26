<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (C) 2019 by the ARA Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ 	 http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
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
              :data-nrt=" $route.name + '_CartHeader_' + qualitySeverity.severity.code + '_' + execution.id "
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

