<template>
  <Breadcrumb class="breadcrumbLink">
    <BreadcrumbItem to="/">Home</BreadcrumbItem>
    <BreadcrumbItem v-if="getBack.path" :to="getBack.path">{{ getBack.meta.title }}</BreadcrumbItem>
    <BreadcrumbItem v-if="actualPath.path">{{ actualPath.meta.title }}</BreadcrumbItem>
  </Breadcrumb>
</template>

<script>
  export default {
    data () {
      return {
        previousPath: [],
        actualPath: []
      }
    },

    computed: {
      getBack () {
        if (!this.actualPath.path?.includes(this.previousPath.name)) {
          this.previousPath = []
        }

        return this.previousPath
      }
    },

    watch: {
      '$route' (to, from) {
        this.actualPath = to
        if (from.name) {
          this.previousPath = from
        }
      }
    }
  }
</script>
