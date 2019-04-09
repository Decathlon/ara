<template>
  <div>
    <functionality-menu />

    <Spin fix v-if="loadingFunctionalities || countries.length === 0 || sources.length === 0 || teamsAssignableToFunctionalities.length === 0"/>
    <div v-else>
      <!-- Screen title with help -->
      <div style="display: flex;">
        <div class="balance">
        </div>

        <div style="flex: 1 0 auto;">
          <h1 style="text-align: center; margin-bottom: 16px;">
            <span v-if="counts.total === 0">No functionality</span>
            <span v-else-if="counts.matching === counts.total && counts.total === 1">Showing the only functionality</span>
            <span v-else-if="counts.matching === counts.total">Showing all <strong>{{counts.total}}</strong> functionalities</span>
            <span v-else>Showing <strong>{{counts.matching}}</strong> filtered functionalit{{counts.matching == 1 ? 'y' : 'ies'}} out of the <strong>{{counts.total}}</strong> functionalit{{counts.total == 1 ? 'y' : 'ies'}}</span>
          </h1>
        </div>

        <div class="balance" style="text-align: right;">
          <a href="https://github.com/decathlon/ara/blob/master/doc/user/functionalities/CreateAndMoveFunctionalities.adoc"
             target="_blank" id="helpLink">
            How to create &amp; move functionalities
            <Icon type="md-help-circle" size="16" style="padding: 0; vertical-align: -3px;"/>
          </a>
        </div>
      </div>

      <!-- Table header -->
      <div class="headerRow">

        <div class="headerCell" :style="'width: calc(100% - ' + sumColumnSizes + 'px);'">
          Functionality<br>
          <ButtonGroup size="small" style="margin-right: 3px;"><!--
        --><Button @click="collapseAll()" title="COLLAPSE ALL">
              <Icon type="md-contract"/>
            </Button><!--
        --><Button @click="expandAll()" title="EXPAND ALL">
              <Icon type="md-expand"/>
            </Button><!--
        --></ButtonGroup><!--
      --><Input placeholder="Identifier" title="Functionality identifier" size="small" icon="md-document" v-model="filter.id" @on-change="filterFunctionalities" style="margin-right: 3px; width: 95px;"/><!--
      --><Input placeholder="Name" size="small" icon="ios-funnel" v-model="filter.functionality" @on-change="filterFunctionalities" style="width: calc(100% - 2 * 26px - 95px - 3 * 3px);"/>
        </div>

        <div class="headerCell" :style="'width: ' + (columnSizes[1]) + 'px;'">
          Team<br>
          <Select size="small" v-model="filter.team" filterable placeholder="All" @on-change="filterFunctionalities">
            <Option value="" :label="'\u00A0'" />
            <Option v-for="team in teamsAssignableToFunctionalities" :value="team.id.toString()" :key="team.id" :label="team.name" />
          </Select>
        </div>

        <div class="headerCell" :style="'width: ' + (columnSizes[2]) + 'px;'">
          Severity
          <div style="text-align: center;">
            <ButtonGroup>
              <Button
                v-for="severity in severities"
                :key="severity"
                :type="(filter.severity === severity ? 'primary' : 'default')"
                size="small"
                style="padding-left: 3px; padding-right: 3px;"
                :title="severity.toLowerCase() | capitalize"
                @click="toggleSeverity(severity)">{{severity.charAt(0).toUpperCase()}}</Button>
            </ButtonGroup>
          </div>
        </div>

        <div class="headerCell" :style="'width: ' + (columnSizes[3]) + 'px;'">
          Created
          <Input placeholder="All" size="small" icon="ios-funnel" v-model="filter.created" @on-change="filterFunctionalities" title="TIP: type 'none' to show features without creation"/>
        </div>

        <div class="headerCell" :style="'width: ' + (columnSizes[4]) + 'px;'">
          Countries
          <div style="text-align: center;">
            <ButtonGroup>
              <Button
                v-for="country in countries"
                :key="country.code"
                :type="(hasCountryFilter(country.code) ? 'primary' : 'default')"
                size="small"
                style="padding-left: 2px; padding-right: 2px;"
                :title="country.name"
                @click="toggleCountry(country.code)">{{country.code.toUpperCase()}}</Button>
            </ButtonGroup>
          </div>
        </div>

        <div class="headerCell" :style="'width: ' + (columnSizes[5]) + 'px;'">
          Coverage by scenarios<br>
          <Select size="small" v-model="filter.coverage" filterable placeholder="All" @on-change="filterFunctionalities">
            <Option value="" :label="'\u00A0'" />
            <!-- TODO Service must expose a /api/constants to get such values -->
            <Option value="COVERED"              label="Covered (no ignored)"            title="Functionalities having active scenarios, none of them are ignored"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(1) + ';'"/> Covered (no ignored)</Option>
            <Option value="PARTIALLY_COVERED"    label="Partially covered (few ignored)" title="Functionalities having active scenarios as well as ignored scenarios"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(2) + ';'"/> Partially covered (few ignored)</Option>
            <Option value="IGNORED_COVERAGE"     label="Ignored coverage (all ignored)"  title="Functionalities having ignored scenarios and no active scenarios"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(3) + ';'"/> Ignored coverage (all ignored)</Option>
            <Option value="STARTED"              label="Started"                         title="Functionalities marked as 'Started' without any active nor ignored scenario"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(4) + ';'"/> Started</Option>
            <Option value="NOT_AUTOMATABLE"      label="Not automatable"                 title="Functionalities marked as 'Not automatable' without any active nor ignored scenario"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(5) + ';'"/> Not automatable</Option>
            <Option value="NOT_COVERED"          label="Not covered"                     title="Functionalities that are not started nor not-automatable and have no active nor ignored scenarios"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(6) + ';'"/> Not covered</Option>

            <Option value="COVERED_OR_PARTIALLY" label="Covered or partially"            title="Functionalities having at least one active scenario" style="border-top: 1px solid #DDDEE1;"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(1) + ';'"/><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(2) + ';'"/> Covered or partially</Option>
            <Option value="COVERED_OR_IGNORED"   label="Covered or ignored"              title="Functionalities having at least one active or ignored scenario"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(1) + ';'"/><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(2) + ';'"/><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(3) + ';'"/> Covered or ignored</Option>
            <Option value="WITH_IGNORES"         label="With ignores"                    title="Functionalities having at least one ignored scenario (but they also can have active scenarios)"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(2) + ';'"/><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(3) + ';'"/> With ignores</Option>
            <Option value="NO_COVERAGE_AT_ALL"   label="No coverage at all"              title="Functionalities having no active nor ignored scenarios, optionally marked as 'Started' or 'Not automatable'"><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(4) + ';'"/><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(5) + ';'"/><Icon type="md-square" :style="'color: ' + util.getFeatureCoverageColor(6) + ';'"/> No coverage at all</Option>

            <Option v-for="(source, sourceIndex) in sources" :key="sourceIndex" :value="'HAS_' + source.code" :label="'Has ' + source.name + ' scenarios'" :style="(sourceIndex === 0 ? 'border-top: 1px solid #DDDEE1;' : '')" />
          </Select>
        </div>

        <div class="headerCell" :style="'width: ' + (columnSizes[6]) + 'px;'">
          Comment
          <Input placeholder="All" size="small" icon="ios-funnel" v-model="filter.comment" @on-change="filterFunctionalities"/>
        </div>

        <div class="headerCell" :style="'width: ' + (columnSizes[7]) + 'px; text-align: center;'">
          Actions
          <Button size="small" :disabled="!hasFilter" @click="removeAllFilters()" title="Remove all filters">
            <Icon type="md-backspace"/>
          </Button>
        </div>

      </div>
      <virtual-scroller :items="flattenedMatchingFunctionalities" item-height="31" pool-size="1000" buffer="200" page-mode>
        <template slot-scope="props">
          <functionality-node
            :key="props.itemKey"
            :node="props.item"
            :columnSizes="columnSizes"
            :sumColumnSizes="sumColumnSizes"
            :hasFilter="hasFilter"
            :countries="countries"
            :teams="teamsAssignableToFunctionalities"
            :nodeToMove="nodeToMove"
            :destinationReferenceNodeId="destinationReferenceNodeId"
            :sources="sources"
            v-on:toggleExpand="toggleExpand"
            v-on:edit="startEditing"
            v-on:duplicate="startDuplicating"
            v-on:move="startMoving"
            v-on:completeMove="completeMove"
            v-on:cancelMove="cancelMove"
            v-on:create="create"
            v-on:delete="deleteNode"
            v-on:showCoverage="showCoverage"/>
        </template>
      </virtual-scroller>
      <div v-if="functionalities.length === 0" class="no-displayed-row">
        No data.
        <div>
          <ButtonGroup style="margin-top: 8px;">
            <Button type="primary" @click="create('FOLDER', null, 'LAST_CHILD')">
              <Icon type="md-add"/> <Icon type="md-folder"/>
              CREATE FOLDER
            </Button>
            <Button @click="create('FUNCTIONALITY', null, 'LAST_CHILD')">
              <Icon type="md-add"/> <Icon type="md-document"/>
              CREATE FUNCTIONALITY
            </Button>
          </ButtonGroup>
        </div>
      </div>
      <div v-else-if="flattenedMatchingFunctionalities.length === 0" class="no-displayed-row">
        Filters are too restrictive.
      </div>
    </div>

    <Modal v-model="showingEditDialog" @on-ok="onEditOk" @on-cancel="showingEditDialog = false"
           :title="(editingRow.id > 0 ? 'Edit' : 'Create') + ' ' + (editingRow && editingRow.type === 'FOLDER' ? 'Folder' : 'Functionality' + (editingRow.id ? ' ' + editingRow.id : ''))"
           :okText="(editingRow.id > 0 ? 'Save' : 'Create')" :loading="loadingSaving" ref="editPopup">
      <Form :label-width="120">
        <Form-item v-if="creatingReferenceNode && creatingRelativePosition" :label="creatingRelativePositionLabel">
          <strong>{{creatingReferenceNode.row.name}}</strong>
        </Form-item>
        <Form-item label="Name:" :required="true">
          <Input v-model="editingRow.name" @on-enter="onEditOk" ref="name" />
        </Form-item>
        <div v-if="editingRow && editingRow.type === 'FUNCTIONALITY'">
          <Form-item label="Team:" :required="true">
            <Select v-model="editingRow.teamId" filterable placeholder="None">
              <Option v-for="team in teamsAssignableToFunctionalities" :value="team.id" :key="team.id" :label="team.name" />
            </Select>
          </Form-item>
          <Form-item label="Severity:" :required="true">
            <Select v-model="editingRow.severity" filterable placeholder="None">
              <Option v-for="severity in severities" :value="severity" :key="severity" :label="severity.toLowerCase() | capitalize" />
            </Select>
          </Form-item>
          <Form-item label="Created:">
            <Input v-model="editingRow.created" @on-enter="onEditOk" />
          </Form-item>
          <Form-item label="Countries:" :required="true">
            <Checkbox
              v-for="country in countries"
              :key="country.code"
              v-model="editingRow.countryCodes[country.code].checked"
              :title="country.name">
                {{country.code.toUpperCase()}}
            </Checkbox>
          </Form-item>
          <Form-item label="Coverage:">
            <Checkbox style="line-height: 12px;" v-model="editingRow.started" @on-change="clickedStarted">Started (scenario written but not automated yet)</Checkbox>
            <Checkbox style="display: block; margin-top: -8px;" v-model="editingRow.notAutomatable" @on-change="clickedNotAutomatable">Not automatable (must be tested manually)</Checkbox>
            <div style="line-height: 12px; color: gray; margin-top: -2px;">
              Note:
              <span v-if="editingRow.id">use the identifier <strong>{{editingRow.id}}</strong></span>
              <span v-else>the functionality will get an identifier <strong>after</strong> it has been created. You will then be able to use that identifier</span>
              on automated Cucumber scenario title(s) to associate this functionality and mark it as covered.<br>
              Having at least one active or ignored scenario will override the "Started" and "Not automatable" statuses.
            </div>
          </Form-item>
          <Form-item label="Comment:">
            <Input v-model="editingRow.comment" type="textarea" :autosize="{ minRows: 2, maxRows: 15 }" />
          </Form-item>
        </div>
      </Form>
    </Modal>

    <Modal v-model="showCoverageDetails" class="noFooter" width="95%"
           :title="'Coverage for ' + (coverageDetailsNode ? coverageDetailsNode.row.name : '')">
      <functionality-coverage-details ref="coverageDetails" />
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import util from '../libs/util'

  import functionalityMenuComponent from '../components/functionality-menu'
  import functionalityNodeComponent from '../components/functionality-node'
  import functionalityCoverageDetailsComponent from '../components/functionality-coverage-details'

  import constants from '../libs/constants.js'

  const DEFAULT_FILTER = {
    id: '',
    functionality: '',
    countries: [],
    team: '',
    severity: '',
    created: '',
    coverage: '',
    comment: ''
  }

  export default {
    name: 'functionality-cartography',

    mixins: [{
      created () {
        this.util = util
        this.constants = constants
      }
    }],

    components: {
      'functionality-menu': functionalityMenuComponent,
      'functionality-node': functionalityNodeComponent,
      'functionality-coverage-details': functionalityCoverageDetailsComponent
    },

    data () {
      return {
        // Functionalities and filtering
        loadingFunctionalities: true,
        functionalities: [],
        flattenedMatchingFunctionalities: [],
        filter: { ...DEFAULT_FILTER },
        columnSizes: [
          0, // Id & Name (0 to take all remaining space)
          190, // Team
          56, // Severity
          80, // Created
          86, // Countries (default value: will be computed when countries are available, to know what width to allocate for all countries)
          180, // Coverage (default value: will be computed when sources are available, to know what width to allocate for all sources)
          110, // Comment
          66 // Actions
        ],
        counts: {
          matching: 0,
          total: 0
        },

        // Application data
        countries: [],
        sources: [],
        severities: [ 'LOW', 'MEDIUM', 'HIGH' ],

        // Edit/Create
        editingNode: null,
        editingRow: {
          countryCodes: {}
        },
        creatingReferenceNode: null,
        creatingRelativePosition: null,
        showingEditDialog: false,
        loadingSaving: true,

        // Move
        nodeToMove: null,
        destinationReferenceNodeId: null,

        // Coverage details popup
        coverageDetailsNode: null,
        showCoverageDetails: false
      }
    },

    computed: {
      teamsAssignableToFunctionalities () {
        return this.$store.getters['teams/teamsAssignableToFunctionalities'](this)
      },

      sumColumnSizes () {
        return this.columnSizes.reduce((a, b) => a + b, 0)
      },

      hasFilter () {
        for (var propertyName in DEFAULT_FILTER) {
          let value = this.filter[propertyName]
          let type = typeof value
          if ((type === 'string' && value) || (Array.isArray(value) && value.length > 0)) {
            return true
          }
        }
        return false
      },

      creatingRelativePositionLabel () {
        switch (this.creatingRelativePosition) {
          case 'ABOVE':
            return 'Above:'
          case 'BELOW':
            return 'Below:'
          case 'LAST_CHILD':
            return 'Inside of:'
          default:
            return `!!UNSUPPORTED:${this.creatingRelativePosition}!!`
        }
      }
    },

    methods: {
      loadFunctionalities () {
        this.loadingFunctionalities = true
        Vue.http
          .get(api.paths.functionalities(this), api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingFunctionalities = false
            this.parseFunctionalities(response.body)
          }, (error) => {
            this.loadingFunctionalities = false
            api.handleError(error)
          })
      },

      parseFunctionalities (json) {
        this.functionalities = this.parseFunctionalityNodes(json, 0)
        this.filterFunctionalities()
      },

      parseFunctionalityNodes (jsonNodes, level) {
        let nodes = []
        if (jsonNodes) {
          for (let i in jsonNodes) {
            let jsonNode = jsonNodes[i]
            jsonNode.countryCodes = util.toSplitted(jsonNode.countryCodes)
            nodes.push({
              id: jsonNode.id, // For virtual-scroller
              row: jsonNode,
              level,
              expanded: true,
              matches: true,
              hasMatchingChildren: false,
              children: this.parseFunctionalityNodes(jsonNode.children, level + 1),
              isPossibleMovingTarget: true
            })
          }
        }
        return nodes
      },

      toggleCountry (country) {
        this.filter.countries = util.toggle(this.filter.countries, this.countries.map(country => country.code), country)
        this.filterFunctionalities()
      },

      hasCountryFilter (countryCode) {
        return this.filter.countries.indexOf(countryCode) !== -1
      },

      toggleSeverity (severity) {
        this.filter.severity = (this.filter.severity === severity ? '' : severity)
        this.filterFunctionalities()
      },

      filterFunctionalities () {
        this.counts.matching = 0
        this.counts.total = 0

        this.filterNodes(this.functionalities)
        this.flattenMatchingFunctionalities()
        this.recomputeQueryString()
      },

      filterNodes (nodes) {
        let hasMatchingNodes = false
        for (var i in nodes) {
          let node = nodes[i]
          node.matches = this.matchFilters(node)
          if (node.row.type === 'FUNCTIONALITY') {
            this.counts.total++
            if (node.matches) {
              this.counts.matching++
            }
          }
          if (node.children) {
            node.hasMatchingChildren = this.filterNodes(node.children)
          } else {
            node.hasMatchingChildren = false
          }
          if (node.matches || node.hasMatchingChildren) {
            hasMatchingNodes = true
          }
        }
        return hasMatchingNodes
      },

      matchFilters (node) {
        return (
          this.matchFilterId(node) &&
          this.matchFilterFunctionality(node) &&
          this.matchFilterCountries(node) &&
          this.matchFilterTeam(node) &&
          this.matchFilterSeverities(node) &&
          this.matchFilterCreated(node) &&
          this.matchFilterCoverage(node) &&
          this.matchFilterComment(node)
        )
      },

      matchFilterId (node) {
        let criteria = this.filter.id
        if (!criteria) {
          return true
        }
        return node.row.type === 'FUNCTIONALITY' && node.row.id === parseInt(criteria, 10)
      },

      matchFilterFunctionality (node) {
        let criteria = this.filter.functionality
        if (!criteria) {
          return true
        }
        return node.row.name.toLowerCase().indexOf(criteria.toLowerCase()) !== -1
      },

      matchFilterCountries (node) {
        let criteria = this.filter.countries
        if (criteria && criteria.length > 0) {
          if (node.row.type === 'FOLDER') {
            return false
          }
          for (let i in criteria) {
            let country = criteria[i]
            if (!node.row.countryCodes || node.row.countryCodes.indexOf(country) === -1) {
              return false
            }
          }
        }
        return true
      },

      matchFilterTeam (node) {
        let criteria = this.filter.team
        if (!criteria) {
          return true
        }
        return ('' + node.row.teamId) === criteria && node.row.type === 'FUNCTIONALITY'
      },

      matchFilterSeverities (node) {
        let criteria = this.filter.severity
        if (!criteria) {
          return true
        }
        return node.row.severity === criteria
      },

      matchFilterCreated (node) {
        let criteria = this.filter.created
        if (!criteria) {
          return true
        }
        if (node.row.type === 'FOLDER') {
          return false
        }
        if (criteria === 'none') {
          return node.row.created === null || node.row.created === undefined || node.row.created === ''
        }
        return node.row.created && node.row.created.toLowerCase().indexOf(criteria.toLowerCase()) !== -1
      },

      matchFilterCoverage (node) {
        let criteria = this.filter.coverage
        if (!criteria) {
          return true
        }
        if (node.row.type === 'FOLDER') {
          return false
        }
        switch (criteria) {
          // "Life-cycle" of a functionality
          case 'COVERED':
            return !!node.row.coveredScenarios && !node.row.ignoredScenarios
          case 'PARTIALLY_COVERED':
            return !!node.row.coveredScenarios && !!node.row.ignoredScenarios
          case 'IGNORED_COVERAGE':
            return !node.row.coveredScenarios && !!node.row.ignoredScenarios
          case 'STARTED':
            return !node.row.coveredScenarios && !node.row.ignoredScenarios && node.row.started
          case 'NOT_AUTOMATABLE':
            return !node.row.coveredScenarios && !node.row.ignoredScenarios && !node.row.started && node.row.notAutomatable
          case 'NOT_COVERED':
            return !node.row.coveredScenarios && !node.row.ignoredScenarios && !node.row.started && !node.row.notAutomatable
          // Bonus filters
          case 'COVERED_OR_PARTIALLY':
            return !!node.row.coveredScenarios
          case 'COVERED_OR_IGNORED':
            return !!node.row.coveredScenarios || !!node.row.ignoredScenarios
          case 'WITH_IGNORES':
            return !!node.row.ignoredScenarios
          case 'NO_COVERAGE_AT_ALL':
            return !node.row.coveredScenarios && !node.row.ignoredScenarios
        }
        let source = this.sources.find(source => 'HAS_' + source.code === criteria)
        if (source) {
          let countryScenarios = '|' + node.row.coveredCountryScenarios + '|' + node.row.ignoredCountryScenarios
          return ('|' + countryScenarios).indexOf('|' + source.code + ':') !== -1
        }
        throw new Error('coverage ' + criteria + ' not handled')
      },

      matchFilterComment (node) {
        let criteria = this.filter.comment
        if (!criteria) {
          return true
        }
        return node.row.comment && node.row.comment.toLowerCase().indexOf(criteria.toLowerCase()) !== -1 && node.row.type === 'FUNCTIONALITY'
      },

      removeAllFilters () {
        this.filter = { ...DEFAULT_FILTER }
        this.filterFunctionalities()
      },

      flattenMatchingFunctionalities () {
        this.flattenedMatchingFunctionalities = []
        this.flattenMatchingnodes(this.functionalities, this.flattenedMatchingFunctionalities)
      },

      flattenMatchingnodes (nodes, flattenedNodes) {
        for (let i in nodes) {
          let node = nodes[i]
          if (node.matches || node.hasMatchingChildren) {
            this.flattenedMatchingFunctionalities.push(node)
          }
          if (node.expanded && node.children && node.hasMatchingChildren) {
            this.flattenMatchingnodes(node.children)
          }
        }
      },

      toggleExpand (node) {
        node.expanded = !node.expanded
        this.flattenMatchingFunctionalities()
      },

      expandAll () {
        this.setAllNodesExpanded(this.functionalities, true)
        this.flattenMatchingFunctionalities()
      },

      collapseAll () {
        this.setAllNodesExpanded(this.functionalities, false)
        this.flattenMatchingFunctionalities()
      },

      setAllNodesExpanded (nodes, expanded) {
        for (let i in nodes) {
          let node = nodes[i]
          node.expanded = expanded
          if (node.children) {
            this.setAllNodesExpanded(node.children, expanded)
          }
        }
      },

      recomputeQueryString () {
        let query = {}
        for (var propertyName in this.filter) {
          if (this.filter[propertyName]) {
            let value = this.filter[propertyName]
            if (propertyName === 'countries') {
              value = util.fromSplitted(value)
            }
            if (value !== '') {
              query[propertyName] = value
            }
          }
        }
        this.$router.replace({ query })
      },

      fromQueryString () {
        let query = this.$route.query
        if (query) {
          for (var propertyName in this.filter) {
            if (query[propertyName]) {
              let value = query[propertyName]
              if (propertyName === 'countries') {
                value = util.toSplitted(value)
              }
              this.filter[propertyName] = value
            }
          }
        }
      },

      toEditableCountryCodes (checkedCountries) {
        let countryCodes = {}
        for (let i in this.countries) {
          let countryCode = this.countries[i].code
          countryCodes[countryCode] = {
            checked: (checkedCountries && checkedCountries.indexOf(countryCode) !== -1)
          }
        }
        return countryCodes
      },

      copyRowPropertiesFromServer (serverRow, clientRow) {
        for (let property in serverRow) {
          let value = serverRow[property]
          if (property === 'countryCodes') {
            value = util.toSplitted(value)
          }
          clientRow[property] = value
        }
      },

      openEditDialog () {
        this.showingEditDialog = true
        this.$nextTick(() => this.$refs.name.focus())
      },

      startEditing (node, duplicate) {
        this.creatingReferenceNode = null
        this.creatingRelativePosition = null
        this.editingNode = node
        this.editingRow = {
          ...node.row,
          countryCodes: this.toEditableCountryCodes(node.row.countryCodes),
          started: !!node.row.started, // null to false
          notAutomatable: !!node.row.notAutomatable // null to false
        }
        if (duplicate) {
          this.creatingReferenceNode = node
          this.creatingRelativePosition = 'BELOW'
          this.editingNode = null
          this.editingRow.id = undefined
          this.editingRow.order = undefined
          this.editingRow.coveredScenarios = undefined
          this.editingRow.coveredCountryScenarios = undefined
          this.editingRow.ignoredScenarios = undefined
          this.editingRow.ignoredCountryScenarios = undefined
        }
        // If editing a node with a severity and then a node with a null severity, the combobox is not cleared: '' will clear it
        if (!this.editingRow.severity) {
          this.editingRow.severity = ''
        }
        this.openEditDialog()
      },

      startDuplicating (node) {
        this.startEditing(node, true)
      },

      create (type, referenceNode, relativePosition) {
        this.creatingReferenceNode = referenceNode
        this.creatingRelativePosition = relativePosition
        this.editingNode = null
        this.editingRow = {
          type,
          countryCodes: this.toEditableCountryCodes([]),
          started: false,
          notAutomated: false
        }
        this.openEditDialog()
      },

      clickedStarted () {
        // Mutually exclusive
        this.editingRow.notAutomatable = false
      },

      clickedNotAutomatable () {
        // Mutually exclusive
        this.editingRow.started = false
      },

      onEditOk () {
        let saveData = { ...this.editingRow }
        saveData.children = null
        saveData.countryCodes = ''
        if (saveData.severity === '') {
          saveData.severity = null
        }
        for (let i in this.countries) {
          let countryCode = this.countries[i].code
          if (this.editingRow.countryCodes[countryCode].checked) {
            saveData.countryCodes += (saveData.countryCodes === '' ? '' : ',') + countryCode
          }
        }
        if (this.editingNode) {
          this.saveNode(saveData)
        } else {
          this.createNode(saveData)
        }
      },

      saveNode (saveData) {
        Vue.http
          .put(api.paths.functionalities(this) + '/' + saveData.id, saveData, api.REQUEST_OPTIONS)
          .then((response) => {
            this.copyRowPropertiesFromServer(response.body, this.editingNode.row)
            this.filterFunctionalities()
            this.$refs.editPopup.close()
          }, (error) => {
            this.loadingSaving = false
            api.handleError(error, () => {
              this.loadingSaving = true
            })
          })
      },

      createNode (saveData) {
        let createInstructions = {
          functionality: saveData,
          referenceId: (this.creatingReferenceNode || {}).id,
          relativePosition: this.creatingRelativePosition
        }
        Vue.http
          .post(api.paths.functionalities(this) + '/', createInstructions, api.REQUEST_OPTIONS)
          .then((response) => {
            let newNode = {
              id: response.body.id, // For virtual-scroller
              row: {}, // To be filled by copyRowPropertiesFromServer()
              level: -1, // To be filled by appendNode()
              expanded: true,
              matches: true,
              hasMatchingChildren: false,
              children: [],
              isPossibleMovingTarget: true
            }
            this.copyRowPropertiesFromServer(response.body, newNode.row)

            this.appendNode(newNode, this.functionalities)
            this.filterFunctionalities()

            this.$refs.editPopup.close()
          }, (error) => {
            this.loadingSaving = false
            api.handleError(error, () => {
              this.loadingSaving = true
            })
          })
      },

      appendNode (newNode, nodes, parentId, level) {
        // Optional parameters for first iteration
        if (!parentId) {
          parentId = undefined
        }
        if (!level) {
          level = 0
        }

        if (newNode.row.parentId === parentId) {
          // We are in the destination parent: place the node here
          newNode.level = level
          this.setLevel(newNode.children, level + 1)
          for (let i = 0; i < nodes.length; i++) {
            let node = nodes[i]
            if (node.row.order > newNode.row.order) {
              nodes.splice(i, 0, newNode)
              return true
            }
          }
          nodes.push(newNode)
          return true
        } else {
          // Recursively browse children until one of them was the one into which to append the requested node
          for (let i = 0; i < nodes.length; i++) {
            let node = nodes[i]
            if (this.appendNode(newNode, node.children, node.id, level + 1)) {
              return true
            }
          }
          return false
        }
      },

      setLevel (nodes, level) {
        if (nodes) {
          for (let i = 0; i < nodes.length; i++) {
            let node = nodes[i]
            node.level = level
            this.setLevel(node.children, level + 1)
          }
        }
      },

      startMoving (nodeToMove) {
        this.nodeToMove = nodeToMove
        this.updateMovingTargets(this.functionalities, nodeToMove, true)
      },

      updateMovingTargets (nodes, nodeToMove, movable) {
        if (nodes) {
          for (let i = 0; i < nodes.length; i++) {
            let node = nodes[i]
            node.isPossibleMovingTarget = (movable && node.id !== nodeToMove.id)
            this.updateMovingTargets(node.children, nodeToMove, node.isPossibleMovingTarget)
          }
        }
      },

      completeMove (referenceNode, relativePosition) {
        // Show loading indicator while not null
        this.destinationReferenceNodeId = referenceNode.id

        let moveInstructions = {
          sourceId: this.nodeToMove.id,
          referenceId: referenceNode.id,
          relativePosition: relativePosition
        }
        Vue.http
          .post(api.paths.functionalities(this) + '/move', moveInstructions, api.REQUEST_OPTIONS)
          .then((response) => {
            this.destinationReferenceNodeId = null
            this.removeNode(this.nodeToMove, this.functionalities)
            this.nodeToMove.row = response.body
            this.appendNode(this.nodeToMove, this.functionalities)
            this.filterFunctionalities()
            this.nodeToMove = null
          }, (error) => {
            this.destinationReferenceNodeId = null
            api.handleError(error)
          })
      },

      cancelMove () {
        this.nodeToMove = null
      },

      removeNode (nodeToRemove, nodes) {
        if (nodes) {
          for (let i = 0; i < nodes.length; i++) {
            let node = nodes[i]
            if (node.id === nodeToRemove.id) {
              nodes.splice(i, 1)
              return true
            }
            if (this.removeNode(nodeToRemove, node.children)) {
              return true
            }
          }
        }
        return false
      },

      countChildren (node) {
        let counts = {
          FOLDER: 0,
          FUNCTIONALITY: 0
        }
        if (node.children) {
          for (let i = 0; i < node.children.length; i++) {
            let child = node.children[i]
            counts[child.row.type]++
            let childCounts = this.countChildren(child)
            counts.FOLDER += childCounts.FOLDER
            counts.FUNCTIONALITY += childCounts.FUNCTIONALITY
          }
        }
        return counts
      },

      deleteNode (node) {
        let childrenCounts = this.countChildren(node)
        let childrenMessage = ''
        if (childrenCounts.FOLDER > 0 || childrenCounts.FUNCTIONALITY > 0) {
          let subFoldersMessage = `${childrenCounts.FOLDER} folder${childrenCounts.FOLDER === 1 ? '' : 's'}`
          let functionalitiesMessage = `${childrenCounts.FUNCTIONALITY} ${childrenCounts.FUNCTIONALITY === 1 ? 'functionality' : 'functionalities'}`
          if (childrenCounts.FOLDER > 0 && childrenCounts.FUNCTIONALITY > 0) {
            childrenMessage = ` (+ ${subFoldersMessage} & ${functionalitiesMessage})`
          } else if (childrenCounts.FOLDER > 0) {
            childrenMessage = ` (+ ${subFoldersMessage})`
          } else {
            childrenMessage = ` (+ ${functionalitiesMessage})`
          }
        }
        let self = this
        this.$Modal.confirm({
          title: `Delete ${node.row.type === 'FOLDER' ? 'Folder' : 'Functionality'}`,
          content: `<p>Delete <b>${util.escapeHtml(node.row.name)}</b>?</p>`,
          okText: `Delete${childrenMessage}`,
          loading: true,
          onOk () {
            Vue.http
              .delete(api.paths.functionalities(self) + '/' + node.id, api.REQUEST_OPTIONS)
              .then((response) => {
                self.$Modal.remove()
                self.removeNode(node, self.functionalities)
                self.filterFunctionalities()
              }, (error) => {
                self.$Modal.remove()
                api.handleError(error)
              })
          }
        })
      },

      showCoverage (node) {
        this.$refs.coverageDetails.load(node)
        this.coverageDetailsNode = node
        this.showCoverageDetails = true
      },

      loadAll () {
        this.fromQueryString()
        this.loadFunctionalities()

        // this.loading = true
        Vue.http
          .get(api.paths.countries(this), api.REQUEST_OPTIONS)
          .then((response) => {
            // this.loading = false
            this.countries = response.body
            this.$set(this.columnSizes, 4, 24 * this.countries.length + 2 * 4)
          }, (error) => {
            api.handleError(error)
            // this.loading = false
          })

        // this.loading = true
        Vue.http
          .get(api.paths.sources(this), api.REQUEST_OPTIONS)
          .then((response) => {
            // this.loading = false
            this.sources = response.body
            // 32 is the size of ', 99 A'; we have 2 * 32 because normal and ignored scenario counts; and 52 is for margins, paddings, icons, and borders
            this.$set(this.columnSizes, 5, Math.max(150, 32 * 2 * this.sources.length + 52))
          }, (error) => {
            api.handleError(error)
            // this.loading = false
          })
      }
    },

    mounted () {
      this.loadAll()
    },

    watch: {
      '$route' (to, from) {
        if (to.params.projectCode !== from.params.projectCode) {
          this.loadAll()
        }
      }
    }
  }
</script>

<style>
  .headerRow {
    border: 1px solid #E9EAEC;
    border-bottom: none;
    background-color: #F8F8F9;
    font-weight: bold;
  }
  .headerRow:after {
    content: '';
    display: block;
    clear: both;
  }
  .headerRow .ivu-select-dropdown {
    font-weight: normal;
  }
  .headerCell {
    float: left;
    border-right: 1px solid #E9EAEC;
    padding: 2px 3px;
    height: 46px;
  }
  .headerCell:last-of-type {
    border-right: none;
  }

  .virtual-scroller {
    border: 1px solid #E9EAEC;
    border-bottom: none;
    background: #FAFAFD;
  }
  .no-displayed-row {
    border: 1px solid #E9EAEC;
    border-top: none;
    background: #FAFAFD;
    text-align: center;
    padding: 16px;
  }

  /* Make sure the two divs on the left and right of the title are same width (and shrink if not enough available width) to center the title */
  .balance {
    flex: 0 1 auto;
    width: 250px;
  }

  #helpLink {
    display: block;
    margin-top: 10px;
  }
  #helpLink:hover {
    color: #57A3F3;
  }
</style>
