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
