<template>
  <div>
    <h2 style="margin: 0 0 8px 18px;">PROJECTS</h2>
    <crud url="/api/projects"
          name="project"
          titleCaseName="Project"
          :introduction="introduction"
          :fields="fields"
          :disableDelete="true"
          v-on:loaded="projectsLoaded"
          ref="crud"/>

    <div v-if="loaded" style="margin-top: 16px; text-align: center;">
      <div v-if="projects.find(project => project.code === demoProjectCode)">
        <Button icon="md-trash" type="error" @click="deleteDemoProject">DELETE THE DEMO PROJECT</Button>
      </div>
      <div v-else>
        <Button :loading="loadingDemoProject" icon="md-add" @click="createDemoProject">CREATE THE DEMO PROJECT</Button>
        <div style="margin-top: 8px; color: lightgray;">You will be able to delete (and re-create) the demo project at any time.</div>
      </div>
    </div>

    <Alert v-if="justCreatedDemoProject" style="margin-top: 16px; text-align: center;" type="success">
      The demo project has been created: you can access its
      <router-link :to="{ name: 'executions', params: { projectCode: this.demoProjectCode } }">executions</router-link>.
    </Alert>
  </div>
</template>

<script>
  import Vue from 'vue'
  import { mapState } from 'vuex'

  import api from '../libs/api'

  import crudComponent from '../components/crud'

  export default {
    name: 'management-projects',

    components: {
      'crud': crudComponent
    },

    computed: {
      ...mapState('projects', [
        'projects',
        'loaded'
      ])
    },

    data () {
      return {
        demoProjectCode: 'the-demo-project',
        loadingDemoProject: false,
        justCreatedDemoProject: false,
        introduction: 'Projects are isolated areas in ARA to manage test reports of several applications or standalone components.',
        fields: [
          {
            code: 'id',
            type: 'hidden',
            newValue: -1,
            primaryKey: true
          },
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'string',
            required: true,
            createOnly: true,
            createOnlyBecause: 'the code ends-up in URLs of ARA, and people should be allowed to bookmark fixed URLs or copy/past them in other services (defect tracking system, wiki, etc.)',
            newValue: '',
            width: undefined,
            help: 'The technical code of the project, to use in ARA URLs (as well as API URLs used by continuous integration to push data to ARA). Eg. "phoenix-front".'
          },
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Project',
            type: 'string',
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined,
            help: 'The name of the project, visible in the top-left combobox in ARA\'s header. Eg. "Phoenix - Front".'
          },
          {
            code: 'defaultAtStartup',
            name: 'Default',
            columnTitle: 'Default',
            type: 'boolean',
            required: false,
            newValue: false,
            width: 96,
            help: '' +
              'Check to use that project as the default one when arriving at ARA homepage without any specified project. ' +
              'Only one project can be declared as the default.'
          }
        ]
      }
    },

    methods: {
      createDemoProject () {
        this.loadingDemoProject = true
        Vue.http
          .post(api.paths.demo(), null, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingDemoProject = false
            this.$refs.crud.load()
            this.justCreatedDemoProject = true
            this.$store.dispatch('teams/ensureTeamsLoaded', this.demoProjectCode)
          }, (error) => {
            this.loadingDemoProject = false
            api.handleError(error)
          })
      },

      deleteDemoProject () {
        let self = this
        this.$Modal.confirm({
          title: 'Delete the Demo Project',
          content: `<p>Delete the demo project?</p>`,
          okText: 'Delete',
          loading: true,
          onOk () {
            self.justCreatedDemoProject = false
            self.$store.commit('teams/unloadTeams', { projectCode: self.demoProjectCode })
            Vue.http
              .delete(api.paths.demo(), api.REQUEST_OPTIONS)
              .then((response) => {
                self.$Modal.remove()
                self.$refs.crud.load()
              }, (error) => {
                self.$Modal.remove()
                api.handleError(error)
              })
          }
        })
      },

      projectsLoaded (data) {
        this.$store.commit('projects/setProjects', data)
      }
    }
  }
</script>

