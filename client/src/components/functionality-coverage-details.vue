<template>
  <div>
    <div v-for="source in tree" :key="source.code">
      <h1 class="source">{{source.name}}</h1>
      <div v-for="feature in source.features" :key="feature.name">
        <h2 class="feature">
          <span class="tags">{{feature.tags}}</span>
          {{feature.name}}
        </h2>
        <div v-for="scenario in feature.scenarios" :key="scenario.id">
          <div :class="'scenario' + (scenario.ignored ? ' ignored' : '')">
            <span class="tags">{{scenario.tags}}</span>
            <strong>{{scenario.name}}</strong>
            <span :class="'severityStyle' + (!scenario.severity || scenario.severity === '_' ? ' none' : '')">{{prettySeverity(scenario.severity)}}</span>
            <a :href="editUrl(scenario, source)" target="_blank" style="margin-left: 8px;">
              <Icon type="md-open"/> EDIT SCENARIO
            </a>
            <pre><code :style="'display: table; min-width: 100%; padding: 0;'" v-html="scenarioUtil.formattedScenario(scenario, null, true)"></code></pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import scenarioUtil from '../libs/scenario-util'
  import util from '../libs/util'

  export default {
    name: 'functionality-coverage-details',

    mixins: [{
      created () {
        this.scenarioUtil = scenarioUtil
      }
    }],

    data () {
      return {
        node: null,
        scenarios: [],
        loadingScenarios: false
      }
    },

    computed: {
      tree () {
        let tree = []

        let lastSource = null
        let lastFeature = null

        for (let i in this.scenarios) {
          let scenario = this.scenarios[i]

          if (!lastSource || lastSource.code !== scenario.source.code) {
            lastSource = {
              ...scenario.source,
              features: []
            }
            tree.push(lastSource)
            lastFeature = null
          }

          if (!lastFeature || lastFeature.file !== scenario.featureFile) {
            lastFeature = {
              file: scenario.featureFile,
              name: scenario.featureName,
              tags: scenario.featureTags,
              scenarios: []
            }
            lastSource.features.push(lastFeature)
          }

          lastFeature.scenarios.push(scenario)
        }

        return tree
      }
    },

    methods: {
      prettySeverity (severityCode) {
        return util.prettySeverity(severityCode, this)
      },

      load (node) {
        this.node = node
        this.loadingScenarios = true
        Vue.http
          .get(api.paths.functionalities(this) + '/' + node.id + '/scenarios', api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingScenarios = false
            this.scenarios = response.body
          }, (error) => {
            this.loadingScenarios = false
            api.handleError(error)
          })
      },

      editUrl (scenario, source) {
        let featureFile = scenario.featureFile
        let folderUrl = source.vcsUrl.replace('{{branch}}', source.defaultBranch)
        return folderUrl + featureFile
      }
    },

    mounted () {
      this.$store.dispatch('severities/ensureSeveritiesLoaded', this)
    }
  }
</script>

<style scoped>
  .source {
    margin: 0;
    border-bottom: 1px solid #E9EAEC;
  }

  .feature {
    margin: 16px 0 8px 0;
  }

  .scenario {
    margin: 8px 0 4px 0;
  }
  .scenario.ignored,
  .scenario.ignored .tags {
    color: #bbb;
  }

  .tags {
    display: block;
    font-size: 11px;
    font-weight: normal;
    color: gray;
  }
</style>
