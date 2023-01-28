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
    <div class="tableContent">
      <span class="breadcrumbLink" @click="$router.go(-1)">
        <Icon type="md-home" />
        list
      </span>

      <h1 class="adminTitle">Members</h1>

      <div class="searchButton">
        <AutoComplete
          v-model="value"
          placeholder="Search a member..."
          icon="ios-search"
          size="default">
            <Option v-for="(item, index) in members" :value="item" :key="index">{{ item.name }}</Option>
        </AutoComplete>
      </div>

      <table class="adminTable" aria-label="Users with their roles and projects">
        <thead>
          <tr>
            <th>Name</th>
            <th>Profile</th>
            <th>Projects</th>
            <th></th>
          </tr>
        </thead>

        <tbody v-if="members">
          <tr v-for="(member, index) in members" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
            <td class="userType">
              {{ getProjectUserNameDisplay(member.login) }}
            </td>

            <td class="userType">
              {{ member.profile }}
            </td>

            <td class="member-projects-list">
              <span v-for="(scope, index) in member.scopes" :class="index > 2 ? 'project-count' : ''">
                <template v-if="index <= 2">
                  {{ scope.project }}
                </template>
                <template v-if="index === member.scopes.length - 1 && index > 2">
                  <Tag color="warning">+{{ member.scopes.length - 3 }}</Tag>
                </template>
              </span>
            </td>

            <td :class="member.login !== currentUser.login ? '' : 'hidden'">
              <Icon v-if="!member.blockReason" type="ios-checkmark-circle" size="24" @click="showUserBlock(member, index)"/>
              <Icon v-if="member.blockReason" type="md-remove-circle" size="24" @click="unblockUser(index)"/>
              <Icon type="md-eye" size="24" @click="navTo(member)"/>
            </td>
          </tr>
        </tbody>
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

    <Modal v-model="blockPopup" title="Block user" okText="Block user" @on-ok="confirmBlockUser" @close="memberToAdd = false" :width="900"
      :loading="loadingSaving" :footer-hide="!selectedBlockOption" ref="editPopup">
      <p>Select what the user will be banned from:</p>

      <div class="banOptions">
        <RadioGroup v-model="selectedBlockOption" type="button">
          <Radio label="ARA"></Radio>
          <Radio label="Projects creation"></Radio>
          <Radio label="Groups creation"></Radio>
        </RadioGroup>
      </div>
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import formField from '../components/form-field'
  import { AuthenticationService } from '../service/authentication.service'

  export default {
    name: 'admin-management-members',
    components: {
      formField
    },
    data () {
      return {
        members: [],
        memberToAdd: false,
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
        editingData: {},
        editingNew: false,
        editing: false,
        showGroup: false,
        userRole: '',
        blockPopup: false,
        memberToBlock: {
          'member': '',
          'index': '',
          'blockReason': ''
        },
        selectedBlockOption: ''
      }
    },
    methods: {
      createMember () {
        Vue.http
          .post('/api/groups', this.editingData, api.REQUEST_OPTIONS)
          .then(() => { return this.members })
      },
      async getMember () {
        const profile = this.currentUser.profile
        this.members = []
        let url = api.paths.scopedUsers
        if (profile === 'SUPER_ADMIN' || profile === 'AUDITOR') {
          url = api.paths.allUsers
        }
        await Vue.http
          .get(url, api.REQUEST_OPTIONS)
          .then((users) => {
            if (users.body.length > 0) {
              for (let user of users.body) {
                this.members.push(user)
              }
            }
            return this.members
          })
      },
      showUserBlock (member, index) {
        this.blockPopup = true
        this.memberToBlock.member = member
        this.memberToBlock.index = index
      },
      unblockUser (index) {
        delete this.members[index].blockReason
        this.getMember('Users')
      },
      confirmBlockUser () {
        this.memberToBlock.blockReason = 'Banned from ' + this.selectedBlockOption
        this.members[this.memberToBlock.index].blockReason = this.memberToBlock.blockReason
      },
      removeGroup (member) {
        if (confirm('Do you really want to delete this member?')) {
          Vue.http
            .delete('/api/groups/' + member.name, api.REQUEST_OPTIONS)
            .then(() => {
              for (let k = 0; k < this.members.length; k++) {
                if (this.members[k].name === member.name) {
                  this.members.splice(k, 1)
                }
              }
            })
        }
      },
      async navTo (user) {
        localStorage.setItem('user', JSON.stringify(user))
        this.$router.push({ name: 'member-projects', params: { user: user.login } })
      },
      getProjectUserNameDisplay (login) {
        if (login === this.currentUser.login) {
          return 'Me'
        }
        return login
      }
    },
    created () {
      this.getMember()
    },
    computed: {
      sortedMembers () {
        this.$store.dispatch('admin/showSubMenuMembers', false)
        return this.members.map(item => item).sort((a, b) => a.id - b.id)
      },
      currentUser () {
        return AuthenticationService.getDetails().user
      }
    }
  }
</script>

<style scoped>
  .searchButton {
    display: flex;
    justify-content: center;
  }
  .searchButton .ivu-select {
    width: 40%;
  }
  .member-projects-list {
    vertical-align: middle;
  }
  .member-projects-list span {
    margin: 0 2px;
    vertical-align: middle;
  }
  .hidden > *{
    visibility: hidden;
  }
</style>
