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
    <div v-if="projects.length">
      <h1 class="adminTitle">Projects management</h1>

      <div class="projectCTA">
        <Select class="filterSelect" v-model="projectFilter" placeholder="Filter" @on-change="filterProjects" :label-in-value="true">
          <Option v-for="(filter, index) in filterType" :value="index" :key="index" :label="filter" />
        </Select>
        <Button v-if="user.default_project" type="error" class="deleteBtn" @click="clearDefaultProject()" ghost>
          Clear default project
        </Button>
        <Button v-if="!demoProjectNotFound" type="warning" class="demoProjectButton" data-nrt="createDemo" :loading="loadingDemoProject" @click="createDemoProject">Create the demo project</Button>
        <Button type="primary" class="addBtn" @click="add()">
          Add project
        </Button>
      </div>

      <table class="adminTable" aria-label="Current user projects scopes">
        <div v-if="showLoader" class="loading-overlay">
          <Icon type="ios-loading" class="ivu-anim-loop" size="32" />
        </div>
        <thead>
          <tr>
            <th>Name</th>
            <th>Code</th>
            <th>Created on</th>
            <th>Created by</th>
            <th>Updated on</th>
            <th>Updated by</th>
            <th v-if="isScopedUser">Role</th>
            <th>Default</th>
            <th></th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(project, index) in projects" :class="index %2 !== 0 ? 'darkGrey' : 'lightGrey'" :key="project.id">
            <td class="project-name">
              <Icon v-if="project.code === 'the-demo-project'" class="demo-project-icon" type="md-construct" size="18" />
              {{ project.name }}
            </td>
            <td>{{ project.code }}</td>
            <td>{{ project.creation_date }}</td>
            <td>{{ getProjectUserNameDisplay(project.creation_user) }}</td>
            <td>{{ project.update_date }}</td>
            <td>{{ getProjectUserNameDisplay(project.update_user) }}</td>
            <td v-if="isScopedUser">{{ getRoleOnProject(project.code) }}</td>
            <td>
              <input type="radio" name="defaultProject" @change="changeDefaultProject(project)" :checked="(user.default_project && user.default_project === project.code) || (projectByDefault === project.code) ">
            </td>
            <td>
              <Icon v-if="isAdminOnProject(project.code)" type="md-close-circle" size="24" @click="deleteProject(project.code)"/>
              <Icon type="md-eye" size="24" @click="openProjectDetails(project)" :class="isAdminOnProject(project.code) ? '' : 'spaced-icon'"/>
              <Icon type="md-create" size="24" @click="add(project)"/>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="no-project">
      <h2>No projects to display. Create a new project by clicking the button below.</h2>
      <Button type="primary" class="addBtn" @click="add()">
        Add project
      </Button>
    </div>

    <Modal v-model="addOrChangeProject" title="Add Project" okText="Add" @on-ok="save" @close="addOrChangeProject = false" :width="900"
        :loading="loadingSaving" ref="editPopup">
        <Form :label-width="128"
          v-if="fields.type !== 'hidden'">
          <Form-item v-for="field in fields"
            :key="field.code"
            :label="(field.type === 'boolean' ? '' : field.name + ':')"
            :required="field.required && (editingNew || (!field.primaryKey && !field.createOnly))">
            <form-field :field="field" v-model="editingData[field.code]" :editingNew="editingNew" :ref="field.code" v-on:enter="save"/>
            <div class="hints">
              {{field.help}}
            </div>
          </Form-item>
        </Form>
      </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import managementProjects from '../views/management-projects.vue'
  import formField from '../components/form-field'
  import { DEMO_PROJECT_CODE, USER } from '../libs/constants'
  import { AuthenticationService } from '../service/authentication.service'
  import { mapState } from 'vuex'

  export default {
    name: 'admin-management',
    components: {
      managementProjects,
      formField
    },
    data () {
      return {
        projects: [],
        addOrChangeProject: false,
        loadingDemoProject: false,
        fields: [
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'string',
            required: true,
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
            width: undefined,
            help: 'The name of the project, visible in the top-left combobox in ARA\'s header.' + 'Eg. "Phoenix - Front".'
          }
        ],
        editingData: {},
        filterType: ['Name', 'Creation Date', 'Update Date', 'Author'],
        editingNew: false,
        editing: false,
        showLoader: false
      }
    },
    methods: {
      newRowData () {
        let row = {}
        for (let field of this.fields) {
          row[field.code] = field.newValue
        }
        return row
      },
      add (project) {
        if (project) {
          this.editProject = true
        }
        this.addOrChangeProject = true
        this.doEdit(project.code ? project : this.newRowData(), true)
      },
      doEdit (row, editingNew) {
        this.editingData = { ...row }
        this.editingNew = editingNew
        this.editing = true
        this.$nextTick(() => this.$refs[this.firstVisibleFieldCode(editingNew)][0].focus())
      },
      firstVisibleFieldCode (editingNew) {
        for (let field of this.fields) {
          if (field.type !== 'hidden' && field.type !== 'select' && !field.readOnly && !field.readOnly && (editingNew || (!field.primaryKey && !field.createOnly))) { // Primary-key is read-only when editing
            return field.code
          }
        }
        throw new Error('The table ' + this.name + ' has no field, or they are all either of type hidden, selects, read-only or non-modifiable primary key')
      },
      save () {
        let row = { ...this.editingData }
        row.id = undefined
        if (this.editProject) {
          Vue.http
            .put('/api/projects', row, api.REQUEST_OPTIONS)
            .then(() => {
              this.getProjectList()
            }, (error) => {
              this.loadingSaving = false
              api.handleError(error, () => {
                this.loadingSaving = true
              })
            })
        } else {
          Vue.http
            .post('/api/projects', row, api.REQUEST_OPTIONS)
            .then(() => {
              this.getProjectList()
            }, (error) => {
              this.loadingSaving = false
              api.handleError(error, () => {
                this.loadingSaving = true
              })
            })
        }
      },
      openProjectDetails (projectInfo) {
        this.$router.push({ name: 'admin-project-details', query: { projectCode: projectInfo.code, projectName: projectInfo.name } })
      },
      async getProjectList () {
        await Vue.http
          .get('api/projects', api.REQUEST_OPTIONS)
          .then((response) => {
            this.projects = response.body
            return this.projects
          })
      },
      isAdminOnProject (projectCode) {
        return this.isSuperAdmin || (this.isScopedUser && (this.getRoleOnProject(projectCode) === USER.ROLE_ON_PROJECT.ADMIN))
      },
      getRoleOnProject (projectCode) {
        let userRole = ''

        this.user.scopes.filter((item) => { if (item.project === projectCode) { userRole = item.role } })

        return userRole
      },
      getProjectUserNameDisplay (login) {
        if (login === this.user.login) {
          return 'Me'
        }
        return login
      },
      clearDefaultProject () {
        Vue.http
          .delete(api.paths.currentUserDefaultProjectClear, api.REQUEST_OPTIONS)
          .then((response) => {
            const updatedUser = response.body
            this.$store.dispatch('users/updateDefaultProject', updatedUser)
            AuthenticationService.saveCurrentUser(updatedUser)
          }, (error) => {
            api.handleError(error)
          })
      },
      changeDefaultProject (project) {
        Vue.http
          .put(api.paths.currentUserDefaultProjectUpdate(project.code), null, api.REQUEST_OPTIONS)
          .then((response) => {
            const updatedUser = response.body
            this.$store.dispatch('users/getUserInfo', updatedUser)
            AuthenticationService.saveCurrentUser(updatedUser)
          }, (error) => {
            api.handleError(error)
          })
      },
      deleteProject (code) {
        let self = this
        this.$Modal.confirm({
          title: 'Delete a project',
          content: `<p>Do you really want to delete <strong>` + code + `</strong>?</p>`,
          okText: 'Delete',
          loading: true,
          onOk () {
            if (code === 'the-demo-project') {
              self.$store.commit('teams/unloadTeams', { projectCode: DEMO_PROJECT_CODE })
            }
            Vue.http
              .delete(code === 'the-demo-project' ? api.paths.demo : `api/projects/` + code, api.REQUEST_OPTIONS)
              .then(() => {
                self.$Modal.remove()
                self.showLoader = true
                setTimeout(() => {
                  self.showLoader = false
                  self.getProjectList()
                }, 500)
              }, (error) => {
                api.handleError(error)
              })
          }
        })
      },
      createDemoProject () {
        this.loadingDemoProject = true
        Vue.http
          .post(api.paths.demo, null, api.REQUEST_OPTIONS)
          .then(() => {
            this.loadingDemoProject = false
            this.getProjectList()
            this.$store.dispatch('teams/ensureTeamsLoaded', DEMO_PROJECT_CODE)
          }, (error) => {
            this.loadingDemoProject = false
            api.handleError(error)
          })
      },
      filterProjects (filter) {
        switch (filter.label) {
          case 'Name':
            return this.projects.sort((a, b) => a.code.localeCompare(b.code))
          case 'Creation Date':
            return this.projects.sort((a, b) => (new Date(b.creation_date)) - (new Date(a.creation_date)))
          case 'Update Date':
            return this.sortedProjectsByUpdate
          case 'Author':
            return this.sortedProjectsByAuthor
        }
      }
    },
    mounted () {
      this.$store.dispatch('admin/setTypeSelected', '')
      this.$store.dispatch('users/getUserInfo', JSON.parse(localStorage.getItem('current_user')))
      this.getProjectList()
    },
    computed: {
      ...mapState('users', ['user']),

      demoProjectNotFound () {
        return this.projects.some((project) => project.code === DEMO_PROJECT_CODE)
      },

      sortedDemoProject () {
        return this.projects.filter(item => item.code === DEMO_PROJECT_CODE)
      },

      isSuperAdmin () {
        return this.user?.profile === USER.PROFILE.SUPER_ADMIN
      },

      isScopedUser () {
        return this.user?.profile === USER.PROFILE.SCOPED_USER
      }
    }
  }
</script>
