<template>
  <span style="width: 100%;" :class="'functionality-node ' + (node.row.type) + (hasFilter && node.matches ? ' matches' : '') + (dropDownVisible ? ' dropDownVisible' : '') + (nodeToMove && nodeToMove.id === node.id ? ' moving' : '')" :id="node.row.id">

    <span class="name toHover cell" :style="'width: calc(100% - ' + sumColumnSizes + 'px); padding-left: ' + (node.level * 32 + 3) + 'px;'">
      <span class="expandedOnHover" v-if="hover" :style="'margin-left: ' + (node.level * 32) + 'px;'">
        <span v-if="node.hasMatchingChildren" @click="emitToggleExpand" class="expandButton"><!--
       --><Icon v-if="node.expanded" type="md-arrow-dropdown" size="16"/><!--
       --><Icon v-else type="md-arrow-dropright" size="16"/><!--
     --></span><!--
     --><span v-else class="noExpandButton"/><!--
     --><Icon style="margin: 0 4px" type="md-folder" v-if="node.row.type === 'FOLDER'"/><!--
     --><span v-if="node.row.type === 'FUNCTIONALITY'" class="id" title="Identifier to use on Cucumber scenario title(s) to associate automated test(s) to this functionality">
          <Icon type="md-document"/>
          {{node.row.id}}
        </span>
        {{node.row.name}}
      </span>
      <span @mouseover="hover = true" @mouseleave="hover = false" class="hoverSummary" :style="(hover ? 'color: transparent;' : '')">
        <span v-if="node.hasMatchingChildren" @click="emitToggleExpand" class="expandButton"><!--
       --><Icon v-if="node.expanded" type="md-arrow-dropdown" size="16"/><!--
       --><Icon v-else type="md-arrow-dropright" size="16"/><!--
     --></span><!--
     --><span v-else class="noExpandButton"/><!--
     --><Icon style="margin: 0 4px" type="md-folder" v-if="node.row.type === 'FOLDER'"/><!--
     --><span v-if="node.row.type === 'FUNCTIONALITY'" class="id" title="Identifier to use on Cucumber scenario title(s) to associate automated test(s) to this functionality">
          <Icon type="md-document"/>
          {{node.row.id}}
        </span>
        {{node.row.name}}
      </span>
    </span>

    <span class="cell" :style="'width: ' + (columnSizes[1]) + 'px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; direction: rtl; text-align: left;'" :title="teamName">
      {{teamName}}
    </span>

    <span class="cell" :style="'width: ' + (columnSizes[2]) + 'px;'">
      {{(node.row.severity ? node.row.severity.toLowerCase() : '') | capitalize}}
    </span>

    <span class="cell" :style="'width: ' + (columnSizes[3]) + 'px;'">
      {{node.row.created}}
    </span>

    <span class="cell" :style="'width: ' + (columnSizes[4]) + 'px; text-align: center;'" :class="(hasCoverage ? 'covered' : '') + (hoverCoverage ? ' hover' : '')" @mouseover="hoverCoverage = true" @mouseleave="hoverCoverage = false" @click="emitShowCoverage">
      <span
        v-if="node.row.type === 'FUNCTIONALITY'"
        v-for="country in countries"
        :key="country.code"
        :class="'country' +
                (node.row.countryCodes && node.row.countryCodes.indexOf(country.code) !== -1 ? ' active' : '') +
                (coveredCountries[country.code] ? ' coveredCountry' : (ignoredCountries[country.code] ? ' ignoredCountry' : ''))"
        :title="countryTitle(country)"
      >{{country.code.toUpperCase()}}</span>
    </span>

    <span class="cell" :style="'width: ' + (columnSizes[5]) + 'px;'" :class="(hasCoverage ? 'covered' : '') + (hoverCoverage ? ' hover' : '')" @mouseover="hoverCoverage = true" @mouseleave="hoverCoverage = false" @click="emitShowCoverage">
      <span style="display: block; float: left; width: 50%;">
        <span v-if="node.row.coveredScenarios" class="country active coveredCountry" :title="titleCoveredByCategoryType">
          <Icon type="md-checkmark"/>
          {{coveredByCategoryType}}
        </span>
        <span v-else-if="node.row.started && !node.row.ignoredScenarios"><Icon type="ios-more"/> Started</span>
        <span v-else-if="node.row.notAutomatable && !node.row.ignoredScenarios" style="white-space: nowrap;"><Icon type="md-outlet"/> Not automatable</span>
      </span>
      <span style="display: block; float: right; width: 50%;" v-if="node.row.ignoredScenarios">
        <span class="country active ignoredCountry" :title="titleIgnoredByCategoryType">
          <Icon type="md-warning"/>
          {{ignoredByCategoryType}}
        </span>
      </span>
    </span>

    <span class="cell" :style="'width: ' + (columnSizes[6]) + 'px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;' + (node.row.comment ? 'cursor: help;' : '')" :title="node.row.comment">
      <span class="dataInTooltip" style="border-bottom-color: lightgray;">{{node.row.comment}}</span>
    </span>

    <span class="cell" :style="'width: ' + (columnSizes[7]) + 'px; text-align: center; line-height: 25px;'">

      <div v-if="destinationReferenceNodeId === node.id">
        <!-- Saving indicator... -->
        <Spin style="width: 20px; margin: 3px auto 0 auto;"/>
      </div>

      <div v-else-if="!!destinationReferenceNodeId">
        <!-- Currently saving: show nothing on other nodes -->
      </div>

      <ButtonGroup v-else-if="!nodeToMove">
        <Button size="small" title="Edit" @click="emitEdit">
          <Icon type="md-create"/>
        </Button>
        <Dropdown title="Other actions" trigger="click" placement="bottom-end" :transfer="true" @on-visible-change="dropDownVisibilityChanged">
          <Button size="small">
            <Icon type="md-menu"/>
          </Button>
          <DropdownMenu slot="list">
            <DropdownItem>
              <div @click="emitMove">
                <Icon type="md-move"/> MOVE TO...
              </div>
            </DropdownItem>

            <DropdownItem v-if="node.row.type === 'FUNCTIONALITY'" divided>
              <div @click="emitDuplicate">
                <Icon type="md-copy"/> DUPLICATE
              </div>
            </DropdownItem>

            <DropdownItem divided>
              <div @click="emitCreate('FOLDER', 'ABOVE')">
                <Icon type="md-add"/><Icon type="md-folder"/><Icon type="md-arrow-round-up"/> CREATE FOLDER ABOVE THIS {{node.row.type}}
              </div>
            </DropdownItem>
            <DropdownItem v-if="node.row.type === 'FOLDER'">
              <div @click="emitCreate('FOLDER', 'LAST_CHILD')">
                <Icon type="md-add"/><Icon type="md-folder"/><Icon type="md-arrow-round-forward"/> CREATE FOLDER INSIDE THIS FOLDER
              </div>
            </DropdownItem>
            <DropdownItem>
              <div @click="emitCreate('FOLDER', 'BELOW')">
                <Icon type="md-add"/><Icon type="md-folder"/><Icon type="md-arrow-round-down"/> CREATE FOLDER BELOW THIS {{node.row.type}}
              </div>
            </DropdownItem>

            <DropdownItem divided>
              <div @click="emitCreate('FUNCTIONALITY', 'ABOVE')">
                <Icon type="md-add"/><Icon type="md-document"/><Icon type="md-arrow-round-up"/> CREATE FUNCTIONALITY ABOVE THIS {{node.row.type}}
              </div>
            </DropdownItem>
            <DropdownItem v-if="node.row.type === 'FOLDER'">
              <div @click="emitCreate('FUNCTIONALITY', 'LAST_CHILD')">
                <Icon type="md-add"/><Icon type="md-document"/><Icon type="md-arrow-round-forward"/> CREATE FUNCTIONALITY INSIDE THIS FOLDER
              </div>
            </DropdownItem>
            <DropdownItem>
              <div @click="emitCreate('FUNCTIONALITY', 'BELOW')">
                <Icon type="md-add"/><Icon type="md-document"/><Icon type="md-arrow-round-down"/> CREATE FUNCTIONALITY BELOW THIS {{node.row.type}}
              </div>
            </DropdownItem>

            <DropdownItem divided>
              <div @click="emitDelete">
                <Icon type="md-trash"/> DELETE
              </div>
            </DropdownItem>
          </DropdownMenu>
        </Dropdown>
      </ButtonGroup>

      <ButtonGroup v-else-if="node.isPossibleMovingTarget">
        <Dropdown title="Move to..." trigger="click" placement="bottom-end" :transfer="true" @on-visible-change="dropDownVisibilityChanged">
          <Button size="small" type="success">
            <Icon type="md-funnel"/>
          </Button>
          <DropdownMenu slot="list">
            <DropdownItem>
              <div @click="emitCompleteMove('ABOVE')">
                <Icon type="md-arrow-round-up"/> MOVE ABOVE THIS {{node.row.type}}
              </div>
            </DropdownItem>
            <DropdownItem v-if="node.row.type === 'FOLDER'">
              <div @click="emitCompleteMove('LAST_CHILD')">
                <Icon type="md-arrow-round-forward"/> MOVE INSIDE THIS FOLDER
              </div>
            </DropdownItem>
            <DropdownItem>
              <div @click="emitCompleteMove('BELOW')">
                <Icon type="md-arrow-round-down"/> MOVE BELOW THIS {{node.row.type}}
              </div>
            </DropdownItem>
          </DropdownMenu>
        </Dropdown>
        <Button size="small" type="warning" title="Cancel move" @click="emitCancelMove()" style="float: none;">
          <Icon type="md-close-circle"/>
        </Button>
      </ButtonGroup>

      <div v-else-if="nodeToMove.id === node.id" :title="'Currently moving this ' + node.row.type.toLowerCase()">
        <span style="display: inline-block; text-align: center; width: 22px;">
          <Icon type="md-move" style="display: inline-block;"/>
        </span>
        <Button size="small" type="warning" title="Cancel move" @click="emitCancelMove()" style="float: none;">
          <Icon type="md-close-circle"/>
        </Button>
      </div>

    </span>

  </span>
</template>

<script>
  import util from '../libs/util'

  export default {
    name: 'functionality-node',

    props: [ 'node', 'columnSizes', 'sumColumnSizes', 'hasFilter', 'countries', 'teams', 'nodeToMove', 'destinationReferenceNodeId', 'sources' ],

    data () {
      return {
        hover: false,
        hoverCoverage: false,
        dropDownVisible: false
      }
    },

    computed: {
      teamName () {
        let id = this.node.row.teamId
        if (id && this.teams) {
          for (let i in this.teams) {
            let team = this.teams[i]
            if (team.id === id) {
              return team.name
            }
          }
        }
        return ''
      },

      hasCoverage () {
        return this.node.row.coveredScenarios || this.node.row.ignoredScenarios
      },

      coveredCountries () {
        return this.countryAggregates(this.node.row.coveredCountryScenarios)
      },

      ignoredCountries () {
        return this.countryAggregates(this.node.row.ignoredCountryScenarios)
      },

      coveredByCategoryType () {
        return this.categoryTypeAggregates(this.coveredCountries, true)
      },

      ignoredByCategoryType () {
        return this.categoryTypeAggregates(this.ignoredCountries, true)
      },

      titleCoveredByCategoryType () {
        return this.categoryTypeAggregates(this.coveredCountries, false, false)
      },

      titleIgnoredByCategoryType () {
        return this.categoryTypeAggregates(this.ignoredCountries, false, true)
      }
    },

    methods: {
      countryAggregates (aggregates) {
        // Eg. aggregates = 'API:*=3,cn=3,nl=1|WEB:=4,*=6,all=1,be=2'
        let countries = {} // Will contain the total count per countryCode + total count per sourceCode (prefixed by '*_')
        if (aggregates) {
          // Eg. aggregates = [ 'API:*=3,cn=3,nl=1', 'WEB:=4,*=6,all=1,be=2' ]
          aggregates = aggregates.split('|')
          for (let i in aggregates) {
            let aggregate = aggregates[i].split(':')
            // Eg. sourceCode = 'WEB' & countryAggregates = [ '*=6', '=4', 'all=1', 'be=2' ]
            let sourceCode = aggregate[0]
            let countryAggregates = aggregate[1].split(',')
            for (let j in countryAggregates) {
              let countryAggregate = countryAggregates[j].split('=')
              // Eg. countryCode = 'be' & scenarioCount = '2'
              let countryCode = countryAggregate[0]
              let scenarioCount = parseInt(countryAggregate[1], 10)
              if (countryCode === '*') {
                if (countries['*_' + sourceCode]) {
                  countries['*_' + sourceCode] += scenarioCount
                } else {
                  countries['*_' + sourceCode] = scenarioCount
                }
              } else if (countryCode) { // Don't include scenarios without country code
                for (let k in this.countries) {
                  let countryCodeK = this.countries[k].code
                  if (countryCode === 'all' || countryCode === countryCodeK) {
                    if (countries[countryCodeK]) {
                      countries[countryCodeK] += scenarioCount
                    } else {
                      countries[countryCodeK] = scenarioCount
                    }
                  }
                }
              }
            }
          }
        }
        return countries
      },

      categoryTypeAggregates (aggregates, abbreviate, ignored) {
        const PREFIX = '*_'
        let sourceCodes = Object.keys(aggregates).filter(key => key.startsWith(PREFIX)).map(key => key.substring(PREFIX.length))
        let message = ''
        for (let i in sourceCodes) {
          let sourceCode = sourceCodes[i]
          let count = aggregates[PREFIX + sourceCode]
          if (count) {
            if (message) {
              message += ', '
            }
            let source = this.sources.find(source => source.code === sourceCode)
            if (!source) {
              source = {
                name: sourceCode,
                letter: sourceCode[0]
              }
            }

            message += count + ' ' + (abbreviate ? source.letter : source.name + (ignored ? ' ignored' : '') + ' scenario' + (count === 1 ? '' : 's'))
          }
        }
        return message
      },

      countryTitle (country) {
        let title = country.name

        let coveredCount = this.coveredCountries[country.code]
        if (coveredCount) {
          title += ': ' + coveredCount + ' scenario' + (coveredCount === 1 ? '' : 's')
        }

        let ignoredCount = this.ignoredCountries[country.code]
        if (ignoredCount) {
          title += (coveredCount ? ' & ' : ': ') + ignoredCount + ' ignored scenario' + (ignoredCount === 1 ? '' : 's')
        }

        return title
      },

      toggleCountry (country) {
        let oldNode = this.node
        let newNode = { ...oldNode }
        newNode.row = { ...oldNode.row }
        let oldCountries = (oldNode.row.countryCodes ? [ ...oldNode.row.countryCodes ] : [])
        newNode.row.countryCodes = util.toggle(oldCountries, this.countries, country)
        this.$emit('update', newNode)
      },

      emitToggleExpand () {
        this.$emit('toggleExpand', this.node)
      },

      emitEdit () {
        this.$emit('edit', this.node)
      },

      emitDuplicate () {
        this.$emit('duplicate', this.node)
      },

      emitMove () {
        this.$emit('move', this.node)
        // Buggy here, so we do it manually :-(
        this.dropDownVisible = false
      },

      emitCompleteMove (position) {
        this.$emit('completeMove', this.node, position)
        // Buggy here, so we do it manually :-(
        this.dropDownVisible = false
      },

      emitCancelMove () {
        this.$emit('cancelMove')
      },

      emitCreate (type, position) {
        this.$emit('create', type, this.node, position)
      },

      emitDelete () {
        this.loadingDelete = true
        this.$emit('delete', this.node)
      },

      emitShowCoverage () {
        if (this.hasCoverage) {
          this.$emit('showCoverage', this.node)
        }
      },

      dropDownVisibilityChanged (visible) {
        this.dropDownVisible = visible
      }
    }
  }
</script>

<style scoped>
  .functionality-node {
    vertical-align: top;
    border-bottom: 1px solid #E9EAEC;
  }
  .functionality-node:after {
    content: '';
    display: block;
    clear: both;
  }
  .functionality-node:hover,
  .functionality-node.dropDownVisible {
    background-color: #EBF7FF !important;
  }

  .FOLDER {
    color: gray;
  }
  .FUNCTIONALITY {
    background-color: white;
  }
  .FUNCTIONALITY .name {
    font-weight: bold;
  }

  .moving,
  .moving:hover,
  .moving .expandedOnHover,
  .moving .country.active {
    background-color: #0082C3 !important;
    color: white !important;
  }

  .expandButton,
  .noExpandButton {
    display: inline-block;
    width: 16px;
  }
  .expandButton {
    text-align: center;
    cursor: pointer;
  }

  .id {
    padding: 2px 3px 2px 6px;
    background-color: darkgray;
    color: white;
    border-radius: 4px;
    margin-right: 5px;
  }

  .cell {
    float: left;
    display: inline-block;
    border-right: 1px solid #E9EAEC;
    padding: 2px 3px;
    height: 30px;
    line-height: 26px;
  }
  .cell:last-of-type {
    border-right: none;
  }

  .covered {
    cursor: pointer;
  }
  .covered:hover,
  .covered:hover .country.active {
    color: #57A3F3 !important;
  }

  .expandedOnHover {
    position: absolute;
    top: -1px; /* Because of border */
    left: -1px; /* Because of border */
    display: inline-block;
    padding: 0 3px;
    border: 1px solid #E9EAEC;
    background-color: #EBF7FF;
    box-shadow: 0 0 6px -2px gray;
    z-index: 1;
    background: #EBF7FF;
    line-height: 30px;
  }
  .hoverSummary {
    position: relative;
    display: inline-block;
    z-index: 2;
    width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .toHover:hover span {
    text-overflow: clip;
  }

  .functionality-node {
    display: inline-block;
    position: relative;
  }
  .functionality-node.matches {
    background-color: #FFFFEA;
  }

  .country {
    visibility: hidden;
    padding: 2px;
    margin: 0 1px;
    border: 1px solid transparent;
    border-radius: 3px;
  }
  .country.active {
    visibility: visible;
    color: #2c3e50 !important;
  }

  .country.coveredCountry {
    border-color: green;
  }
  .country.ignoredCountry {
    border-style: dashed;
    border-color: gray;
  }
  .covered.hover .country.active.coveredCountry,
  .covered.hover .country.active.ignoredCountry {
    border-color: #57A3F3 !important;
  }
  .covered.hover,
  .covered.hover .country {
    color: #57A3F3 !important;
  }

  .ivu-btn {
    vertical-align: top;
    margin-top: 1px;
  }

  .ivu-dropdown-menu {
    padding: 0 !important;
  }
  .ivu-dropdown-item {
    padding: 7px 16px;
  }
</style>
