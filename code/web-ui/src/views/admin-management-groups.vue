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
    <h1 v-if="this.groupInfo" class="adminTitle">
      {{ this.groupInfo.name }}
      <p class="title-description"><strong>{{ this.groupInfo.description }}</strong></p>
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

          <tbody>
            <tr
              v-for="(managers, index) in this.groupInfo.managers"
              :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ managers.login }}</p>
              </td>
              <td align="right">
                <Icon type="md-close-circle" size="24" @click="removeManager(managers.login)"/>
              </td>
            </tr>
          </tbody>
        </table>
      </TabPane>
      <TabPane label="Users">
        <table v-if="this.groupInfo.members[0]" class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="(user, index) in this.groupInfo.members"
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

        <p v-else>There are no users in this group.</p>
      </TabPane>
      <TabPane label="Scopes">
        <table v-if="this.groupInfo.scopes[0]" class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="(scopes, index) in this.groupInfo.scopes"
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

      <Button @click="addElementToGroup = true" type="primary" size="medium" slot="extra">Add {{ groupTabs[activeTab] }}</Button>
    </Tabs>

    <Modal v-model="addElementToGroup" ok-text="Ajouter" :title='"Add " + selectedTab' footer-hide :width="900" :loading="loadingSaving">
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="128">
        <Form-item label="Search">
          <Input v-model="userSearch" icon="md-search" placeholder="Enter something..." />
        </Form-item>
        <Form-item v-if="selectedTab === 'Managers'" :label="selectedTab" prop="user">
          <RadioGroup v-if="availableManagers && availableManagers.length > 0" v-model="formValidate.user">
            <Radio v-for="(manager, index) in availableManagers" :key="index" :label="manager"></Radio>
          </RadioGroup>
          <p v-else>There are no {{ selectedTab }} to add to this group</p>
        </Form-item>
        <Form-item v-if="selectedTab === 'Users'" :label="selectedTab" prop="user">
          <RadioGroup v-if="availableUsers && availableUsers.length > 0" v-model="formValidate.user">
            <Radio v-for="(user, index) in availableUsers" :key="index" :label="user"></Radio>
          </RadioGroup>
          <p v-else>There are no {{ selectedTab }} to add to this group</p>
        </Form-item>
        <Form-item v-if="selectedTab === 'Projects'" :label="selectedTab" prop="projectCode">
          <RadioGroup v-model="formValidate.projectCode">
            <Radio v-for="(project, index) in searchProjects" :key="index" :label="project"></Radio>
          </RadioGroup>
        </Form-item>
        <Form-item v-if="selectedTab === 'Projects'" label="Role" prop="projectRole">
          <RadioGroup v-model="formValidate.projectRole">
            <Radio v-for="(role, index) in userRole" class="ivu-radio-border" :key="index" :label="role"></Radio>
          </RadioGroup>
        </Form-item>
        <Form-item class="modal-cta">
          <Button @click="addElementToGroup = false">Cancel</Button>
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
        groupTabs: ['Managers', 'Users', 'Scopes'],
        userRole: ['Member', 'Maintainer', 'Admin'],
        groupInfo: [],
        activeTab: 0,
        addElementToGroup: false,
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
            groups.body.filter(group => {
              if (group.name === this.$route.params.groupName) {
                this.groupInfo = group
              }
            })
          })
      },

      getGroupElements () {
        Vue.http
          .get(api.paths.scopedUsers, api.REQUEST_OPTIONS)
          .then((users) => {
            this.usersList = users.body.map(user => user.login)

            Vue.http
              .get(api.paths.currentUser, api.REQUEST_OPTIONS)
              .then((user) => { this.scopesList = user.body.scopes.map(scope => scope.project) })
          })
      },

      addToGroup (form) {
        this.$refs[form].validate((valid) => {
          if (valid) {
            switch (this.selectedTab) {
              case 'Managers':
                Vue.http
                  .put(api.paths.groupsManagersManagement(this.formValidate.user, this.groupInfo.id), api.REQUEST_OPTIONS)
                  .then(response => {
                    this.addElementToGroup = false
                    this.groupInfo = response.body
                  })
                break
              case 'Users':
                Vue.http
                  .put(api.paths.groupsMembersManagement(this.formValidate.user, this.groupInfo.id), api.REQUEST_OPTIONS)
                  .then(response => {
                    this.addElementToGroup = false
                    this.groupInfo = response.body
                  })
                break
              case 'Projects':
                const newProject = {
                  project: this.formValidate.projectCode,
                  role: this.formValidate.projectRole.toUpperCase()
                }

                Vue.http
                  .put(api.paths.groupScopeManagement(this.groupInfo.id, this.formValidate.projectCode), newProject, api.REQUEST_OPTIONS)
                  .then(response => {
                    this.addElementToGroup = false
                    this.groupInfo = response.body
                  })
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
          .delete(api.paths.groupsMembersManagement(user, this.groupInfo.id), api.REQUEST_OPTIONS)
          .then(() => { return this.groupInfo })
      },

      removeManager (manager) {
        Vue.http
          .delete(api.paths.groupsManagersManagement(manager, this.groupInfo.id), api.REQUEST_OPTIONS)
          .then(() => { return this.groupInfo })
      },

      removeScope (scope) {
        Vue.http
          .delete(api.paths.groupScopeManagement(this.groupInfo.id, scope), api.REQUEST_OPTIONS)
          .then(() => { return this.groupInfo })
      },

      deleteGroup () {
        Vue.http
          .delete(api.paths.groupById(this.groupInfo.id), api.REQUEST_OPTIONS)
          .then(() => this.$router.go(-1))
      }
    },

    computed: {
      availableUsers () {
        if (this.groupInfo) {
          const alreadyPartOf = this.groupInfo.members.map(i => i.login)

          return this.usersList.filter((user) => !alreadyPartOf.includes(user))
        }
      },

      availableManagers () {
        if (this.groupInfo) {
          const alreadyPartOf = this.groupInfo.managers.map(i => i.login)

          return this.usersList.filter((user) => !alreadyPartOf.includes(user))
        }
      },

      searchedUser () {
        return this.usersList.filter(user => user.includes(this.userSearch))
      },

      searchProjects () {
        return this.scopesList.filter(scope => scope.includes(this.userSearch))
      },

      selectedTab () {
        return this.groupTabs[this.activeTab]
      }
    },

    mounted () {
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
