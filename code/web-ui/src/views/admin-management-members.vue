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
    <h1>Members management</h1>

    <div v-if="!showGroup" class="tableContent">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Users</th>
            <th></th>
          </tr>
        </thead>

        <tbody v-if="members">
          <tr v-for="member in sortedMembers" :key="member">
            <td>{{ member.name }}</td>
            <td>{{ member.users ? member.users : '' }}</td>
            <td>
              <Icon type="md-close-circle" size="24" @click="removeGroup(member)"/>
              <Icon type="md-create" size="24" @click="showGroupInfo(member.name)"/>
            </td>
          </tr>
        </tbody>
        <button class="addMember" @click="memberToAdd = true">
          <Icon type="md-add" size="24"/>
        </button>
      </table>
    </div>

    <div v-if="showGroup" class="tableContent groupManagement">
      <h2>{{ groupName }}</h2>
      <table>
        <div class="tabTitle">
          <p>Identity</p>
        </div>
        <tbody>
          <tr>
            <td>
              <span>Name</span>
              <input type="text" :value="groupName">
            </td>
          </tr>
        </tbody>
      </table>

      <table>
        <div class="tabTitle">
          <p>Users</p>
        </div>
        <tbody v-if="groupMembers.length > 0">
          <tr v-for="member in groupMembers" :key="member">
            <td>{{ member.name }}</td>
            <td>{{ member.role }}</td>
            <td>
              <Icon type="md-close-circle" size="24" @click="removeGroupMember(member)"/>
              <Icon type="md-create" size="24"/>
            </td>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td class="alert-msg">No member(s) to display</td>
          </tr>
        </tbody>
        <button class="addMember" @click="memberToGroup = true">
          <Icon type="md-add" size="24"/>
        </button>
      </table>
    </div>

    <Modal v-model="memberToAdd" title="Add Group" okText="Add" @on-ok="createMember" @close="memberToAdd = false" :width="900"
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

    <Modal v-model="memberToGroup" title="Add Member" okText="Add" @on-ok="addMemberToGroup" @close="memberToGroup = false" :width="900"
      :loading="loadingSaving" ref="editPopup">
      <Form :label-width="128">
        <Form-item v-for="field in groupFields"
                   :key="field.code"
                   :label="(field.type === 'boolean' ? '' : field.name + ':')"
                   :required="field.required && (editingNew || (!field.primaryKey && !field.createOnly))">
          <form-field :field="field" v-model="editingData[field.code]" :editingNew="editingNew" :ref="field.code" v-on:enter="addMemberToGroup"/>
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
    name: 'admin-management-members',

    components: {
      formField
    },

    data () {
      return {
        members: [],
        groups: [],
        groupDetails: [],
        memberToAdd: false,
        memberToGroup: false,
        fields: [
          {
            code: 'name',
            name: 'Group name',
            columnTitle: 'Name',
            type: 'string',
            required: true,
            newValue: '',
            width: undefined,
            businessKey: true
          }
        ],
        groupFields: [
          {
            code: 'name',
            name: 'Member name',
            columnTitle: 'Name',
            type: 'string',
            required: true,
            newValue: '',
            width: undefined,
            businessKey: true,
            help: 'Type a name of a user that already exist'
          },
          {
            code: 'role',
            name: 'Role',
            columnTitle: 'Role',
            type: 'select',
            options: [
              { value: 'MEMBER', label: 'Member' },
              { value: 'MAINTAINER', label: 'Maintainer' }
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
        showGroup: false,
        groupName: '',
        groupMembers: []
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

      showGroupInfo (name) {
        Vue.http
          .get('/api/groups/' + name + '/members')
          .then((response) => {
            this.groupMembers = response.body
          })
        this.showGroup = !this.showGroup
        this.groupName = name
      },

      createMember () {
        Vue.http
          .post('/api/groups', this.editingData, api.REQUEST_OPTIONS)
          .then(setTimeout(() => this.getMember(), 50))
      },

      async getMember () {
        this.members = []
        await Vue.http
          .get('api/groups', api.REQUEST_OPTIONS)
          .then((groupList) => {
            if (groupList.body.length > 0) {
              for (let i = 0; i < groupList.body.length; i++) {
                this.members.push({
                  'name': groupList.body[i].name,
                  'users': ''
                })
                Vue.http
                  .get('/api/groups/' + groupList.body[i].name + '/members')
                  .then((response) => {
                    for (let j = 0; j < response.body.length; j++) {
                      this.members[i].users = response.body[j].name
                    }
                  })
              }
            }

            return this.members
          })

        Vue.http
          .get('api/users', api.REQUEST_OPTIONS)
          .then((response) => {
            this.members.push(response.body[0])

            return { ...this.members }
          })
      },

      addMemberToGroup () {
        Vue.http
          .post('/api/groups/' + this.groupName + '/members', JSON.parse(JSON.stringify(this.editingData)), api.REQUEST_OPTIONS)
      },

      removeGroup (memberInfo) {
        if (confirm('Do you really want to delete this member?')) {
          Vue.http
            .delete('/api/groups/' + memberInfo.name, api.REQUEST_OPTIONS)
            .then(setTimeout(() => this.getMember(), 50))
        }
      },

      removeGroupMember (groupMemberInfo) {
        if (confirm('Do you really want to delete this group member?')) {
          Vue.http
            .delete('/api/groups/' + this.groupName + '/members/' + groupMemberInfo.name, api.REQUEST_OPTIONS)
            .then(setTimeout(() => this.showGroupInfo(), 50))
        }
      }
    },

    created () {
      this.getMember()
    },

    computed: {
      sortedMembers () {
        return this.members.map(item => item).sort((a, b) => a.id - b.id)
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

  .groupManagement table {
    margin: 3rem auto;
    position: relative;
  }

  .tabTitle {
    position: absolute;
    top: -28px;
    background-color: #ffffff;
    padding: 5px 15px;
    border-radius: 5px 5px 0 0;
    font-weight: 900;
  }

  .groupManagement tbody {
    background-color: #ffffff;
    box-shadow: 1px 1px 1px 1px #cfcfcf;
    text-align: left;
  }

  .groupManagement tbody td {
    padding: 34px 25px;
  }

  .groupManagement tbody td span {
    display: flex;
  }

  .groupManagement tbody td input {
    padding: 8px 15px;
    border: 1px solid #d8d8d8;
    border-radius: 5px;
  }

  .groupManagement .addMember {
    background-color: #ff7d00;
    border: none;
    color: #ffffff;
  }
</style>
