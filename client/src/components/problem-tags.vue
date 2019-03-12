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
