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
    <h1>Projects management</h1>

    <table>
      <thead>
        <tr>
          <th>Code</th>
          <th>Name</th>
          <th>Member(s)</th>
          <th>Default</th>
          <th></th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="project in sortedProjects" :class="project.id %2 !== 0 ? 'darkGrey' : 'lightGrey'" :key="project.id">
          <td>{{ project.code }}</td>
          <td>{{ project.name }}</td>
          <td v-for="member in project.members" :key="member.name">
            {{ member.name }}
          </td>
          <td>
            <input type="radio" name="defautProject" @change="changeDefaultProject(project)" :checked="project.defaultAtStartup === true ? true : false">
          </td>
          <td>
            <Icon type="md-eye" size="24" @click="openProjectDetails(project)"/>
          </td>
        </tr>
      </tbody>
      <button class="addProject" @click="add">
        <Icon type="md-add" size="24"/>
      </button>
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
  import manangementProjects from '../views/management-projects.vue'
  import formField from '../components/form-field'

  export default {
    name: 'admin-management',

    components: {
      manangementProjects,
      formField
    },

    data () {
      return {
        projects: [],
        addOrChangeProject: false,
        introduction: 'Projects are isolated areas in ARA to manage test reports of several applications or standalone components.',
        fields: [
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'string',
            required: true,
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
            required: true,
            newValue: '',
            width: 96,
            help: '' +
              'Check to use that project as the default one when arriving at ARA homepage without any specified project. ' +
              'Only one project can be declared as the default.'
          }
        ],
        editingData: {},
        editingNew: false,
        editing: false
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
        this.$router.push({ name: 'admin-project-details', params: { projectInfo } })
      },

      getProjectList () {
        Vue.http
          .get('api/projects', api.REQUEST_OPTIONS)
          .then((response) => {
            const project = response.body

            for (let i = 0; i < project.length; i++) {
              Vue.http
                .get('/api/projects/' + project[i].code + '/members/users')
                .then((response) => {
                  project[i].members = response.body
                  this.projects = Array.from(project)
                })
            }
            return this.projects
          })
      },

      changeDefaultProject (project) {
        const defaultInfo = {
          'code': project.code,
          'name': project.name,
          'defaultAtStartup': true
        }

        Vue.http
          .put('api/projects/' + project.code, defaultInfo, api.REQUEST_OPTIONS)
      }
    },

    mounted () {
      this.getProjectList()
    },

    computed: {
      sortedProjects () {
        return this.projects.map(item => item).sort((a, b) => a.id - b.id)
      }
    }
  }
</script>

<style scoped>
  h1 {
    text-align: center;
    font-weight: bold;
    margin-top: 3rem;
  }

  h2 {
    text-align: left;
    top: 0;
    margin: 0 0 2rem;
  } 

  table {
    width: 90%;
    margin: 3rem auto;
    border-collapse: collapse;
    position: relative;
  }

  thead tr {
    background-color: #3880BE;
  }

  thead tr th {
    padding: 10px;
    text-align: center;
    color: #ffffff;
  }

  thead tr th:first-child {
    border-radius: 10px 0 0 0;
  }

  thead tr th:last-child {
    border-radius: 0 10px 0 0;
  }

  tbody {
    text-align: center;
    font-weight: bold;
  }

  tbody tr td {
    padding: 10px;
  }

  tbody tr td i {
    color: #AC8DAF;
    float: right;
    margin-right: 1rem;
  }

  i {
    cursor: pointer;
  }

  .addProject {
    position: absolute;
    top: -20px;
    right: 20px;
    background-color: #ffffff;
    padding: 8px;
    border-radius: 100px;
    border: 2px solid #3780be;
    color: #ff5600;
  }
</style>
