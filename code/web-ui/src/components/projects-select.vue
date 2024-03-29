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
  <!-- Do not blink the placeholder during the few milliseconds the project list is loading: only show the placeholder when project list is loaded and there is no project -->
  <Select v-model="selectedOption"
          style="width: 200px;"
          :disabled="!atLeastOneProject"
          :class="ghost ? 'ghost' : ''"
          @on-change="selectOption"
          :placeholder="loaded && atLeastOneProject ? 'Select Project' : 'No project to choose'">
    <Option v-for="project in projects" :value="project.code" :key="project.code"><Icon type="md-briefcase" style="visibility: hidden;"/> {{project.name}}</Option>
  </Select>
</template>

<script>
  import { mapState } from 'vuex'

  export default {
    name: 'projects-select',

    props: [ 'ghost' ],

    data () {
      let currentProjectCode = this.$route.params.projectCode
      return {
        /**
         * The current project code: always set to a valid project code when it is possible:
         * * Undefined when loaded the first screen without projectCode in URL (eg. on 'MANAGE PROJECTS LIST' view)
         * * Set to the URL's projectCode when provided at startup
         * * Set to the default project (or first one, if no default) when project list is loaded and still no current project
         * * Changed when user selects a new project code in the UI Select list
         */
        currentProjectCode,

        /**
         * The current option selected in the UI list: most of the time, it is in sync with currentProjectCode. Except:
         * * When clicking 'MANAGE PROJECT' or 'MANAGE PROJECTS LIST': its value is temporarily the value of that menu item code and
         * immediatly replaced by the current project code
         * * There is a bug in iView's Select component when loading projects list while the currentProjectCode is already defined:
         * the newly loaded project is not selected in the list. To fix this, we set the option to undefined and then immediatly
         * after that, we set again the current project code for the list to refresh correctly and select the correct option
         */
        selectedOption: currentProjectCode
      }
    },

    computed: {
      atLeastOneProject () {
        return this.projects && this.projects.length > 0
      },

      ...mapState('projects', [
        'projects',
        'loaded',
        'defaultProjectCode'
      ]),

      isFramed () {
        return this.$route.matched.some(record => record.meta.isFramed)
      }
    },

    methods: {
      selectOption (option) {
        if (option) {
          if (option !== this.currentProjectCode) {
            this.changeCurrentProject(option)
            if (this.$route.params.id) {
              // The screen was referring to an id of execution, error, problem...
              // which will not be valid on the other project.
              // So we redirect to the home screen of that new project
              this.$router.push({ name: 'executions', params: { projectCode: this.currentProjectCode } })
            } else {
              // Stay on the same screen, but for the new project
              this.$router.push({ params: { projectCode: this.currentProjectCode } })
            }
          }
        }
      },

      pushOrOpen (route) {
        if (this.isFramed) {
          window.open(this.$router.resolve(route).href, '_blank')
        } else {
          this.$router.push(route)
        }
      },

      changeCurrentProject (projectCode) {
        let oldProjectCode = this.currentProjectCode
        this.currentProjectCode = projectCode
        this.selectedOption = projectCode
        if (projectCode !== oldProjectCode) {
          this.$emit('projectSelection', projectCode)
        }
      }
    },

    mounted () {
      this.$store.dispatch('projects/ensureProjectsLoaded')
    },

    watch: {
      '$route' (to, from) {
        if (to.params.projectCode && to.params.projectCode !== from.params.projectCode) {
          this.$nextTick(() => { this.changeCurrentProject(to.params.projectCode) })
        }
      },

      projects () {
        // If the application loaded on a page without project (eg. 'MANAGE PROJECTS LIST'),
        // select the default project, or the first one if none is the default
        this.selectedOption = undefined

        let nextProjectCode = this.currentProjectCode
        if (!nextProjectCode || !this.projects.find(project => project.code === nextProjectCode)) {
          // No current project or the current project has just been deleted
          nextProjectCode = this.defaultProjectCode
        }

        this.$nextTick(() => { this.changeCurrentProject(nextProjectCode) })
      }
    }
  }
</script>

<style scoped>
  .ghost >>> .ivu-select-selection {
    background: transparent;
    border-color: white;
    color: white;
  }
  .ghost >>> .ivu-select-selection .ivu-select-arrow {
    color: white;
  }
  .ghost >>> .ivu-select-selection .ivu-select-placeholder {
    color: white;
    opacity: 0.66;
  }
  .ghost >>> .ivu-select-selection:hover {
    background-color: #2B85E4;
  }

  >>> .ivu-icon {
    margin-right: 4px;
  }
</style>
