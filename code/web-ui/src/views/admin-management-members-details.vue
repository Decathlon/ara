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
    <span class="breadcrumbLink" @click="$router.go(-1)">Members list</span>

    <h1>Members management</h1>

    <div class="tableContent groupManagement">
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
          <p>Group member(s)</p>
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
            <td>There is no members in this group</td>
          </tr>
        </tbody>

        <button class="addMember" @click="memberToGroup = true">
          <Icon type="md-add" size="24"/>
        </button>
      </table>
    </div>

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
  import { mapState } from 'vuex'

  export default {
    name: 'admin-management-members',

    components: {
      formField
    },

    data () {
      return {
        members: [],
        groups: [],
        memberToGroup: false,
        groupFields: [
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Name',
            type: 'autocomplete',
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined
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
        groupName: this.$route.query.groupName,
        groupMembers: []
      }
    },

    methods: {
      showGroupInfo (name) {
        Vue.http
          .get('/api/groups/' + name + '/members')
          .then((response) => {
            this.groupMembers = response.body
          })
      },

      addMemberToGroup () {
        Vue.http
          .post('/api/groups/' + this.groupName + '/members', this.editingData, api.REQUEST_OPTIONS)
          .then(setTimeout(() => this.showGroupInfo(this.groupName), 100))
      },

      removeGroupMember (groupMemberInfo) {
        if (confirm('Do you really want to delete this group member?')) {
          Vue.http
            .delete('/api/groups/' + this.groupName + '/members/' + groupMemberInfo.name, api.REQUEST_OPTIONS)
            .then(setTimeout(() => this.showGroupInfo(this.groupName), 100))
        }
      }
    },

    created () {
      this.showGroupInfo(this.groupName)
    },

    computed: {
      sortedMembers () {
        return this.members.map(item => item).sort((a, b) => a.id - b.id)
      },

      ...mapState('users', ['getAllUsers'])
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
