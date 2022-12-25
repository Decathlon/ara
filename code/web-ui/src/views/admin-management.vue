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
    <h1 class="adminTitle">Projects management</h1>

    <div class="projectCTA">
     <Select class="filterSelect" v-model="projectFilter" clearable filterable placeholder="Filter">
        <Option v-for="(filters, index) in filterType" :value="index" :key="index" :label="filters" />
      </Select>
      <Button v-if="demoProjectNotFound" type="warning" class="demoProjectButton" data-nrt="createDemo" :loading="loadingDemoProject" @click="createDemoProject">CREATE THE DEMO PROJECT</Button>
      <Button v-else class="change-view" @click="switchView = !switchView" type="primary" ghost>{{ !switchView ? 'Show Demo Project' : 'Show Projects List' }}</Button>
      <Button type="primary" class="addBtn" @click="add()">
        Add project
      </Button>
    </div>

    <table v-if="switchView" class="adminTable" aria-label="Current user projects">
      <thead>
        <tr>
          <th>Name</th>
          <th>Creation date</th>
          <th>Update date</th>
          <th>Author</th>
          <th>Default</th>
          <th></th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="(project, index) in sortedDemoProject" :key="project.id">
          <td>{{ project.name }}</td>
          <td>Creation date</td>
          <td>Update date</td>
          <td>Created by</td>
          <td>
            <input type="radio" name="defaultProject" @change="changeDefaultProject(project)" :checked="project.defaultAtStartup === true">
          </td>
          <td>
            <Icon type="md-close-circle" size="24" @click="deleteDemoProject()"/>
            <Icon type="md-eye" size="24" @click="openProjectDetails(project)"/>
          </td>
        </tr>
      </tbody>
    </table>

    <table v-if="!switchView" class="adminTable" aria-label="Current user project scopes">
      <thead>
        <tr>
          <th>Name</th>
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
        <tr v-for="(project, index) in sortedProjects" :class="index %2 !== 0 ? 'darkGrey' : 'lightGrey'" :key="project.id">
          <td>{{ project.name }}</td>
          <td>{{ project.creation_date }}</td>
          <td>{{ getProjectUserNameDisplay(project.creation_user) }}</td>
          <td>{{ project.update_date }}</td>
          <td>{{ getProjectUserNameDisplay(project.update_user) }}</td>
          <td v-if="isScopedUser">{{ getRoleOnProject(project.code) }}</td>
          <td>
            <input type="radio" name="defaultProject" @change="changeDefaultProject(project)" :checked="project.defaultAtStartup === true">
          </td>
          <td>
            <Icon v-if="isAdminOnProject(project.code)" type="md-close-circle" size="24" @click="deleteProject(project.code)"/>
            <Icon type="md-eye" size="24" @click="openProjectDetails(project)"/>
          </td>
        </tr>
      </tbody>
    </table>
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
  import _ from 'lodash'
  import { DEMO_PROJECT_CODE, USER } from '../libs/constants'
  import { AuthenticationService } from '../service/authentication.service'
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
          },
          {
            code: 'defaultAtStartup',
            name: 'Default',
            columnTitle: 'Default',
            type: 'boolean',
            required: true,
            newValue: '',
            width: 96,
            help: 'Check to use that project as the default one when arriving at ARA homepage without any specified project. ' +
              'Only one project can be declared as the default.'
          }
        ],
        editingData: {},
        filterType: ['Name', 'Creation Date', 'Update Date', 'Author'],
        editingNew: false,
        editing: false,
        switchView: false
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
      add () {
        this.addOrChangeProject = true
        this.doEdit(this.newRowData(), true)
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
        Vue.http
          .post('/api/projects', row, api.REQUEST_OPTIONS)
          .then((response) => {
            const newProject = response.body
            Vue.http
              .get('/api/projects/' + newProject.code + '/members/users')
              .then((response) => {
                newProject.members = response.body
                this.projects.push(newProject)
              })
            this.$refs.editPopup.close()
          }, (error) => {
            this.loadingSaving = false
            api.handleError(error, () => {
              this.loadingSaving = true
            })
          })
      },
      openProjectDetails (projectInfo) {
        this.$router.push({ name: 'admin-project-details', path: '/projectDetails/', query: { projectCode: projectInfo.code, projectName: projectInfo.name } })
      },
      getProjectList () {
        Vue.http
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
        return _(this.userDetails.scopes).filter({ 'project': projectCode }).map('role').first()
      },
      getProjectUserNameDisplay (login) {
        if (login === this.userDetails.login) {
          return 'Me'
        }
        return login
      },
      changeDefaultProject (project) {
        const defaultInfo = {
          'id': project.id,
          'code': project.code,
          'name': project.name,
          'defaultAtStartup': true
        }
        Vue.http
          .put('api/projects/' + project.code, defaultInfo, api.REQUEST_OPTIONS)
      },
      deleteDemoProject () {
        let self = this
        this.$Modal.confirm({
          title: 'Delete the Demo Project',
          content: `<p>Do you really want to delete the demo project?</p>`,
          okText: 'Delete',
          loading: true,
          onOk () {
            self.$store.commit('teams/unloadTeams', { projectCode: DEMO_PROJECT_CODE })
            Vue.http
              .delete(api.paths.demo(), api.REQUEST_OPTIONS)
              .then(() => {
                self.$Modal.remove()
                self.getProjectList()
                self.switchView = false
              }, (error) => {
                api.handleError(error)
              })
          }
        })
      },
      deleteProject (code) {
        Vue.http
          .delete('api/projects/' + code, api.REQUEST_OPTIONS)
          .then(() => {
            this.getProjectList()
          }, (error) => {
            api.handleError(error)
          })
      },
      createDemoProject () {
        this.loadingDemoProject = true
        Vue.http
          .post(api.paths.demo(), null, api.REQUEST_OPTIONS)
          .then(() => {
            this.loadingDemoProject = false
            this.getProjectList()
            this.$store.dispatch('teams/ensureTeamsLoaded', DEMO_PROJECT_CODE)
          }, (error) => {
            this.loadingDemoProject = false
            api.handleError(error)
          })
      }
    },
    mounted () {
      this.getProjectList()
    },
    computed: {
      demoProjectNotFound () {
        return !_(this.projects).some({ 'code': DEMO_PROJECT_CODE })
      },

      sortedProjects () {
        return this.projects.map(item => item).sort((a, b) => a.code - b.code).filter(project => project.code !== DEMO_PROJECT_CODE)
      },

      sortedDemoProject () {
        return this.projects.filter(item => item.code === DEMO_PROJECT_CODE)
      },

      userDetails () {
        return AuthenticationService.getDetails().user
      },

      isSuperAdmin () {
        return this.userDetails?.profile === USER.PROFILE.SUPER_ADMIN
      },

      isScopedUser () {
        return this.userDetails?.profile === USER.PROFILE.SCOPED_USER
      }
    }
  }
</script>
