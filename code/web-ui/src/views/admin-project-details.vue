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
    <h1>Projects details</h1>

    <div class="tableContent">
      <h2>{{ projectInfo.name }}</h2>

      <table>
        <thead>
          <tr>
            <th>Members</th>
            <th>Role</th>
            <th></th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="member in projectInfo.members" :key="member.name" :class="member.name %2 !== 0 ? 'darkGrey' : 'lightGrey'">
            <td >{{ member.name }}</td>
            <td>{{ member.role }}</td>
            <td>
              <Icon type="md-close-circle" size="24" @click="openProjectDetails(project)"/>
              <Icon type="md-create" size="24" @click="openProjectDetails(project)"/>
            </td>
          </tr>
        </tbody>
        <button class="addMember" @click="addMember">
          <Icon type="md-add" size="24"/>
        </button>
      </table>
    </div>

    <Modal v-model="memberToAdd" title="Add Member" okText="Add" @on-ok="createMember" @close="memberToAdd = false" :width="900"
      :loading="loadingSaving" ref="editPopup">
      <Form :label-width="128">
        <Form-item v-for="field in fields"
                   :key="field.code"
                   :label="(field.type === 'boolean' ? '' : field.name + ':')"
                   :required="field.required && (editingNew || (!field.primaryKey && !field.createOnly))">
          <form-field :field="field" v-model="editingData[field.code]" :editingNew="editingNew" :ref="field.code" v-on:enter="createMember"/>
        </Form-item>
      </Form>
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import formField from '../components/form-field'

  export default {
    name: 'admin-project-details',

    components: {
      formField
    },

    data () {
      return {
        projectInfo: [],
        currentMember: [],
        newMember: {
          name: '',
          role: ''
        },
        memberToAdd: false,
        fields: [
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Name',
            type: 'string',
            required: true,
            createOnly: true,
            createOnlyBecause: 'the code ends-up in URLs of ARA, and people should be allowed to bookmark fixed URLs or copy/past them in other services (defect tracking system, wiki, etc.)',
            newValue: '',
            width: undefined,
            help: 'The technical code of the project, to use in ARA URLs (as well as API URLs used by continuous integration to push data to ARA). Eg. "phoenix-front".',
            primaryKey: true
          },
          {
            code: 'role',
            name: 'Role',
            columnTitle: 'Role',
            type: 'select',
            options: [
              { value: 'MEMBER', label: 'Member' },
              { value: 'MAINTAINER', label: 'Maintainer' },
              { value: 'ADMIN', label: 'Admin' }
            ],
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined,
            help: 'The name of the project, visible in the top-left combobox in ARA\'s header. Eg. "Phoenix - Front".'
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

      addMember () {
        this.memberToAdd = true
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
          if (field.type !== 'hidden' && field.type !== 'select' && !field.readOnly && !field.readOnly && (editingNew)) {
            return field.code
          }
        }
        throw new Error('The table ' + this.name + ' has no field, or they are all either of type hidden, selects, read-only or non-modifiable primary key')
      },

      createMember () {
        console.log(this)
        let row = {
          name: 'MEMBER 1',
          role: 'MEMBER'
        }
        Vue.http
          .post('/api/projects/' + this.projectInfo.code + '/members/users', row, api.REQUEST_OPTIONS)
          .then((response) => {
            console.log(response)
          })
      }
    },

    created () {
      this.projectInfo = this.$route.params.projectInfo
      console.log(this.projectInfo)
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
    margin: 1rem auto;
    width: 90%;
  } 

  table {
    width: 90%;
    margin: 0 auto;
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
    float: right;
    margin-right: 1rem;
  }

  i {
    cursor: pointer;
  }

  .ivu-icon-md-close-circle {
    color: rgb(188, 188, 188);
  }

  .ivu-icon-md-create {
    color: #AC8DAF;
  }

  .tableContent {
    margin-top: 3rem;
  }

  .addMember {
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
