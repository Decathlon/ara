<template>
  <div class="problems-kpi-area">
    <RadioGroup :value="activeProblemId" style="display: inline">
      <div v-for="problem in sortedProblems" :key="problem.id" style="margin-bottom: 8px">
        <div class="problem-occurrence" @click="select(problem.id)">
          <Radio :label="problem.id">
            <div class="total">
              {{problemTotals[problem.id].text}}
            </div>
          </Radio>
        </div>
        <div class="tag-container" :style="(problemTotals[problem.id].totalIfClicked === 0 ? 'opacity: 0.5;' : '')">
          <problem-tag :problem="problem" :link="true" :filteredTeamId="filteredTeamId"/>
        </div>
      </div>
    </RadioGroup>
  </div>
</template>

<script>
  import problemComponent from '../components/problem-tag'

  export default {
    name: 'nrt-cycle-problems',

    props: ['problems', 'problemTotals', 'filteredTeamId', 'activeProblemId'],

    components: {
      'problem-tag': problemComponent
    },

    methods: {
      select (value) {
        if (this.activeProblemId && parseInt(this.activeProblemId, 10) === value) {
          this.$emit('dispatch-selected-problem', null)
        } else {
          this.$emit('dispatch-selected-problem', value)
        }
      }
    },

    computed: {
      sortedProblems () {
        if (!this.problemTotals) {
          return []
        }
        let sortedArray = []
        for (let problem of this.problems) {
          sortedArray.push(problem)
        }
        let self = this
        return sortedArray.sort(function (problem1, problem2) {
          let total1 = self.problemTotals[problem1.id]
          let total2 = self.problemTotals[problem2.id]

          if (!total1 || !total2) {
            return 0
          }

          // First-priority criteria: problem has the most occurences in current filtered view
          let diff = total2.totalIfClicked - total1.totalIfClicked
          if (diff !== 0) {
            return diff
          }

          // Second-priority criteria: problem has the most occurences in total (unfiltered view)
          diff = total2.total - total1.total
          if (diff !== 0) {
            return diff
          }

          // Least-priority criteria: sort by name
          return problem1.name.toLowerCase().localeCompare(problem2.name.toLowerCase())
        })
      }
    }
  }
</script>

<style>
  .problems-kpi-area {
    overflow-y: auto;
    width: calc(100% - 16px);
    position: relative;
    height: 100%;
  }

  .tag-container {
    margin-left: 101px;
    margin-right: calc(16px - 4px);
  }

  .problem-occurrence {
    margin: 0 8px 0 2px;
    height: 22px;
    padding-right: 5px;
    float: left;
  }

  .problem-occurrence .ivu-radio-group-item {
    display: block;
    width: 90px;
    margin: 0 -2px;
    padding: 1px 2px;
    border-radius: 3px;
  }

  .problem-occurrence .ivu-radio-group-item:hover {
    background-color: #F5F7F9;
  }

  .total {
    display: inline-block;
    color: gray;
  }

  .ivu-radio-group > div:last-of-type {
    margin-bottom: 0 !important;
  }
</style>
