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
    <div>
      <h1 class="adminTitle">{{ getMemberInfo.login }}'s projects</h1>
    </div>

    <div>
      <div v-if="currentProfile.profile === 'SUPER_ADMIN'" class="projectCTA">
        <Button type="primary" class="addBtn" @click="add()">
          Affect to a new project
        </Button>
      </div>
      <table class="adminTable" aria-label="User's project and his role for each of them">
        <tbody v-if="members">
          <tr v-for="(scope, index) in getMemberInfo.scopes" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
            <td class="userType">
              {{ scope.project }}
            </td>

            <td class="userType">
              <ul class="user-project-roles">
                <li class="user-role-chip" 
                    @click="changeProfile({ member: getMemberInfo, project: scope.project, role: 'MEMBER'})" 
                    :class="scope.role === 'MEMBER' ? ' active' : ''">
                    <span v-if="scope.role === 'MEMBER'"><Icon type="md-checkmark" /></span>
                    Member
                </li>
                <li class="user-role-chip"
                    @click="changeProfile({ member: getMemberInfo, project: scope.project, role: 'MAINTAINER'})"
                    :class="scope.role === 'MAINTAINER' ? ' active' : ''">
                    <span v-if="scope.role === 'MAINTAINER'"><Icon type="md-checkmark" /></span>
                    Maintainer
                </li>
                <li class="user-role-chip"
                    @click="changeProfile({ member: getMemberInfo, project: scope.project, role: 'ADMIN'})" 
                    :class="scope.role === 'ADMIN' ? ' active' : ''">
                    <span v-if="scope.role === 'ADMIN'"><Icon type="md-checkmark" /></span>
                    Admin
                </li>
              </ul>
            </td>

            <td class="remove-member-project-btn">
                <Icon type="md-close-circle" size="32" @click="changeProfile('remove')" />
            </td>
          </tr>
        </tbody>
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
  export default {
    name: 'admin-management-members',
    components: {
      formField
    },
    data () {
      return {
        memberInfo: [],
        members: [],
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
        groupMembers: [],
        memberType: localStorage.getItem('memberType'),
        currentProfile: ''
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
      },
      changeProfile (changeType) {
        const role = {
          scope: changeType.newRole,
          project: changeType.project
        }
        if (changeType === 'remove') {
          Vue.http
            .delete(api.paths.userProjectScopeManagement(changeType.member.login, changeType.project), api.REQUEST_OPTIONS)
            .then(() => this.getMemberInfo)
        } else {
          Vue.http
            .put(api.paths.userProjectScopeManagement(changeType.member.login, changeType.project), role, api.REQUEST_OPTIONS)
            .then(() => this.getMemberInfo)
        }
      }
    },
    created () {
      this.currentProfile = JSON.parse(localStorage.getItem('current_user'))
    },
    computed: {
      sortedMembers () {
        return this.members.map(item => item).sort((a, b) => a.id - b.id)
      },
      getMemberInfo () {
        return JSON.parse(localStorage.getItem('user'))
      }
    }
  }
</script>

<style scoped>
    .adminTable {
        font-size: 16px;
    }
    .adminTable tbody tr {
        height: 80px;
    }
    .user-project-roles {
        display: flex;
        list-style: none;
        justify-content: center;
    }
    .user-role-chip {
        display: flex;
        width: 150px;
        cursor: pointer;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        border: 2px solid #d9dde1;
        height: 44px;
        padding: 0 25px;
        border-radius: 100px;
        margin: 0 80px;
    }
    .user-role-chip:hover {
      background-color: #007DBC;
      border: 2px solid #007DBC;
      color: #ffffff;
    }
    .user-role-chip.active {
        background-color: #007DBC;
        border: 1px solid #007DBC;
        color: #ffffff;
    }

    .adminTable tbody tr:first-child td:first-child {
        border-radius: 10px 0 0 0;
        display: table-cell;
    }

    .adminTable tbody tr:first-child td:last-child {
        border-radius: 0 10px 0 0;
        display: table-cell;
    }

    .adminTable tbody tr:last-child td:first-child {
        border-radius: 0 0 0 10px;
        display: table-cell;
    }

    .adminTable tbody tr:last-child td:last-child {
        border-radius: 0 0 10px 0;
        display: table-cell;
    }
</style>
