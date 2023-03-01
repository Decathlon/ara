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

      <h1 class="adminTitle">{{ memberType }}</h1>

      <div v-if="manageProject || isNotAuditor">
        <div v-if="searchedUser.length" class="array-filters">
          <div class="member-type-select">
            <Select placeholder="Members" @on-change="changeMemberView" :label-in-value="true">
              <Option v-for="item in memberValues" :value="item.value" :key="item.value">{{ item.label }}</Option>
            </Select>
          </div>
          <div class="member-search">
            <Input class="filterSearch" v-model="searchElement" search placeholder="Enter something..." />
          </div>
          <div class="member-cta" :class="memberType === 'Members' ? 'hidden' : ''">
            <Button type="primary" class="addBtn" @click="memberToAdd = true">
              Add group
            </Button>
          </div>
        </div>

        <table v-if="searchedUser.length" class="adminTable" aria-label="Users with their roles and projects">
          <thead>
            <tr v-if="memberType === 'Members'">
              <th v-for="header in memberHeader">
                {{ header }}
              </th>
            </tr>
            <tr v-else>
              <th v-for="header in groupHeader">
                {{ header }}
              </th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="(member, index) in searchedUser" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
              <td>
                <div>
                  <div v-if="memberType !== 'Groups'" class="user-avatar">
                    <span v-if="member.pictureUrl" :style="getAvatar(member)" class="user-picture"></span>
                    <Avatar v-else class="user-picture">{{ getAvatar(member)  }}</Avatar>
                    <p>{{ member.login }}</p>
                  </div>
                  <div v-else>
                    <p>{{ member.name }}</p>
                  </div>
                </div>
              </td>

              <td>
                <p v-if="memberType === 'Members'">{{ member.profile }}</p>
                <p v-else>{{  member.managers[0].login }}</p>
              </td>

              <td v-if="memberType === 'Groups'">
                {{  member.users }}
              </td>

              <td class="member-projects-list">
                <span v-for="(scope, index) in member.scopes" :class="index > 2 ? 'project-count' : ''">
                  <template v-if="index <= 2">
                    <Tag color="default">{{ scope.project }}</Tag>
                  </template>
                  <template v-if="index === member.scopes.length - 1 && index > 2">
                    <Tag color="warning">+{{ member.scopes.length - 3 }}</Tag>
                  </template>
                </span>
              </td>

              <td class="table-cta">
                <Icon v-if="!isMe(member.login) && isNotAuditor && isManagerOf(member.scopes)" type="md-eye" size="24" @click="navTo(member)" />
              </td>
            </tr>
          </tbody>
        </table>

        <div v-else-if="showError" class="no-values">
          <h2>There are no groups to display. Contact an admin or create a new group by clicking the button below.</h2>
          <Button type="primary" class="addBtn" @click="memberToAdd = true" >
            Add group
          </Button>
        </div>
      </div>

      <div class="adminTable" v-else>
        <Alert type="warning">You need to be a <strong>super admin</strong> or at least <strong>admin of one project</strong> to access this page</Alert>
      </div>
    </div>

    <Modal v-model="memberToAdd" title="Add Group" :width="900"
      :loading="loadingSaving" :footer-hide="true">
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="128">
        <Form-item label="Code" prop="code">
          <Input v-model="formValidate.code" />
        </Form-item>
        <Form-item label="Description" prop="code">
          <Input v-model="formValidate.description" type="textarea" placeholder="Group's decription..." />
        </Form-item>
        <Form-item class="modal-cta">
          <Button @click="memberToAdd = false">Cancel</Button>
          <Button type="primary" @click="handleSubmit()">Submit</Button>
        </Form-item>
      </Form>
    </Modal>

    <Modal v-model="blockPopup" title="Block user" okText="Block user" @on-ok="confirmBlockUser" :width="900"
      :loading="loadingSaving" :footer-hide="!selectedBlockOption">
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
  import { AuthenticationService } from '../service/authentication.service'
  import { USER } from '../libs/constants'

  export default {
    name: 'admin-management-members',

    data () {
      return {
        members: [],
        memberToAdd: false,
        memberHeader: ['Name', 'Profile', 'Projects', ''],
        groupHeader: ['Name', 'Management', 'Users', 'Projects', ''],
        blockPopup: false,
        formValidate: {
          code: '',
          description: ''
        },
        ruleValidate: {
          code: { required: true, message: 'The code cannot be empty', trigger: 'blur' }
        },
        memberToBlock: {
          'member': '',
          'index': '',
          'blockReason': ''
        },
        memberValues: [
          {
            value: 'member',
            label: 'Members'
          },
          {
            value: 'group',
            label: 'Groups'
          }
        ],
        selectedBlockOption: '',
        memberType: 'Members',
        searchElement: '',
        showError: false
      }
    },

    methods: {
      getGroups () {
        this.members = []
        Vue.http
          .get(api.paths.allGroups, api.REQUEST_OPTIONS)
          .then((groups) => {
            if (groups.body.length > 0) {
              for (let group of groups.body) {
                this.members.push(group)
              }
            } else {
              this.showError = true
            }

            return this.members.sort((a, b) => a.name.localeCompare(b.name))
          })
      },

      async getMember () {
        const profile = this.currentUser.profile
        this.members = []
        let url = api.paths.scopedUsers
        if (profile === USER.PROFILE.SUPER_ADMIN || profile === USER.PROFILE.AUDITOR) {
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

            this.members.push({
              login: 'John Doe',
              profile: 'SCOPED_USER',
              scopes: []
            }, {
              login: 'Bryan',
              profile: 'SCOPED_USER',
              scopes: []
            })
            return this.members.sort((a, b) => a.login.localeCompare(b.login))
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

      navTo (user) {
        localStorage.setItem('user', JSON.stringify(user))
        if (this.memberType === 'Members') {
          this.$router.push({ name: 'member-projects', params: { user: user.login } })
        } else {
          this.$router.push({ name: 'group-details', params: { groupName: user.name } })
        }
      },

      getProjectUserNameDisplay (login) {
        if (login === this.currentUser.login) {
          return 'Me'
        }
        return login
      },

      changeMemberView (type) {
        this.memberType = type.label
        if (type.label === 'Members') {
          this.getMember()
        } else {
          this.getGroups()
        }
      },

      isManagerOf (scopes) {
        const adminProject = this.currentUser.scopes.filter(project => project.role === 'ADMIN')

        if (this.isAdmin) {
          return true
        } else {
          return scopes.some(admin => adminProject.find(project => project.project === admin.project))
        }
      },

      isMe (user) {
        if (user === this.currentUser.login) {
          return true
        } else {
          return false
        }
      },

      handleSubmit () {
        this.$refs['formValidate'].validate((valid) => {
          if (valid) {
            const group = {
              name: this.formValidate.code,
              description: this.formValidate.description
            }

            Vue.http
              .post(api.paths.groupBasePath, group, api.REQUEST_OPTIONS)
              .then(() => { return this.members })
          }
        })
      },

      getAvatar (member) {
        if (member.pictureUrl) {
          return `backgroundImage: url("` + member.pictureUrl + `")`
        } else if (member.login) {
          return member.login.substring(0, 1)
        }
      }
    },

    created () {
      this.getMember()
    },

    computed: {
      currentUser () {
        return AuthenticationService.getDetails().user
      },

      manageProject () {
        return this.currentUser.scopes.some(({ role }) => role === 'ADMIN')
      },

      isAdmin () {
        return this.currentUser.profile === 'SUPER_ADMIN'
      },

      isNotAuditor () {
        return this.currentUser.profile !== 'AUDITOR'
      },

      searchedUser () {
        return this.members.filter(member => member.login ? member.login.toLowerCase().includes(this.searchElement.toLowerCase()) : member.name.toLowerCase().includes(this.searchElement.toLowerCase()))
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

  .array-filters {
    width: 90%;
    margin: 0 auto;
    display: flex;
  }

  .member-type-select {
    width: 100px;
    margin-right: auto;
  }

  .member-search {
    width: 400px;
    margin: auto;
  }

  .member-cta {
    margin-left: auto;
  }

  .ivu-tag {
    cursor: initial;
  }

  .ivu-tag:hover {
    opacity: 1;
  }

  .user-avatar {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
  }

  .user-avatar .user-picture {
    background-size: cover;
    width: 35px;
    height: 35px;
    border-radius: 100px;
    margin: 0 15px 0 0;
  }

  .user-avatar span {
    position: absolute;
    left: 25px;
  }
</style>
