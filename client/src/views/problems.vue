<template>
  <problems :filtersInQueryString="true"/>
</template>

<script>
  import problemsComponent from '../components/problems'

  export default {
    name: 'problems-view',
    components: {
      'problems': problemsComponent
    }
  }
</script>
