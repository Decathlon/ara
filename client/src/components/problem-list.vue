<template>
  <div>
    <table border class="table">
      <thead>
        <tr>
          <th v-if="selectButtonText"></th>
          <th v-if="showProblemNames">Problem</th>
          <th>First Seen</th>
          <th>Last Seen</th>
          <th>Stability</th>
          <th>Errors</th>
          <th>Scenario(s)</th>
          <th>Branch(s)</th>
          <th>Release(s)</th>
          <th>Version(s)</th>
          <th>Country(ies)</th>
          <th>Type(s)</th>
          <th>Platform(s)</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="problem in problems" :key="problem.id">
          <td v-if="selectButtonText && showProblemNames">
            <Button type="primary" @click="select(problem.id, problem.name)" style="margin: 4px;"
                    :loading="loadingSelect && problem.id == selectedProblemId"
                    :disabled="loadingSelect && problem.id != selectedProblemId">{{selectButtonText}}
            </Button>
          </td>
          <td v-if="showProblemNames" style="width: 100%;">
            <div>
              <problem-tag :problem="problem" :link="true"/><!-- No space between problem-tag and comment-icon
           --><span v-if="problem.comment"
                    :title="'Comment: ' + problem.comment"
                    style="display: inline-block; vertical-align: top;"
                    ><Icon type="md-text" size="20"/></span>
            </div>
            <span style="color: rgb(158, 167, 180);">Creation: <strong>{{formatDate(problem.creationDateTime)}}</strong></span>
            <span v-if="problem.rootCause" style="margin-left: 8px;">Root cause: <strong>{{problem.rootCause.name}}</strong></span>
          </td>

          <td>{{formatDate(problem.firstSeenDateTime)}}</td>
          <td>{{formatDate(problem.lastSeenDateTime)}}</td>

          <td style="white-space: nowrap;">
            <problem-stability :cycleStabilities="problem.aggregate.cycleStabilities" :problem="problem"/>
          </td>
          <td>{{problem.aggregate.errorCount}}</td>
          <td>
            <em v-if="problem.aggregate.scenarioCount > 1">{{problem.aggregate.scenarioCount}}</em>
            <span v-else :title="problem.aggregate.firstScenarioName">{{withEllipsis(problem.aggregate.firstScenarioName)}}</span>
          </td>
          <td>
            <em v-if="problem.aggregate.branchCount > 1">{{problem.aggregate.branchCount}}</em>
            <span v-else>{{problem.aggregate.firstBranch}}</span>
          </td>
          <td>
            <em v-if="problem.aggregate.releaseCount > 1">{{problem.aggregate.releaseCount}}</em>
            <span v-else>{{problem.aggregate.firstRelease}}</span>
          </td>
          <td>
            <em v-if="problem.aggregate.versionCount > 1">{{problem.aggregate.versionCount}}</em>
            <span v-else>{{problem.aggregate.firstVersion}}</span>
          </td>
          <td>
            <em v-if="problem.aggregate.countryCount > 1">{{problem.aggregate.countryCount}}</em>
            <span v-else-if="problem.aggregate.firstCountry">{{problem.aggregate.firstCountry.name}}</span>
          </td>
          <td>
            <em v-if="problem.aggregate.typeCount > 1">{{problem.aggregate.typeCount}}</em>
            <span v-else-if="problem.aggregate.firstType">{{problem.aggregate.firstType.name}}</span>
          </td>
          <td>
            <em v-if="problem.aggregate.platformCount > 1">{{problem.aggregate.platformCount}}</em>
            <span v-else>{{problem.aggregate.firstPlatform}}</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
 import util from '../libs/util'
 import problemTagComponent from '../components/problem-tag'
 import problemStabilityComponent from '../components/problem-stability'
 export default {
   name: 'problem-list',
   props: ['problems', 'selectButtonText', 'showProblemNames'],
   mixins: [{
     methods: util
   }],
   components: {
     'problem-tag': problemTagComponent,
     'problem-stability': problemStabilityComponent
   },
   data () {
     return {
       loadingSelect: false
     }
   },
   methods: {
     withEllipsis (string) {
       const MAX_LENGTH = 32 // Max 3 lines of text, like other columns, etc.
       const SUFFIX = '...'

       if (!string) {
         return string
       }
       let trimmed = string.trim()
       if (trimmed.length > MAX_LENGTH - SUFFIX.length) {
         return trimmed.substr(0, MAX_LENGTH - SUFFIX.length).trim() + SUFFIX
       }
       return trimmed
     },
     select (problemId, problemName) {
       this.selectedProblemId = problemId
       let onStartCallback = () => {
         this.loadingSelect = true
       }
       let onDoneCallback = () => {
         this.loadingSelect = false
       }
       this.$emit('select', problemId, problemName, onStartCallback, onDoneCallback)
     }
   }
}
</script>
