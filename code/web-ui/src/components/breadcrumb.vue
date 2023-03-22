<template>
  <Breadcrumb v-if="savedSingleUserConnections" class="breadcrumbLink">
    <BreadcrumbItem to="/">Home</BreadcrumbItem>
    <BreadcrumbItem v-if="previousPath.path" :to="previousPath.path">{{ previousPath.meta.title }}</BreadcrumbItem>
    <BreadcrumbItem v-if="actualPath.path">{{ actualPath.meta.title }}</BreadcrumbItem>
  </Breadcrumb>
</template>

<script>
  import { mapState } from 'vuex'

  export default {
    data () {
      return {
        previousPath: localStorage.getItem('previousPath'),
        actualPath: []
      }
    },

    computed: {
      ...mapState('admin', ['savedSingleUserConnections']),

      prevPath () {
        const backupPath = JSON.parse(localStorage.getItem('previousPath'))
        return backupPath
      }
    },

    mounted () {
      this.previousPath = JSON.parse(localStorage.getItem('previousPath'))
    },

    watch: {
      '$route' (to, from) {
        this.actualPath = to
        if (from.name && !from.path.includes('settings')) {
          this.previousPath = from
          localStorage.setItem('previousPath', JSON.stringify(from))
        }

        if (!this.actualPath.path?.includes(this.previousPath.name)) {
          this.previousPath = []
        }
      }
    }
  }
</script>
