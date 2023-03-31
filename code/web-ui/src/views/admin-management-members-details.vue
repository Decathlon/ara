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

    <div class="adminTitle">
      <h1>{{ memberInfo.name }}</h1>
      <div class="memberRole">
        {{ memberInfo.role }}
        <div v-if="memberType == 'Users'">
          <Icon type="md-create" />
        </div>
        <div v-else>
          <p>Group description</p>
        </div>
      </div>
    </div>

    <div>
      <table class="adminTable">
        <thead>
          <tr>
            <th>Projects</th>
            <th>Role</th>
            <th></th>
          </tr>
        </thead>

        <tbody v-if="members">
          <tr v-for="(member, index) in sortedMembers" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
            <td class="userType">
              {{ member.name }}
            </td>

            <td class="userType">
              {{ member.role }}
            </td>

            <td class="userType">
              {{ member.blockReason }}
            </td>
          </tr>
        </tbody>
        <button class="showBtn" @click="memberToAdd = true">
          <Icon type="md-eye" size="24"/>
        </button>
        <button class="addBtn" @click="memberToAdd = true">
          <Icon type="md-add" size="24"/>
        </button>
      </table>

      <table class="adminTable">
        <thead>
          <tr>
            <th>Name</th>
            <th>Admin(s)</th>
          </tr>
        </thead>

        <tbody v-if="members">
          <tr v-for="(member, index) in sortedMembers" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
            <td class="userType">
              {{ member.name }}
            </td>

            <td class="userType">
              {{ member.role }}
            </td>
          </tr>
        </tbody>
        <button v-if="memberType == 'Groups'" class="addBtn" @click="memberToAdd = true">
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
        memberType: localStorage.getItem('memberType')
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
      this.memberInfo = this.$route.query
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
  .memberRole {
    text-align: center;
    color: #AC8DAF;
    font-weight: bold;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .memberRole i {
    color: #ff7d00;
    font-size: 14px;
    margin-left: 5px;
  }
</style>
