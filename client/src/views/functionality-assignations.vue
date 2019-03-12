<template>
  <div>
    <functionality-menu />

    <Spin fix v-if="loadingScenarios" />
    <div v-else>
      <Collapse v-model="collapse" accordion style="margin-top: 16px;">
        <Panel name="help">
          <Icon type="md-help-circle"/> HOW TO ASSIGN FUNCTIONALITIES TO SCENARIOS?
          <div slot="content">
            <p>
              For a functionality to be covered, it needs to have at least one scenario assigned to it.<br>
              You need to edit the title of scenarios and add the identifier of the functionality in it.<br>
              A functionality can have several scenarios and a scenario can have several functionalities.
            </p>
            <p>
              For instance, the first scenario is assigned to the functionality with identifier <Icon type="md-document"/> 1866,
              and the second functionality covers four functionalities
              (<Icon type="md-document"/> 1866, <Icon type="md-document"/> 1870 and <Icon type="md-document"/> 1971):<br>
              <img src="../assets/help/assign-functionalities-0.png" width="706" height="370">
            </p>
            <p>
              Once the file is modified, the merge request accepted and the build completed on branch "develop", the assignation will be automatically made, like this (one covering scenario and one ignored scenario in this example):<br>
              <img src="../assets/help/assign-functionalities-1.png" width="1084" height="649">
            </p>
            <p>
              For more information on how to edit the scenarios and create the merge request, expand the "WHERE TO EDIT OR ADD A SCENARIO?" help block on
              <router-link :to="{ name: 'scenario-writing-helps' }">SCENARIO-WRITING HELPS</router-link>
            </p>
          </div>
        </Panel>
      </Collapse>

      <h2>SCENARIOS WITH NO OR WRONG ASSIGNATIONS ({{scenarios ? scenarios.length : 'error while loading scenarios'}})</h2>

      <p>
        All scenarios must have one or more functionality identifiers in their titles, one or more country codes, and one severity.<br>
        Click a feature name to edit its file and assign functionalities, country-codes or severity-codes to the scenarios having none.<br>
        Expand the help section above for more information on how to proceed.<br>
        If a scenario listed here appears to have functionalities, make sure there is no typo or the functionality ID really exists.<br>
        Same goes for country and severity codes.
      </p>

      <Table border :columns="columns" :data="scenarios" :no-data-text="loadingScenariosFailed ? 'Loading failed.' : 'None. Everything is fine!'" />
    </div>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  import functionalityMenuComponent from '../components/functionality-menu'

  export default {
    name: 'functionality-assignations',

    components: {
      'functionality-menu': functionalityMenuComponent
    },

    data () {
      return {
        collapse: null,
        loadingScenarios: true,
        loadingScenariosFailed: false,
        scenarios: [],
        columns: [
          {
            title: 'Type',
            width: 100,
            render: (h, params) => {
              return h('span', params.row.source.name)
            }
          },
          {
            title: 'Feature',
            key: 'featureName',
            width: 400,
            render: (h, params) => {
              let source = params.row.source
              let featureFile = params.row.featureFile
              let folderUrl = source.vcsUrl.replace('{{branch}}', source.defaultBranch)
              let url = folderUrl + featureFile

              return h(
                'a',
                {
                  attrs: {
                    href: url,
                    target: 'blank'
                  },
                  style: {
                    display: 'block'
                  }
                },
                [
                  h('Icon', { attrs: { type: 'md-open' } }),
                  h('span', ' ' + params.row['featureName'])
                ]
              )
            }
          },
          {
            title: 'Scenario',
            key: 'name'
          },
          {
            title: 'Wrong Functionality Identifiers',
            key: 'wrongFunctionalityIds',
            width: 230,
            render: (h, params) => {
              let wrongs = params.row.wrongFunctionalityIds
              let none = !wrongs && !params.row.functionalityCount
              return h('span', { class: { none, wrongs } },
                (none ? 'none' : '') +
                (wrongs || ''))
            }
          },
          {
            title: 'Wrong Country Codes',
            key: 'wrongCountryCodes',
            width: 180,
            render: (h, params) => {
              let wrongs = params.row.wrongCountryCodes
              let none = !wrongs && !params.row.hasCountryCodes
              return h('span', { class: { none, wrongs } },
                (none ? 'none' : '') +
                (wrongs || ''))
            }
          },
          {
            title: 'Wrong Severity Code',
            key: 'wrongSeverityCode',
            width: 180,
            render: (h, params) => {
              let wrongs = params.row.wrongSeverityCode
              let none = !wrongs && !params.row.hasSeverity
              return h('span', { class: { none, wrongs } },
                (none ? 'none' : '') +
                (wrongs || ''))
            }
          }
        ]
      }
    },

    methods: {
      loadAssignations () {
        this.loadingScenarios = true
        Vue.http
          .get(api.paths.scenarios(this) + '/without-functionalities', api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingScenarios = false
            this.loadingScenariosFailed = false
            this.scenarios = response.body
          }, (error) => {
            this.loadingScenarios = false
            this.loadingScenariosFailed = true
            api.handleError(error)
          })
      }
    },

    mounted () {
      this.loadAssignations()
    },

    watch: {
      '$route' () {
        this.loadAssignations()
      }
    }
  }
</script>

<style scoped>
  p {
    margin-bottom: 8px;
  }
  img {
    box-shadow: 0 0 8px lightgray;
  }
  >>> .none {
    background-color: #ED3F14;
    color: white;
    padding: 0 2px;
    border-radius: 3px;
  }
  >>> .wrongs {
    color: #ED3F14;
  }
</style>
