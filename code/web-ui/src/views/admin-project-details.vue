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
    <div class="projectHeader">
      <div>
        <h1 class="adminTitle">
          {{ projectName }}
          <p class="projectCode ivu-text-center"><strong>{{ projectCode }}</strong></p>
        </h1>
      </div>

      <Button title="Edit" class="editBtn" type="primary" ghost>Edit project</Button>
      <Button title="Delete project" class="removeBtn" type="error">Remove project</Button>
    </div>

    <div class="tableContent">
      <span class="breadcrumbLink" @click="$router.go(-1)">
        <Icon type="md-home" />
        Project list
      </span>

      <div class="projectMemberFilter">
        <h3>Project's members</h3>
        <Button type="primary" shape="circle" icon="md-checkmark">Users</Button>
        <Button type="primary" shape="circle" icon="md-checkmark">Groups</Button>
      </div>

      <table class="adminTable" aria-label="Project's members name and role">
        <thead>
          <tr>
            <th>Name</th>
            <th>Role</th>
            <th></th>
          </tr>
        </thead>

        <tbody v-if="projectInfo">
          <tr v-for="(member, index) in projectInfo" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
            <td >{{ member.name }}</td>
            <td>{{ member.role }}</td>
            <td>
              <Icon type="md-close-circle" class="crossIcon" size="24" @click="removeUserFromProject()"/>
              <Icon type="md-create" size="24" @click="openProjectDetails(project)"/>
            </td>
          </tr>
        </tbody>
        <button class="addBtn" @click="addMember">
          <Icon type="md-add" size="24"/>
        </button>
      </table>

      <Button class="saveProjectChange" type="primary" :disabled="!changesTab">Save changes</Button>
    </div>

    <Modal v-model="memberToAdd" title="Add Member" okText="Add" :footer-hide="!memberTypeSelected" @on-ok="addMemberToProject" @close="memberToAdd = false" :width="900"
      :loading="loadingSaving" ref="editPopup">
      <Form v-if="!memberTypeSelected" :label-width="128">
        <div class="memberType">
          <span @click="memberTypeSelected = 'Users'">Users</span>
          <span @click="memberTypeSelected = 'Groups'">Groups</span>
        </div>
      </Form>
      <Form v-else :label-width="128">
        <Form-item v-for="field in fields"
                   :key="field.code"
                   :label="(field.type === 'boolean' ? '' : field.name + ':')"
                   :required="field.required && (editingNew || (!field.primaryKey && !field.createOnly))">
          <form-field :field="field" v-model="editingData[field.code]" :editingNew="editingNew" :ref="field.code" v-on:enter="addMemberToProject(memberTypeSelected)"/>
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
        projectName: '',
        projectCode: '',
        memberToAdd: false,
        fields: [
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Name',
            type: 'autocomplete',
            required: true,
            createOnly: true,
            createOnlyBecause: 'the code ends-up in URLs of ARA, and people should be allowed to bookmark fixed URLs or copy/past them in other services (defect tracking system, wiki, etc.)',
            newValue: '',
            width: undefined,
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
            width: undefined
          }
        ],
        editingData: {},
        editingNew: false,
        editing: false,
        memberTypeSelected: ''
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
        if (this.memberTypeSelected) {
          this.$nextTick(() => this.$refs[this.firstVisibleFieldCode(editingNew)][0].focus())
        }
      },
      firstVisibleFieldCode (editingNew) {
        for (let field of this.fields) {
          if (field.type !== 'hidden' && field.type !== 'select' && !field.readOnly && !field.readOnly && (editingNew)) {
            return field.code
          }
        }
        throw new Error('The table ' + this.name + ' has no field, or they are all either of type hidden, selects, read-only or non-modifiable primary key')
      },
      addMemberToProject (memberType) {
        if (memberType === 'Users') {
          Vue.http
            .post('/api/projects/' + this.projectCode + '/members/users', this.editingData, api.REQUEST_OPTIONS)
        } else {
          Vue.http
            .post('/api/projects/' + this.projectCode + '/members/groups', this.editingData, api.REQUEST_OPTIONS)
        }
      }
    },
    mounted () {
      this.projectName = this.$route.query.projectName
      this.projectCode = this.$route.query.projectCode
      Vue.http
        .get('/api/projects/' + this.projectCode + '/members/users')
        .then((response) => {
          for (let user of response.body) {
            this.projectInfo.push({
              name: user.name,
              role: user.role
            })
          }
        })
    },
    beforeDestroy () {
      this.projectInfo = this.$route.params
    }
  }
</script>
