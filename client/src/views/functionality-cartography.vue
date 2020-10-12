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
  <div>
    <functionality-menu />

    <Spin fix v-if="loadingSettings"></Spin>

    <div v-else-if="countries.length === 0 || sources.length === 0 || teamsAssignableToFunctionalities.length === 0">
      <h2 style="text-align: center; width: 75%; margin: auto; font-weight: normal;">
        <span>
          Before adding functionalities, please make sure to add at least
          a <router-link :to="{ name: 'management-countries' }">country</router-link>,
          a <router-link :to="{ name: 'management-sources' }">source</router-link> and
          a <router-link :to="{ name: 'management-teams' }">team</router-link> (that is assignable to functionalities).
        </span>
      </h2>

    </div>

    <div v-else>
      <Spin fix v-if="loadingFunctionalities"/>
      <!-- Screen title with help -->
      <div style="display: flex;">
        <div class="balance">
        </div>

        <div style="flex: 1 0 auto;">
          <h1 style="text-align: center; margin-bottom: 16px;">
            <span v-if="counts.total === 0">No functionality</span>
            <span v-else-if="counts.matching === counts.total && counts.total === 1">Showing the only functionality</span>
            <span v-else-if="counts.matching === counts.total">Showing all <strong>{{counts.total}}</strong> functionalities</span>
            <span v-else>Showing <strong>{{counts.matching}}</strong> filtered functionalit{{counts.matching == 1 ? 'y' : 'ies'}} out of <strong>{{counts.total}}</strong></span><!--
            --><span v-if="selectionCount > 0">, <strong>{{selectionCount}}</strong> selected</span>
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
           <if-feature-enabled code="xprt-mprt-crtg">
              <template v-if="!isMovingSelection">
                <Dropdown title="Other actions" trigger="click" placement="bottom-end" :transfer="true" >
                  <Button size="small">
                    <Icon type="md-menu"/>
                  </Button>
                  <DropdownMenu slot="list">
                    <DropdownItem>
                      <div @click="removeAllFilters()">
                        <Icon type="md-backspace"/> REMOVE ALL FILTERS
                      </div>
                    </DropdownItem>
                    <DropdownItem divided>
                      <div @click="openExportPopup">
                        <Icon type="md-cloud-download" /> EXPORT CURRENT FUNCTIONALITIES
                      </div>
                    </DropdownItem>
                    <DropdownItem>
                      <div @click="openImportPopup">
                        <Icon type="md-cloud-upload" /> IMPORT NEW FUNCTIONALITIES
                      </div>
                    </DropdownItem>
                    <DropdownItem divided>
                      <div @click="selectAll()">
                        <Icon type="md-checkbox-outline"/> SELECT ALL
                      </div>
                    </DropdownItem>
                    <DropdownItem>
                      <div @click="clearSelection()">
                        <Icon type="md-square-outline" /> CLEAR SELECTION
                      </div>
                    </DropdownItem>
                    <DropdownItem :disabled="noSelection" divided>
                      <div @click="startMovingSelection()">
                        <Icon type="md-move"/> MOVE SELECTION TO...
                      </div>
                    </DropdownItem>
                    <DropdownItem :disabled="noSelection">
                      <div @click="deleteSelection()">
                        <Icon type="md-trash" /> DELETE SELECTION
                      </div>
                    </DropdownItem>
                  </DropdownMenu>
                </Dropdown>
                <functionality-export-popup ref="exportPopup" />
                <functionality-import-popup ref="importPopup" />
              </template>
              <template v-else>
                <Button size="small" type="warning" title="Cancel move" @click="cancelMove()" style="float: none;">
                  <Icon type="md-close-circle"/>
                </Button>
              </template>
          </if-feature-enabled>
          <if-feature-disabled code="xprt-mprt-crtg">
            <Button size="small" :disabled="!hasFilter" @click="removeAllFilters()" title="Remove all filters">
              <Icon type="md-backspace"/>
            </Button>
          </if-feature-disabled>
        </div>
        <div class="headerCell" :style="'width: ' + (columnSizes[8]) + 'px; text-align: center;'"></div>

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
            :isMovingSelection="isMovingSelection"
            :sources="sources"
            v-on:updateSelection="updateSelection"
            v-on:toggleExpand="toggleExpand"
            v-on:edit="startEditing"
            v-on:duplicate="startDuplicating"
            v-on:move="startMoving"
            v-on:completeMove="completeMove"
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
  import ifFeatureEnabledComponent from '../components/if-feature-enabled'
  import ifFeatureDisabledComponent from '../components/if-feature-disabled'
  import functionalityExportPopup from '../components/functionality-export-popup'
  import functionalityImportPopup from '../components/functionality-import-popup'

  import constants from '../libs/constants.js'

  import _ from 'lodash'

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
      'functionality-coverage-details': functionalityCoverageDetailsComponent,
      'if-feature-enabled': ifFeatureEnabledComponent,
      'if-feature-disabled': ifFeatureDisabledComponent,
      'functionality-export-popup': functionalityExportPopup,
      'functionality-import-popup': functionalityImportPopup
    },

    data () {
      return {
        // Functionalities and filtering
        loadingSettings: true,
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
          60, // Actions
          25 // Selection
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

        // Coverage details popup
        coverageDetailsNode: null,
        showCoverageDetails: false,

        // Selection
        isMovingSelection: false,
        nodesSelection: []
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
      },

      noSelection () {
        return !this.nodesSelection || this.nodesSelection.length === 0
      },

      selectionCount () {
        let count = 0
        for (let i in this.nodesSelection) {
          const selection = this.nodesSelection[i]
          if (selection.row.type === 'FUNCTIONALITY') {
            count++
          } else {
            count += this.countChildren(selection).FUNCTIONALITY
          }
        }
        return count
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
            jsonNode.countryCodes = util.toSplit(jsonNode.countryCodes)
            nodes.push({
              id: jsonNode.id, // For virtual-scroller
              row: jsonNode,
              level,
              expanded: true,
              matches: true,
              hasMatchingChildren: false,
              isSelected: false,
              children: this.parseFunctionalityNodes(jsonNode.children, level + 1),
              moveDetails: {
                isBeingMoved: false,
                isMovingNodesParent: false,
                isInLowerHierarchy: false
              }
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
            const isEmptyFolder = node.row.type === 'FOLDER' && node.children.length === 0
            node.hasMatchingChildren = isEmptyFolder ? true : this.filterNodes(node.children)
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
        if (this.hasFilter()) {
          this.filter = { ...DEFAULT_FILTER }
          this.filterFunctionalities()
        }
      },

      flattenMatchingFunctionalities () {
        this.flattenedMatchingFunctionalities = []
        this.flattenMatchingNodes(this.functionalities, this.flattenedMatchingFunctionalities)
      },

      flattenMatchingNodes (nodes, flattenedNodes) {
        for (let i in nodes) {
          let node = nodes[i]
          if (node.matches || node.hasMatchingChildren) {
            this.flattenedMatchingFunctionalities.push(node)
          }
          if (node.expanded && node.children && node.hasMatchingChildren) {
            this.flattenMatchingNodes(node.children)
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
              value = util.fromSplit(value)
            }
            if (value !== '') {
              query[propertyName] = value
            }
          }
        }
        this.$router.replace({ query }).catch(() => {})
      },

      fromQueryString () {
        let query = this.$route.query
        if (query) {
          for (var propertyName in this.filter) {
            if (query[propertyName]) {
              let value = query[propertyName]
              if (propertyName === 'countries') {
                value = util.toSplit(value)
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
            value = util.toSplit(value)
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
              moveDetails: {
                isBeingMoved: false,
                isMovingNodesParent: false,
                isInLowerHierarchy: false
              }
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

      selectOnlyThisNode (node, nodeId) {
        node.isSelected = (node.id === nodeId)

        const children = node.children

        if (children) {
          for (let i in children) {
            const child = children[i]
            this.selectOnlyThisNode(child, nodeId)
          }
        }
      },

      startMoving (node) {
        this.nodesSelection = [node]

        for (let i in this.functionalities) {
          const child = this.functionalities[i]
          this.selectOnlyThisNode(child, node.id)
        }

        this.propagateSelectionToChildren(node)

        this.startMovingSelection()
      },

      completeMove (referenceNode, relativePosition) {
        if (this.nodesSelection && this.nodesSelection.length > 0) {
          const selectionIds = _(this.nodesSelection).map('id').value()
          let moveInstructions = {
            sourceIds: selectionIds,
            referenceId: referenceNode.id,
            relativePosition: relativePosition
          }

          this.loadingFunctionalities = true

          Vue.http
            .post(api.paths.functionalities(this) + '/move/list', moveInstructions, api.REQUEST_OPTIONS)
            .then((response) => {
              this.loadingFunctionalities = false
              this.isMovingSelection = false
              this.nodesSelection = []
              this.parseFunctionalities(response.body)
            }, (error) => {
              this.loadingFunctionalities = false
              api.handleError(error)
            })
        }
      },

      cancelMove () {
        this.isMovingSelection = false
        this.resetAllNodesTargetDetails()
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

      getDeleteMessage (node) {
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
        return childrenMessage
      },

      deleteNode (node) {
        const childrenMessage = this.getDeleteMessage(node)
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
        this.isMovingSelection = false
        this.nodesSelection = []

        this.fromQueryString()
        this.loadFunctionalities()

        this.loadingSettings = true

        Vue.http
          .get(api.paths.countries(this), api.REQUEST_OPTIONS)
          .then((response) => {
            this.countries = response.body
            this.$set(this.columnSizes, 4, 24 * this.countries.length + 2 * 4)
            Vue.http
              .get(api.paths.sources(this), api.REQUEST_OPTIONS)
              .then((response) => {
                this.sources = response.body
                // 32 is the size of ', 99 A'; we have 2 * 32 because normal and ignored scenario counts; and 52 is for margins, paddings, icons, and borders
                this.$set(this.columnSizes, 5, Math.max(150, 32 * 2 * this.sources.length + 52))
                this.loadingSettings = false
              }, (error) => {
                this.loadingSettings = false
                api.handleError(error)
              })
          }, (error) => {
            this.loadingSettings = false
            api.handleError(error)
          })
      },

      applySelectToNode (node, selected) {
        node.isSelected = selected
        if (node.children) {
          for (let i in node.children) {
            const child = node.children[i]
            this.applySelectToNode(child, selected)
          }
        }
      },

      applySelectToAllNodes (selected) {
        let selection = []
        for (let i in this.functionalities) {
          const child = this.functionalities[i]
          this.applySelectToNode(child, selected)
          if (selected) {
            selection = selection.concat(child)
          }
        }
        this.nodesSelection = selection
      },

      selectAll () {
        this.applySelectToAllNodes(true)
      },

      clearSelection () {
        this.applySelectToAllNodes(false)
      },

      propagateSelectionToChildren (node) {
        if (node.children) {
          for (let i in node.children) {
            const child = node.children[i]
            child.isSelected = node.isSelected
            this.propagateSelectionToChildren(child)
          }
        }
      },

      propagateSelectionToParent (node) {
        let currentParentId = node.row.parentId

        while (currentParentId) {
          const parentNode = this.getNodeFromId(this.functionalities, currentParentId)
          const children = parentNode.children
          const notAllChecked = !_(children).every('isSelected')
          currentParentId = parentNode.row.parentId

          if (notAllChecked) {
            parentNode.isSelected = false
          }
        }
      },

      findNodeInElement (node, nodeId) {
        if (node.id === nodeId) {
          return node
        } else if (node.children) {
          let i
          let result = null
          for (i = 0; result == null && i < node.children.length; i++) {
            let child = node.children[i]
            result = this.findNodeInElement(child, nodeId)
          }
          return result
        }
        return null
      },

      getNodeFromId (nodes, nodeId) {
        for (let i in nodes) {
          let node = nodes[i]
          let nodeFound = this.findNodeInElement(node, nodeId)
          if (nodeFound) {
            return nodeFound
          }
        }
      },

      getNodeSelection (node) {
        const isFolder = node.row.type === 'FOLDER'
        if (node.isSelected) {
          return [node]
        } else if (node.children && isFolder) {
          let selection = []
          for (let i in node.children) {
            const child = node.children[i]
            selection = selection.concat(this.getNodeSelection(child))
          }
          return selection
        }
        return []
      },

      updateNodesSelection () {
        let selection = []
        for (let i in this.functionalities) {
          const child = this.functionalities[i]
          selection = selection.concat(this.getNodeSelection(child))
        }
        this.nodesSelection = selection
      },

      updateSelection (node) {
        this.propagateSelectionToChildren(node)
        this.propagateSelectionToParent(node)
        this.updateNodesSelection()
      },

      updateTargetDetails (node) {
        const children = node.children

        const isBeingMoved = _(this.nodesSelection).some(['id', node.id])
        const isInLowerHierarchy = node.isSelected && !isBeingMoved
        const isParentOfMovingNode = _(this.nodesSelection).map(function (n) { return n.row.parentId }).value().includes(node.id)
        if (isBeingMoved) {
          node.moveDetails.isBeingMoved = true
        }
        if (isInLowerHierarchy) {
          node.moveDetails.isInLowerHierarchy = true
        }
        if (isParentOfMovingNode) {
          node.moveDetails.isMovingNodesParent = true
        }

        if (children) {
          for (let i in children) {
            const child = children[i]
            this.updateTargetDetails(child)
          }
        }
      },

      startMovingSelection () {
        if (this.noSelection) {
          return
        }
        this.isMovingSelection = true
        for (let i in this.functionalities) {
          const child = this.functionalities[i]
          this.updateTargetDetails(child)
        }
      },

      resetNodeTargetDetails (node) {
        node.moveDetails.isBeingMoved = false
        node.moveDetails.isInLowerHierarchy = false
        node.moveDetails.isMovingNodesParent = false

        const children = node.children

        if (children) {
          for (let i in children) {
            const child = children[i]
            this.resetNodeTargetDetails(child)
          }
        }
      },

      resetAllNodesTargetDetails () {
        for (let i in this.functionalities) {
          const child = this.functionalities[i]
          this.resetNodeTargetDetails(child)
        }
      },

      deleteSelection () {
        if (this.noSelection) {
          return
        }

        this.isMovingSelection = false

        const selectionNumber = this.nodesSelection.length

        const hasOnlyFunctionalities = _(this.nodesSelection).every((node) => node.row.type === 'FUNCTIONALITY')
        const hasOnlyFolders = _(this.nodesSelection).every((node) => node.row.type === 'FOLDER')

        let title = 'Delete '
        if (hasOnlyFunctionalities) {
          title += (selectionNumber === 1 ? 'a' : selectionNumber) + ' functionalit' + (selectionNumber > 1 ? 'ies' : 'y')
        } else if (hasOnlyFolders) {
          title += (selectionNumber === 1 ? 'a' : selectionNumber) + ' folder' + (selectionNumber > 1 ? 's' : '')
        } else {
          title += 'selection (' + selectionNumber + ')'
        }

        let content = '<p>Delete '
        const nodeDescriptions = _(this.nodesSelection)
          .map((node) => `<b>${util.escapeHtml(node.row.name)}</b> ` + this.getDeleteMessage(node))
          .values()
          .join(', ')
        content += nodeDescriptions + '?</p>'

        let okText = 'Delete selection'

        const ids = _(this.nodesSelection)
          .map((node) => `id=${node.id}`)
          .values()
          .join('&')

        let self = this
        this.$Modal.confirm({
          title: title,
          content: content,
          okText: okText,
          loading: true,
          onOk () {
            self.loadingFunctionalities = true
            Vue.http
              .delete(api.paths.functionalities(self) + '?' + ids, api.REQUEST_OPTIONS)
              .then((response) => {
                self.$Modal.remove()
                self.loadingFunctionalities = false
                self.nodesSelection = []
                self.parseFunctionalities(response.body)
              }, (error) => {
                self.$Modal.remove()
                self.loadingFunctionalities = false
                api.handleError(error)
              })
          }
        })
      },

      // Export / Import
      openExportPopup () {
        var projectCode = this.$route.params.projectCode
        this.$refs.exportPopup.openExportPopup(projectCode, this.flattenedMatchingFunctionalities)
      },

      openImportPopup () {
        var projectCode = this.$route.params.projectCode
        this.$refs.importPopup.openImportPopup(projectCode)
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
