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
  <div class="tableContent">
    <h1 v-if="filteredGroups" class="adminTitle">
      {{ filteredGroups[0].name }}
      <p class="title-description"><strong>{{ filteredGroups[0].description }}</strong></p>
    </h1>

    <Button class="top-right-btn" type="error" size="medium" ghost @click="deleteGroup()">Delete group</Button>
    
    <Tabs class="adminTable" type="card" v-model="activeTab" :animated="false">
      <TabPane label="Managers">
        <table class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th></th>
            </tr>
          </thead>

          <tbody v-for="member in filteredGroups" :key="member.id">
            <tr
              v-for="(managers, index) in member.managers"
              :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ managers.login }}</p>
              </td>
              <td align="right">
                <Icon v-if="member.managers.length > 1" type="md-close-circle" size="24" @click="removeManager(managers.login)"/>
              </td>
            </tr>
          </tbody>
        </table>
      </TabPane>
      <TabPane label="Users">
        <table v-if="filteredGroups && filteredGroups[0].members.length" class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th></th>
            </tr>
          </thead>

          <tbody v-for="member in filteredGroups" :key="member.id">
            <tr
              v-for="(user, index) in member.members"
              :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ user.login }}</p>
              </td>
              <td align="right">
                <Icon type="md-close-circle" size="24" @click="removeUser(user.login)"/>
              </td>
            </tr>
          </tbody>
        </table>

        <p v-else>There are no users in this group yet.</p>
      </TabPane>
      <TabPane label="Projects">
        <table v-if="filteredGroups && filteredGroups[0].scopes.length" class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
              <th></th>
            </tr>
          </thead>

          <tbody v-for="member in filteredGroups" :key="member.id">
            <tr
              v-for="(scopes, index) in member.scopes"
              :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ scopes.project }}</p>
              </td>
              <td>
                <p>{{ scopes.role }}</p>
              </td>
              <td align="right">
                <Icon type="md-close-circle" size="24" @click="removeScope(scopes.project)"/>
              </td>
            </tr>
          </tbody>
        </table>

        <p v-else>There are no projects associated to this group yet.</p>
      </TabPane>

      <Button @click="addUserToGroup = true" type="primary" size="medium" slot="extra">Add {{ groupTabs[activeTab] }}</Button>
    </Tabs>

    <Modal
      v-model="memberToAdd"
      title="Add Group"
      :width="900"
      :loading="loadingSaving"
      :footer-hide="true"
    >
      <Form
        ref="formValidate"
        :model="formValidate"
        :rules="ruleValidate"
        :label-width="128"
      >
        <Form-item label="Code" prop="code">
          <Input v-model="formValidate.code" />
        </Form-item>
        <Form-item class="modal-cta">
          <Button @click="closeModal(modalConfiguration[0])">Cancel</Button>
          <Button type="primary" @click="handleSubmit()">Submit</Button>
        </Form-item>
      </Form>
    </Modal>

    <Modal v-model="addUserToGroup" ok-text="Ajouter" :title='"Add " + selectedTab' footer-hide :width="900" :loading="loadingSaving">
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="128">
        <Form-item label="Search">
          <Input v-model="userSearch" icon="md-search" placeholder="Enter something..." />
        </Form-item>
        <Form-item v-if="selectedTab === 'Managers'" :label="selectedTab" prop="user">``
          <RadioGroup v-model="formValidate.user">
            <Radio v-for="(manager, index) in searchedUser" :key="index" :label="manager"></Radio>
          </RadioGroup>
        </Form-item>
        <Form-item v-if="selectedTab === 'Users'" :label="selectedTab" prop="user">``
          <RadioGroup v-model="formValidate.user">
            <Radio v-for="(user, index) in searchedUser" :key="index" :label="user"></Radio>
          </RadioGroup>
        </Form-item>
        <Form-item v-else :label="selectedTab" prop="projectCode">
          <RadioGroup v-model="formValidate.projectCode">
            <Radio v-for="(project, index) in searchedUser" :key="index" :label="project"></Radio>
          </RadioGroup>
        </Form-item>
        <Form-item v-if="selectedTab === 'Projects'" label="Role" prop="projectRole">
          <RadioGroup v-model="formValidate.projectRole">
            <Radio v-for="(role, index) in userRole" class="ivu-radio-border" :key="index" :label="role"></Radio>
          </RadioGroup>
        </Form-item>
        <Form-item class="modal-cta">
          <Button @click="memberToAdd = false">Cancel</Button>
          <Button type="primary" @click="addToGroup('formValidate')">Add</Button>
        </Form-item>
      </Form>
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  export default {
    name: 'admin-management-groups',

    data () {
      return {
        members: [],
        memberToAdd: false,
        blockPopup: false,
        formValidate: {
          user: '',
          projectCode: '',
          projectRole: ''
        },
        ruleValidate: {
          user: { required: true, message: 'You need to choose one user', trigger: 'blur' },
          projectCode: { required: true, message: 'You need to choose a project', trigger: 'blur' },
          projectRole: { required: true, message: 'You need to choose a role', trigger: 'blur' }
        },
        groupTabs: ['Managers', 'Users', 'Projects'],
        userRole: ['Member', 'Maintainer', 'Admin'],
        selectedBlockOption: '',
        memberType: 'Members',
        groupInfo: [],
        activeTab: 0,
        addUserToGroup: false,
        userSearch: '',
        usersList: [],
        scopesList: []
      }
    },

    methods: {
      getGroupInfo () {
        Vue.http
          .get(api.paths.allGroups, api.REQUEST_OPTIONS)
          .then((groups) => {
            if (groups.body.length > 1) {
              this.groupInfo = groups.body
            } else {
              this.groupInfo.push(groups.body)
            }
          })
      },

      getGroupElements () {
        Vue.http
          .get(api.paths.scopedUsers, api.REQUEST_OPTIONS)
          .then((users) => {
            this.usersList = users.body.map(user => user.login)

            Vue.http
              .get(api.paths.projects, api.REQUEST_OPTIONS)
              .then((projects) => { this.scopesList = projects.body.map(project => project.code) })
          })
      },

      addToGroup (form) {
        this.$refs[form].validate((valid) => {
          if (valid) {
            switch (this.selectedTab) {
              case 'Managers':
                Vue.http
                  .put(api.paths.groupsManagersManagement(this.formValidate.user, this.groupInfo[0].id), api.REQUEST_OPTIONS)
                  .then(response => { this.groupInfo = response.body })
                break
              case 'Users':
                Vue.http
                  .put(api.paths.groupsMembersManagement(this.formValidate.user, this.groupInfo[0].id), api.REQUEST_OPTIONS)
                  .then(response => {
                    this.groupInfo = response.body
                  })
                break
              case 'Projects':
                const newProject = {
                  project: this.formValidate.projectCode,
                  role: this.formValidate.projectRole.toUpperCase()
                }

                Vue.http
                  .put(api.paths.groupScopeManagement(this.groupInfo[0].id, this.formValidate.projectCode), newProject, api.REQUEST_OPTIONS)
                  .then(response => { this.groupInfo = response.body })
                break
            }
          } else {
            this.$Message.error({
              content: 'Please fill all required fields',
              duration: 2
            })
          }
        })
      },

      removeUser (user) {
        Vue.http
          .delete(api.paths.groupsMembersManagement(user, this.groupInfo[0].id), api.REQUEST_OPTIONS)
          .then(() => { this.getGroupInfo() })
      },

      removeManager (manager) {
        Vue.http
          .delete(api.paths.groupsManagersManagement(manager, this.groupInfo[0].id), api.REQUEST_OPTIONS)
          .then(() => { this.getGroupInfo() })
      },

      removeScope (scope) {
        Vue.http
          .delete(api.paths.groupScopeManagement(this.groupInfo[0].id, scope), api.REQUEST_OPTIONS)
          .then(() => { this.getGroupInfo() })
      },

      deleteGroup () {
        Vue.http
          .delete(api.paths.groupById(this.groupInfo[0].id), api.REQUEST_OPTIONS)
          .then(() => this.$router.go(-1))
      }
    },

    computed: {
      filteredGroups () {
        if (this.groupInfo.length > 0) {
          return this.groupInfo.filter(item => item.name === this.$route.params.groupName)
        }
      },

      availableUsers () {
        return this.usersList.filter((item) => !(this.filteredGroups[0].managers.map(i => i.login)).includes(item))
      },

      searchedUser () {
        if (this.selectedTab === 'Users') {
          return this.usersList.filter(user => user.includes(this.userSearch))
        } else {
          return this.scopesList.filter(scope => scope.includes(this.userSearch))
        }
      },

      selectedTab () {
        return this.groupTabs[this.activeTab]
      }
    },

    created () {
      this.getGroupInfo()
      this.getGroupElements()
    }
  }
</script>

<style scoped>
.projectCTA {
  justify-content: right;
}

.projectCTA .spacing-btn {
  margin-right: 15px;
}

.tab-content {
  width: 100%;
  border-collapse: collapse;
}

.tab-content th:first-child {
  border-radius: 4px 0 0 0;
}

.top-right-btn {
  position: absolute;
  top: 0;
  right: 0;
}
</style>
