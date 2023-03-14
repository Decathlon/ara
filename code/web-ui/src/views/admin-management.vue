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
    <div class="tableContent" v-if="projects.length">
      <h1 class="adminTitle">Projects management</h1>

      <div class="projectCTA">
        <Select class="filterSelect" placeholder="Filter" @on-change="filterProjects" :label-in-value="true">
          <Option v-for="(filter, index) in filterType" :value="index" :key="index" :label="filter" />
        </Select>
        <Input v-if="filterSelected === 'Name' || filterSelected === 'Created By'" class="filterSearch" v-model="searchElement" search placeholder="Enter something..." />
        <DatePicker v-else class="dateSelect" type="datetimerange" placement="bottom-right" placeholder="Select date range" format="yyyy-MM-dd HH:mm:ss" :start-date="new Date(2023, 1, 1)" @on-change="dateSelected" />
        <Button v-if="user.default_project" type="error" class="deleteBtn btn-group-right" @click="clearDefaultProject()" ghost>
          Clear default project
        </Button>
        <Button v-if="demoProjectNotFound" type="warning" class="demoProjectButton" data-nrt="createDemo" :loading="loadingDemoProject" @click="createDemoProject">Create the demo project</Button>
        <Button type="primary" :class="demoProjectNotFound ? 'addBtn btn-group-right' : 'addBtn align-right'" @click="openProjectCreationModal()">
          Add project
        </Button>
      </div>

      <table class="adminTable" aria-label="Current user projects scopes">
        <div v-if="showLoader" class="loading-overlay">
          <Icon type="ios-loading" class="ivu-anim-loop" size="32" />
        </div>
        <thead>
          <tr>
            <th></th>
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
          <tr v-for="(project, index) in searchProject" :class="index %2 !== 0 ? 'darkGrey' : 'lightGrey'" :key="project.id">
            <td>
              <Tooltip v-if="project.description" placement="right" :content="project.description">
                <Icon type="md-information-circle" size="24"/>
              </Tooltip>
            </td>
            <td class="project-name">
              <Icon v-if="isDemoProject(project.code)" class="demo-project-icon" type="md-flask" size="18" />
              {{ project.name }}
            </td>
            <td>{{ project.code }}</td>
            <td>{{ project.creation_date }}</td>
            <td>{{ project.creation_user }}</td>
            <td>{{ project.update_date }}</td>
            <td>{{ project.update_user }}</td>
            <td v-if="isScopedUser">{{ $t('role.' + project.currentUserRole) }}</td>
            <td>
              <input type="radio" name="defaultProject" @change="changeDefaultProject(project)" :checked="(user.default_project && user.default_project === project.code)">
            </td>
            <td class="table-cta" align="right">
              <Icon type="md-eye" size="24" @click="openProjectDetails(project)" :class="isDemoProject(project.code) ? 'hidden' : ''"/>
              <Icon type="md-create" size="24" @click="openProjectUpdateModal(project)" :class="isAdminOnProject(project) ? '' : 'hidden'"/>
              <Icon type="md-close-circle" size="24" @click="deleteProject(project.code)" :class="isAdminOnProject(project) ? '' : 'hidden'"/>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="no-values">
      <h2>No projects to display. Contact an admin or create a new project by clicking the button below.</h2>
      <Button type="primary" class="addBtn" @click="openProjectCreationModal()" >
        Add project
      </Button>
    </div>

    <Modal v-for="(modalConfiguration, index) in Object.entries(projectModalConfigurations)" :key="index"
           v-model="modalConfiguration[1].open"
           :title="modalConfiguration[1].display.title"
           :okText="modalConfiguration[1].display.okButton"
           :width="900"
           :loading="showLoader"
           :footer-hide="true">
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="128">
        <Form-item label="Code" prop="code">
          <Input v-model="formValidate.code" :disabled="modalConfiguration[0] === 'update'"/>
          <p class="hints">{{fieldDescription.code.hint}}</p>
          <p class="form-field-info-description">{{fieldDescription.code.warning}}</p>
        </Form-item>
        <Form-item label="Name" prop="name">
          <Input v-model="formValidate.name"/>
          <p class="hints">{{fieldDescription.name.hint}}</p>
          <p class="form-field-info-description">{{fieldDescription.name.warning}}</p>
        </Form-item>
        <Form-item :key="'description'" :label="'Description:'">
          <div>
            <Input v-model="formValidate.description" :type="'textarea'" :autosize="{ minRows: 2, maxRows: 15 }"/>
            <div class="hints">{{fieldDescription.description.hint}}</div>
          </div>
          <div class="form-field-info-description">{{fieldDescription.description.warning}}</div>
        </Form-item>
        <Form-item class="modal-cta">
          <Button @click="closeModal(modalConfiguration[0])">Cancel</Button>
          <Button type="primary" @click="handleSubmit(modalConfiguration[0])">Submit</Button>
        </Form-item>
      </Form>
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import { DEMO_PROJECT_CODE, USER } from '../libs/constants'
  import { AuthenticationService } from '../service/authentication.service'
  import { mapState } from 'vuex'

  export default {
    name: 'admin-management',

    data () {
      return {
        projects: [],
        loadingDemoProject: false,
        formValidate: {
          code: '',
          name: '',
          description: ''
        },
        ruleValidate: {
          code: { required: true, message: 'The code cannot be empty', trigger: 'blur' },
          name: { required: true, message: 'The name cannot be empty', trigger: 'blur' }
        },
        fieldDescription: {
          code: {
            hint: 'The technical code of the project, to use in ARA URLs (as well as API URLs used by continuous integration to push data to ARA). Eg. "phoenix-front".',
            warning: 'Choose the project code carefully because once created, this cannot be changed later. Also, this must be unique to each project.'
          },
          name: {
            hint: 'The name of the project, visible in the top-left combobox in ARA\'s header. E.g. "Phoenix - Front".',
            warning: 'Like the code field, this must also be unique.'
          },
          description: {
            hint: 'Explain briefly what your project is supposed to do.',
            warning: 'Write a short description of 512 characters max'
          }
        },
        projectModalConfigurations: {
          creation: {
            open: false,
            display: {
              title: 'Create Project',
              okButton: 'Create'
            }
          },
          update: {
            open: false,
            display: {
              title: 'Update Project',
              okButton: 'Update'
            }
          }
        },
        filterType: ['Name', 'Creation Date', 'Update Date', 'Created By'],
        filterSelected: 'Name',
        searchElement: '',
        showLoader: false,
        dateToSearch: []
      }
    },
    methods: {
      closeModal (form) {
        this.projectModalConfigurations[form].open = false
      },

      openProjectCreationModal () {
        this.projectModalConfigurations.creation.open = true
        this.resetProjectCreationForm()
      },

      resetProjectCreationForm () {
        this.projectModalConfigurations.creation.model = {}
      },

      create () {
        const projectToCreate = this.formValidate
        const projectCode = projectToCreate.code
        Vue.http
          .post(api.paths.projects, projectToCreate, api.REQUEST_OPTIONS)
          .then(async () => {
            this.showLoader = false
            this.closeModal('creation')
            this.$Message.success({
              content: `Project <b>${projectCode}</b> succesfully created!`,
              duration: 3,
              closable: true
            })
            await AuthenticationService.refreshUser()
              .then((refreshedUser) => {
                this.$store.dispatch('users/getUserInfo', refreshedUser)
                this.initProjects()
              })
          }, (error) => {
            this.showLoader = false
            api.handleError(error)
          })
      },

      openProjectUpdateModal (project) {
        this.projectModalConfigurations.update.open = true
        this.formValidate = { ...project }
      },

      update () {
        const projectToUpdate = this.formValidate
        const projectCode = projectToUpdate.code
        Vue.http
          .put(api.paths.projectByCode(projectCode), projectToUpdate, api.REQUEST_OPTIONS)
          .then(() => {
            this.showLoader = false
            this.closeModal('update')
            this.$Message.success({
              content: `Project <b>${projectCode}</b> succesfully updated!`,
              duration: 3,
              closable: true
            })
            this.initProjects()
          }, (error) => {
            this.showLoader = false
            api.handleError(error)
          })
      },

      openProjectDetails (projectInfo) {
        this.$router.push({ name: 'admin-project-details', params: { projectCode: projectInfo.code, projectName: projectInfo.name, userRole: this.user.profile } })
      },

      async initProjects () {
        await Vue.http
          .get(api.paths.projects, api.REQUEST_OPTIONS)
          .then((response) => {
            this.projects = response.body
            this.projects.forEach((project) => {
              project.currentUserRole = this.getRoleOnProject(project.code)
              if (project.creation_user === this.user.login) {
                project.creation_user = 'Me'
                project.update_user = 'Me'
              }
            })

            return this.projects
          })
      },

      isAdminOnProject (project) {
        return this.isSuperAdmin || (this.isScopedUser && (project.currentUserRole === USER.ROLE_ON_PROJECT.ADMIN))
      },

      getRoleOnProject (projectCode) {
        return this.user.scopes.find((scope) => scope.project === projectCode)?.role
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
            const isDemoProject = self.isDemoProject(code)
            if (isDemoProject) {
              self.$store.commit('teams/unloadTeams', { projectCode: DEMO_PROJECT_CODE })
            }
            const url = isDemoProject ? api.paths.demo : api.paths.projectByCode(code)
            Vue.http
              .delete(url, api.REQUEST_OPTIONS)
              .then(() => {
                self.$Modal.remove()
                self.showLoader = true
                setTimeout(() => {
                  self.showLoader = false
                  self.initProjects()
                }, 500)
              }, (error) => {
                self.showLoader = false
                api.handleError(error)
              })
          }
        })
      },

      isDemoProject (projectCode) {
        return projectCode === DEMO_PROJECT_CODE
      },

      createDemoProject () {
        this.loadingDemoProject = true
        Vue.http
          .post(api.paths.demo, null, api.REQUEST_OPTIONS)
          .then(() => {
            this.loadingDemoProject = false
            this.initProjects()
            this.$store.dispatch('teams/ensureTeamsLoaded', DEMO_PROJECT_CODE)
          }, (error) => {
            this.loadingDemoProject = false
            api.handleError(error)
          })
      },

      filterProjects (filter) {
        this.filterSelected = filter.label
      },

      dateSelected (dates) {
        this.dateToSearch = dates
      },

      handleSubmit (submit) {
        this.$refs['formValidate'][0].validate((valid) => {
          if (valid) {
            if (submit === 'creation') {
              this.create()
            } else {
              this.update()
            }
          }
        })
      }
    },
    mounted () {
      this.$store.dispatch('admin/setTypeSelected', '')
      this.$store.dispatch('users/getUserInfo', AuthenticationService.getDetails().user)
      this.initProjects()
    },
    computed: {
      ...mapState('users', ['user']),

      demoProjectNotFound () {
        return !this.projects.some((project) => project.code === DEMO_PROJECT_CODE)
      },

      isSuperAdmin () {
        return this.user?.profile === USER.PROFILE.SUPER_ADMIN
      },

      isScopedUser () {
        return this.user?.profile === USER.PROFILE.SCOPED_USER
      },

      searchProject () {
        switch (this.filterSelected) {
          case 'Name':
            return this.projects.filter(project => project.name.toLowerCase().includes(this.searchElement.toLowerCase()))
          case 'Creation Date':
            if (this.dateToSearch[0]) {
              return this.projects.filter(project => (Date.parse(project.creation_date) >= Date.parse(this.dateToSearch[0])) && (Date.parse(project.creation_date) <= Date.parse(this.dateToSearch[1])))
            } else {
              return this.projects
            }
          case 'Update Date':
            if (this.dateToSearch[0]) {
              return this.projects.filter(project => (Date.parse(project.update_date) >= Date.parse(this.dateToSearch[0])) && (Date.parse(project.update_date) <= Date.parse(this.dateToSearch[1])))
            } else {
              return this.projects
            }
          case 'Created By':
            return this.projects.filter(project => project.creation_user.toLowerCase().includes(this.searchElement.toLowerCase()))
        }
      }
    }
  }
</script>
