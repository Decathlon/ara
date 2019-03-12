<template>
</template>

<script>
  import { mapState } from 'vuex'

  export default {
    name: 'redirecter',

    computed: {
      ...mapState('projects', [
        'defaultProjectCode',
        'loaded'
      ]),

      isFramed () {
        return this.$route.matched.some(record => record.meta.isFramed)
      }
    },

    methods: {
      redirect () {
        if (!this.defaultProjectCode) {
          // No project: go to project management screen
          this.$router.replace({ name: 'management-projects' })
        } else if (this.$route.path === '/') {
          // Home screen => go to the default project's home screen
          this.$router.replace({ name: 'executions', params: { projectCode: this.defaultProjectCode } })
        } else {
          this.$router.replace({
            path: '/projects/' + this.defaultProjectCode + this.$route.path,
            params: this.$route.params,
            query: this.$route.query
          })
        }
      }
    },

    mounted () {
      if (this.loaded) {
        this.redirect()
      } else {
        this.$store.dispatch('projects/ensureProjectsLoaded')
      }
    },

    watch: {
      loaded () {
        this.redirect()
      }
    }
  }
</script>
