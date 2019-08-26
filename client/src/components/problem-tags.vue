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
  <span v-if="problems">
    <problem-tag v-for="problem in problems" :key="problem.id" :problem="problem" :filteredTeamId="filteredTeamId"
                 :link="true" :scenarioHandling="scenarioHandling"/>
  </span>
</template>

<script>
  import problemComponent from '../components/problem-tag'

  export default {
    name: 'problem-tags',
    props: ['problems', 'filteredTeamId', 'scenarioHandling'],
    components: {
      'problem-tag': problemComponent
    }
  }
</script>
