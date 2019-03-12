<template>
  <div>
    <functionality-menu />

    <Spin fix v-if="loadingCoverage" />
    <div v-else>
      <div style="text-align: center;">
        <span v-for="axisIndex in filterIndices" :key="axisIndex">
          {{coverage.axes[axisIndex].name}}:
          <button-group>
            <i-button
              size="small"
              v-for="(point, pointIndex) in coverage.axes[axisIndex].points"
              :key="pointIndex"
              :title="point.tooltip"
              :type="selectedAxes[axisIndex] === pointIndex ? 'primary' : 'default'"
              @click="selectAxis(axisIndex, pointIndex)">
                {{point.name}}
            </i-button>
          </button-group>
        </span>
      </div>

      <div>
        <table class="grid">
          <tr>
            <td>
              <div class="cell empty"></div>
            </td>

            <!-- X titles -->
            <td v-for="(xPoint, x) in coverage.axes[tableXIndex].points" :key="x" :title="xPoint.tooltip">
              <div class="cell title">{{xPoint.name}}</div>
            </td>

            <!-- Legend -->
            <td rowspan="1000" class="side-column details">
              <!-- Keep balanced with left side-column -->
              <Affix :offset-top="76">
                <div :class="'legend title' + (tableValues[hoverPoint ? hoverPoint.y : 0][hoverPoint ? hoverPoint.x : 0][0] === 0 ? ' none' : '')">
                  <div style="padding-left: 13px;">{{legendTitle}}</div>
                  <div>
                    <span class="aggregate-number">{{tableValues[hoverPoint ? hoverPoint.y : 0][hoverPoint ? hoverPoint.x : 0][0]}}</span>
                  </div>
                </div>
                <div v-for="(point, i) in coverage.axes[circleIndex].points" :key="i" v-if="i !== 0" :title="point.tooltip"
                  :class="'legend' + (tableValues[hoverPoint ? hoverPoint.y : 0][hoverPoint ? hoverPoint.x : 0][i] === 0 ? ' none' : '')">
                  <Icon :type="(tableValues[hoverPoint ? hoverPoint.y : 0][hoverPoint ? hoverPoint.x : 0][i] === 0 ? 'md-square-outline' : 'md-square')" :style="'color: ' + util.getFeatureCoverageColor(i) + ';'"/>
                  {{point.name}}
                  <div>
                    <span class="aggregate-number">
                      {{tableValues[hoverPoint ? hoverPoint.y : 0][hoverPoint ? hoverPoint.x : 0][i]}}
                    </span>
                  </div>
                </div>
              </Affix>
            </td>
          </tr>
          <tr v-for="(yPoint, y) in coverage.axes[tableYIndex].points" :key="y">
            <!-- Y titles -->
            <td class="side-column y-title">
              <div class="cell title y" :title="yPoint.tooltip" style="text-align: right;">{{yPoint.name}}</div>
            </td>
            <td v-for="(xPoint, x) in coverage.axes[tableXIndex].points" :key="x" :class="'cell data' + (x === 0 && y === 0 ? ' extra' : '') + (x === 0 || y === 0 ? ' big' : '')" v-on:mouseover="hoverPoint = { x, y }" v-on:mouseleave="hoverPoint = null">
              <router-link :to="cartographyLink(x, y)">
                <div>
                  <div v-if="tableValues[y][x][0] > 0">
                    <div class="circle">
                      <i-circle style="top: 2px;   left: 2px; color: #19BE6B;" :size="60" :stroke-width="8"  :percent="circlePercent(x, y, 5)" :trail-width="6" :trail-color="util.getFeatureCoverageColor(6)" :stroke-color="util.getFeatureCoverageColor(5)">{{percent(x, y)}}</i-circle>
                      <i-circle style="top: 1.5px; left: 1.5px;" :size="61" :stroke-width="10" :percent="circlePercent(x, y, 4)" trail-color="transparent" :stroke-color="util.getFeatureCoverageColor(4)"></i-circle>
                      <i-circle style="top: 1px;   left: 1px;"   :size="62" :stroke-width="12" :percent="circlePercent(x, y, 3)" trail-color="transparent" :stroke-color="util.getFeatureCoverageColor(3)"></i-circle>
                      <i-circle style="top: 0.5px; left: 0.5px;" :size="63" :stroke-width="14" :percent="circlePercent(x, y, 2)" trail-color="transparent" :stroke-color="util.getFeatureCoverageColor(2)"></i-circle>
                      <i-circle style="top: 0px;   left: 0px;"   :size="64" :stroke-width="16" :percent="circlePercent(x, y, 1)" trail-color="transparent" :stroke-color="util.getFeatureCoverageColor(1)"></i-circle>
                    </div>
                    <div style="color: #495060; margin: -4px 0 8px 0;" title="Number of covered (no ignored) functionalities over all functionalities">
                      <span style="color: #19BE6B;">{{tableValues[y][x][1]}}</span>/{{tableValues[y][x][0]}}
                    </div>
                  </div>
                  <div v-else class="circle none" style="margin-top: 20px; margin-bottom: 19px;">
                    -
                  </div>
                </div>
              </router-link>
            </td>
          </tr>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import util from '../libs/util'

  import functionalityMenuComponent from '../components/functionality-menu'

  export default {
    name: 'functionality-coverage',

    mixins: [{
      created () {
        this.util = util
      }
    }],

    components: {
      'functionality-menu': functionalityMenuComponent
    },

    data () {
      return {
        filterIndices: [ 0 ], // Country
        selectedAxes: [ 0 ], // 'All'
        tableXIndex: 1, // Severity
        tableYIndex: 2, // Team
        circleIndex: 3, // Coverage level
        hoverPoint: null,
        coverage: null,
        loadingCoverage: true
      }
    },
    computed: {
      tableValues () {
        let cells = []
        for (let y in this.coverage.axes[this.tableYIndex].points) {
          let row = []
          for (let x in this.coverage.axes[this.tableXIndex].points) {
            row.push(this.cellValues(x, y))
          }
          cells.push(row)
        }
        return cells
      },

      legendTitle () {
        let valueIndices = []
        for (let i = 0; i < this.coverage.axes.length; i++) {
          let done = false
          for (let j = 0; j < this.filterIndices.length; j++) {
            if (i === this.filterIndices[j]) {
              valueIndices[i] = this.selectedAxes[j]
              done = true
              break
            }
          }
          if (done) {
            continue
          }
          if (i === this.tableXIndex) {
            valueIndices[i] = (this.hoverPoint ? this.hoverPoint.x : 0)
          } else if (i === this.tableYIndex) {
            valueIndices[i] = (this.hoverPoint ? this.hoverPoint.y : 0)
          } else {
            valueIndices[i] = 0
          }
        }
        let legend = ''
        for (let i = 0; i < this.coverage.axes.length; i++) {
          let point = this.coverage.axes[i].points[valueIndices[i]]
          if (point.id !== '') {
            legend += (legend === '' ? '' : ' | ') + point.name
          }
        }
        if (legend === '') {
          legend = 'All'
        }
        return legend + ' functionalities'
      }
    },
    methods: {
      loadCoverage () {
        this.loadingCoverage = true
        Vue.http
          .get(api.paths.functionalities(this) + '/coverage', api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingCoverage = false
            this.coverage = response.body
            this.fromQueryString()
          }, (error) => {
            this.loadingCoverage = false
            api.handleError(error)
          })
      },

      selectAxis (axisIndex, pointIndex) {
        this.$set(this.selectedAxes, axisIndex, pointIndex)
        this.recomputeQueryString()
      },

      /**
       * @return the one-dimension array of values for a single cell in the table (functionality count per coverage level), given the selected filters.
       */
      cellValues (x, y) {
        let values = []
        for (let i = 0; i < this.coverage.axes[this.circleIndex].points.length; i++) {
          values.push(this.coverage.values[this.flatIndex([ this.selectedAxes[0], x, y, i ])])
        }
        return values
      },

      flatIndex (coordinates) {
        let index = coordinates[0]
        let multiplier = 1
        for (let i = 1; i < coordinates.length; ++i) {
          multiplier *= this.coverage.axes[i - 1].points.length
          index += coordinates[i] * multiplier
        }
        return index
      },

      percent (x, y) {
        let total = this.tableValues[y][x][0] // All
        let covered = (
          this.tableValues[y][x][1] // + // Covered (no ignored)
          // this.tableValues[y][x][2] // Partially covered (few ignored)
        )

        if (total > 0) {
          return Math.floor(100 * covered / total) + '%' // Floor to not display 100% if not fully covered (eg. covered at 99.8%)
        } else {
          return '-'
        }
      },

      circlePercent (x, y, z) {
        let total = this.tableValues[y][x][0] // All
        let filled = 0
        for (let i = 1; i <= z; i++) {
          filled += this.tableValues[y][x][i]
        }

        if (total > 0) {
          return Math.floor(100 * filled / total) // Floor to not display 100% if not fully covered (eg. covered at 99.8%)
        } else {
          return 0
        }
      },

      computeQueryString () {
        let query = {}

        for (let i = 0; i < this.filterIndices.length; i++) {
          let selectedAxisIndex = this.selectedAxes[i]
          if (selectedAxisIndex > 0) { // 0 = All
            let axisIndex = this.filterIndices[i]
            let axisCode = this.coverage.axes[axisIndex].code
            let axisValue = this.coverage.axes[axisIndex].points[selectedAxisIndex].id
            query[axisCode] = axisValue
          }
        }

        return query
      },

      recomputeQueryString () {
        this.$router.replace({ query: this.computeQueryString() })
      },

      findWithAttr (array, attr, value) {
        for (let i = 0; i < array.length; i++) {
          if (array[i][attr] === value) {
            return i
          }
        }
        return -1
      },

      fromQueryString () {
        let query = this.$route.query
        if (query) {
          for (let i = 0; i < this.filterIndices.length; i++) {
            let axisIndex = this.filterIndices[i]
            let axisCode = this.coverage.axes[axisIndex].code
            if (query[axisCode]) {
              let index = this.findWithAttr(this.coverage.axes[axisIndex].points, 'id', query[axisCode])
              if (index !== -1) {
                this.selectedAxes[i] = index
              }
            }
          }
        }
      },

      cartographyLink (x, y) {
        let query = this.computeQueryString()

        if (x > 0) {
          let axisIndex = this.tableXIndex
          let axisCode = this.coverage.axes[axisIndex].code
          let axisValue = this.coverage.axes[axisIndex].points[x].id
          query[axisCode] = axisValue
        }

        if (y > 0) {
          let axisIndex = this.tableYIndex
          let axisCode = this.coverage.axes[axisIndex].code
          let axisValue = this.coverage.axes[axisIndex].points[y].id
          query[axisCode] = axisValue
        }

        return {
          name: 'functionality-cartography',
          query
        }
      }
    },

    mounted () {
      this.loadCoverage()
    },

    watch: {
      '$route' (to, from) {
        if (to.params.projectCode !== from.params.projectCode) {
          this.loadCoverage()
        }
      }
    }

  }
</script>

<style scoped>
  .grid { border-spacing: 4px; margin: 0 -4px; font-size: 1em; text-align: center; margin: 16px auto 0 auto; }
  .side-column { width: 200px; }
  .side-column.details { vertical-align: top; text-align: left; }
  .cell { border: 1px solid #E3E8EE; border-radius: 3px; background-color: transparent; width: 100px; }
  .cell a { display: block; padding: 2px 6px; }
  .cell.empty { border-color: transparent; width: auto; background: none; }
  .cell.title { border-color: transparent; width: auto; background: none; }
  .cell.title.y { text-align: left; }
  .cell.big { border-width: 2px; border-color: #CBD6DE; background-color: white; font-weight: bold; font-size: 1.1em; }
  .cell.extra.big { border-color: #A5B4C4; font-size: 1.2em; }
  .cell.data:hover { background-color: #EBF7FF; }

  .circle { position: relative; width: 64px; height: 64px; margin: 8px auto; }
  .circle .ivu-chart-circle { position: absolute; top: 0; left: 0; }
  .circle.none { line-height: 64px; }

  .legend { margin: 0 0 12px 12px; line-height: 1.2em; }
  .legend.title { border-bottom: 1px solid #DDDEE1; padding: 28px 0 16px 0; margin-bottom: 16px; min-height: calc(3 * 1.2em + 28px + 16px + 16px); }
  .aggregate-number { display: block; font-size: 2em; line-height: 1em; padding-left: 13px; margin-top: 4px; }
  .none,
  .none i { color: lightgray !important; }
</style>
