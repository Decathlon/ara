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
    <strong v-if="problem.blamedTeam"
            :class="'blamedTeam' + (isFiltered ? ' filtered' : '')"
            :title="(isFiltered ? 'Filtering on this team: the team has this problem assigned' : '')">
      <Icon v-if="isFiltered" type="ios-funnel"/>
      {{problem.blamedTeam.name}}:
    </strong>
    {{problem.name}}
    <span v-if="problem.effectiveStatus !== 'OPEN'"
          style="background-color: white; color: #2c3e50; margin-left: 2px; padding: 0 2px; border-radius: 100px; white-space: nowrap;"
          ><Icon type="md-close-circle" style=" vertical-align: -1px;"/>
      {{problem.effectiveStatus}}
    </span>
    <span v-if="problem.defectId" :class="'defectId' + (problem.defectUrl && problem.defectExistence === 'NONEXISTENT' ? ' NONEXISTENT' : '')" title="Work item">
      <span v-if="problem.defectUrl && problem.defectExistence === 'NONEXISTENT'">
        <Icon type="md-warning"/>
        Nonexitent:
      </span>
      #{{problem.defectId}}
    </span>
  </div>
</template>

<script>
  export default {
    name: 'problem-tag-content',

    props: [ 'problem', 'filteredTeamId' ],

    computed: {
      isFiltered () {
        return this.filteredTeamId === this.problem.blamedTeam.id
      }
    }
  }
</script>

<style scoped>
  .filtered {
    background-color: yellow !important;
    color: black !important;
    border-radius: 3px 0 0 3px;
  }
</style>
