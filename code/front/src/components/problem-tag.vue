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
  <router-link v-if="link" :class="'problem ' + problem.effectiveStatus" :to="{ name: 'problem', params: { id: problem.id }}" :title="title"
               :style="reappearedOnHandledScenario ? 'opacity: 0.5;' : ''">
    <problem-tag-content :problem="problem" :filteredTeamId="filteredTeamId" :scenarioHandling="scenarioHandling"/>
  </router-link>
  <div v-else :class="'problem ' + problem.effectiveStatus" :title="title">
    <problem-tag-content :problem="problem" :filteredTeamId="filteredTeamId" :scenarioHandling="scenarioHandling"/>
  </div>
</template>

<script>
  import util from '../libs/util'

  import problemTagContentComponent from '../components/problem-tag-content'

  export default {
    name: 'problem-tag',

    props: [ 'problem', 'link', 'filteredTeamId', 'scenarioHandling' ],

    components: {
      'problem-tag-content': problemTagContentComponent
    },

    computed: {
      reappearedOnHandledScenario () {
        return this.scenarioHandling === 'HANDLED' && this.problem.effectiveStatus === 'REAPPEARED'
      },

      title () {
        if (this.problem.effectiveStatus === 'OPEN') {
          return 'Open problem'
        } else {
          return 'Problem closed on ' + util.formatDate(this.problem.closingDateTime) +
            (this.problem.effectiveStatus === 'REAPPEARED' ? ' but reappeared afterward' : '') +
            (this.reappearedOnHandledScenario ? ' - Not mandatory: another error is handled by a problem for this scenario' : '')
        }
      }
    }
  }
</script>
