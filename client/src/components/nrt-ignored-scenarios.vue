<template>
  <div v-if="ignoredScenarios">
    <div v-if="!loadingIgnoredScenarios && !severityTotals.severities.length">
      No ignored scenario: keep up the good work!
    </div>
    <div style="float: left; width: 500px; margin-bottom: 16px;">
      <!-- Top header row: severity names + global percentages -->
      <Row type="flex" :gutter="4" justify="space-around">
        <i-col span="4" />
        <i-col :span="(24 - 4) / severityTotals.severities.length" v-for="globalSeverityStat of severityTotals.severities" :key="globalSeverityStat.severity.code" style="text-align: center;">
          <strong>{{globalSeverityStat.severity.shortName}}</strong>
          <div class="tdStyle globalStyle percentStyle" v-if="globalSeverityStat.counts.ignored > 0">
            {{globalSeverityStat.counts.percent}}%<br>
            <span style="font-size: 75%;">{{globalSeverityStat.counts.ignored}}/{{globalSeverityStat.counts.total}}</span>
          </div>
        </i-col>
      </Row>

      <!-- Each row is a source (excluding Global), each cell a severity (last one is Global) -->
      <div v-for="sourceStat of ignoredScenarios" v-if="sourceStat.source.code !== '*'" :key="sourceStat.source.code">
        <Row type="flex" :gutter="4" justify="space-around">
          <i-col span="4" style="padding-top: 6px; text-align: right; line-height: 42px;">
            <strong>{{sourceStat.source.name}}</strong>
          </i-col>

          <i-col :span="(24 - 4) / severityTotals.severities.length" v-for="globalSeverityStat of severityTotals.severities" :key="globalSeverityStat.severity.code" style="text-align: center;">
            <div v-for="severityStat of sourceStat.severities" v-if="severityStat.severity.code === globalSeverityStat.severity.code" :key="severityStat.severity.code" @click="showIgnoredScenarios(sourceStat.source.code, severityStat.severity)">
              <Alert type="info" :class="'clickablePercent' + (severityStat.counts.ignored > 0 ? ' withIgnore' : '') + (sourceStat.source.code === activeSourceCode && severityStat.severity.code === activeSeverityCode ? ' active' : '')">
                <span v-if="severityStat.counts.ignored < 0" style="color: rgb(237, 63, 20)">
                  ?
                </span>
                <span v-else-if="severityStat.counts.ignored > 0">
                  {{severityStat.counts.percent}}%<br>
                  <span style="font-size: 75%;">{{severityStat.counts.ignored}}/{{severityStat.counts.total}}</span>
                </span>
                <span v-else>
                  &nbsp;
                </span>
              </Alert>
            </div>
          </i-col>
        </Row>
      </div>
    </div>

    <!-- The scenarios matching the clicked filter box -->
    <div style="border-left: 1px solid #e9eaec; margin-left: 508px; padding-left: 8px;">
      <div v-if="featuresWithIgnoredScenarios">
        <h2 style="margin-top: 0;">{{detailsTitle}}</h2>
        <div v-for="feature in featuresWithIgnoredScenarios" :key="feature.reportFileName">
          <strong style="display: block; margin-top: 6px;">
            {{feature.name}}
            <span style="color: lightgray">({{feature.scenarios.length}})</span>
            <a :href="editUrl(feature)" target="_blank" style="margin-left: 8px; font-weight: normal;">
              <Icon type="md-open"/> EDIT SCENARIOS
            </a>
          </strong>
          <div v-for="(scenario, index) in feature.scenarios" :key="index" class="scenarioName">
            {{scenario.name}}
            <span class="severityStyle">{{scenario.severity}}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Spin v-else style="width: 20px; margin: 3px auto 0 auto;"/>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  export default {
    data () {
      return {
        loadingIgnoredScenarios: false,
        ignoredScenarios: null,

        detailsTitle: null,
        featuresWithIgnoredScenarios: null,
        activeSourceCode: null,
        activeSeverityCode: null
      }
    },

    computed: {
      severityTotals () {
        return this.getSourceStat('*')
      }
    },

    methods: {
      loadIgnoredScenarios () {
        this.loadingIgnoredScenarios = true
        Vue.http
          .get(api.paths.scenarios(this) + '/ignored', api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingIgnoredScenarios = false
            this.ignoredScenarios = response.body
          }, (error) => {
            this.loadingIgnoredScenarios = false
            api.handleError(error)
          })
      },

      getSourceStat (sourceCode) {
        for (let sourceStat of this.ignoredScenarios) {
          if (sourceStat.source.code === sourceCode) {
            return sourceStat
          }
        }
        return null
      },

      showIgnoredScenarios (sourceCode, severity) {
        let filteredFeatures = []
        let sourceStat = this.getSourceStat(sourceCode)
        for (let severityStat of sourceStat.severities) {
          if ((severity.code === '*' || severityStat.severity.code === severity.code) && severityStat.features) {
            for (let featureStat of severityStat.features) {
              let feature = filteredFeatures.find(e => e.file === featureStat.file)
              if (!feature) {
                feature = {
                  file: featureStat.file,
                  name: featureStat.name,
                  source: sourceStat.source,
                  scenarios: []
                }
                filteredFeatures.push(feature)
              }
              for (let scenarioName of featureStat.scenarios) {
                feature.scenarios.push({
                  name: scenarioName,
                  severity: severityStat.severity.name
                })
              }
            }
          }
        }
        if (filteredFeatures.length) {
          this.sortFeatures(filteredFeatures)
          this.featuresWithIgnoredScenarios = filteredFeatures
          this.detailsTitle = sourceStat.source.name + (severity.code === '*' ? '' : ' - ' + severity.name)
          this.activeSourceCode = sourceCode
          this.activeSeverityCode = severity.code
        }
      },

      sortFeatures (features) {
        features.sort((a, b) => a.name.localeCompare(b.name))
        for (let feature of features) {
          feature.scenarios.sort((a, b) => a.name.localeCompare(b.name))
        }
      },

      editUrl (feature) {
        let source = feature.source
        let folderUrl = source.vcsUrl.replace('{{branch}}', source.defaultBranch)
        return folderUrl + feature.file
      }
    },

    mounted () {
      this.loadIgnoredScenarios()
    },

    watch: {
      '$route' () {
        this.loadIgnoredScenarios()
      }
    }
  }
</script>

<style scoped>
  .clickablePercent {
    margin-bottom: 4px;
    padding-right: 16px;
    background: none;
    text-align: center;
    font-size: 150%;
    border: 1px solid transparent;
  }
  .clickablePercent.withIgnore {
    border: 1px solid #E3E8EE;
    background-color: white;
  }
  .clickablePercent.withIgnore:not(.active) {
    cursor: pointer;
  }
  .clickablePercent.withIgnore.active {
    background-color: #2D8CF0;
    color: white;
  }
  .clickablePercent.withIgnore:not(.active):hover {
    background-color: #EBF7FF;
  }

  /* Duplicated from nrt-severity-total.vue */
  .tdStyle { border-radius: 5px; line-height: 1em; text-align: center; vertical-align: top; padding: 8px; }
  .globalStyle { color: black; }
  .percentStyle { font-size: 22px; line-height: 22px; padding-bottom: 4px; font-weight: bold; }
  .smallStyle { font-size: 10px; color: gray; }

  .scenarioName {
    margin: 2px 0;
  }
  .severityStyle {
    padding-top: 1px;
    padding-bottom: 1px;
  }
</style>
