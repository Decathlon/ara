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
          <p class="title-description"><strong>{{ projectCode }}</strong></p>
        </h1>
      </div>
    </div>

    <div class="tableContent">
      <span class="breadcrumbLink" @click="$router.go(-1)">
        <Icon type="md-home" />
        Project list
      </span>

      <div class="projectCTA">
        <h2 v-if="!emptyProject">Project's members</h2>

        <span :class="isMember ? 'hidden' : isAuditor ? 'hidden' : ''">
          <Alert v-if="emptyProject" type="warning" class="btn-group-right">There are no members to show in this projects</Alert>
          <Button title="Add" type="primary" ghost class="btn-group-right" @click="memberToAdd = true">Add member</Button>
          <Button title="Edit" type="primary" class="btn-group-right" ghost>Edit project</Button>
          <Button title="Delete project" class="btn-group-right" type="error">Remove project</Button>
        </span>
      </div>

      <table v-if="!emptyProject" class="adminTable" aria-label="Project's members name and role">
        <thead>
          <tr>
            <th>Name</th>
            <th>Role</th>
            <th></th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(member, index) in usersList" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
            <td >{{ isMe(member.login) }}</td>
            <td>{{ getUserRole(member.scopes) }}</td>
            <td class="table-cta">
              <Icon v-if="!isMember && !isAuditor" type="md-close-circle" class="crossIcon" size="24" @click="removeUserFromProject(member)" />
              <Icon v-if="!isMember && !isAuditor" type="md-create" size="24" @click="openProjectDetails(project)" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <Modal v-model="memberToAdd" title="Add Member" okText="Add" footer-hide :width="900"
      :loading="loadingSaving" ref="editPopup">
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="128">
        <FormItem label="Search">
          <Input search placeholder="Enter something..." />
        </FormItem>
        <FormItem label="Role" prop="role">
          <RadioGroup v-model="formValidate.role">
            <Radio v-for="role in userRole" class="ivu-radio-border" :label="role"></Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="Users" prop="scope">
          <span v-if="!isAlreadyMember.length"><strong>No members to add</strong></span>
          <RadioGroup v-model="formValidate.scope">
            <Radio v-for="user in isAlreadyMember" class="ivu-radio-border" :label="user"></Radio>
          </RadioGroup>
        </FormItem>
        <Form-item class="modal-cta">
          <Button @click="memberToAdd = false">Cancel</Button>
          <Button type="primary" @click="handleSubmit('formValidate')" :disabled="!isAlreadyMember.length">Add</Button>
        </Form-item>
      </Form>
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import formField from '../components/form-field'
  import { AuthenticationService } from '../service/authentication.service'
  import { mapState } from 'vuex'
  import { USER } from '../libs/constants'

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
        usersList: [],
        members: [],
        emptyProject: false,
        userRole: ['Member', 'Maintainer', 'Admin'],
        formValidate: {
          role: '',
          scope: ''
        },
        ruleValidate: {
          role: [
            { required: true, message: 'You need to assign your user a role', trigger: 'change' }
          ],
          scope: [
            { required: true, message: 'Choose at least one user to add', trigger: 'change' }
          ]
        },
        projectRole: ''
      }
    },

    methods: {
      getScopedUsers () {
        this.usersList = []

        Vue.http
          .get(api.paths.scopedUsers, api.REQUEST_OPTIONS)
          .then((response) => {
            this.members = response.body
          })

        Vue.http
          .get(api.paths.scopedUsersByProject(this.projectCode), api.REQUEST_OPTIONS)
          .then((response) => {
            if (response.body.length > 0) {
              this.emptyProject = false
              this.usersList = response.body
            } else {
              this.emptyProject = true
            }
          })
      },

      getUserRole (scope) {
        let userRole = ''
        scope.filter((item) => {
          if (item.project === this.projectCode) {
            userRole = item.role
          }
        })
        return userRole
      },

      handleSubmit (name) {
        this.$refs[name].validate((valid) => {
          if (valid) {
            const scopeInfo = {
              role: this.formValidate.role.toUpperCase(),
              project: this.projectCode
            }
            Vue.http
              .put(api.paths.userProjectScopeManagement(this.formValidate.scope, this.projectCode), scopeInfo, api.REQUEST_OPTIONS)
              .then(() => {
                this.getScopedUsers()
                this.$refs[name].resetFields()
                this.memberToAdd = false
                this.$Message.success({
                  content: 'User successfully added to ' + this.projectCode,
                  duration: 2
                })
              })
          } else {
            this.$Message.error({
              content: 'Please fill all required fields',
              duration: 2
            })
          }
        })
      },

      removeUserFromProject (member) {
        const scopeInfo = {
          role: this.getUserRole(member.scopes),
          project: this.projectCode
        }

        Vue.http
          .delete(api.paths.userProjectScopeManagement(member.login, this.projectCode), scopeInfo, api.REQUEST_OPTIONS)
          .then(() => {
            this.getScopedUsers()
            this.$Message.success({
              content: 'User successfully removed from ' + this.projectCode,
              duration: 2
            })
          })
      },

      isMe (user) {
        if (user === this.user.login) {
          return 'Me'
        } else {
          return user
        }
      }
    },

    computed: {
      ...mapState('users', ['user']),

      isAlreadyMember () {
        const newUser = this.members?.map(item => item.login)

        if (this.usersList.length > 0) {
          return newUser.filter(val => !JSON.stringify(this.usersList).includes(val))
        } else {
          return newUser
        }
      },

      isMember () {
        return this.$route.params.userRole === USER.ROLE_ON_PROJECT.MEMBER
      },

      isAuditor () {
        return this.$route.params.userRole === USER.PROFILE.AUDITOR
      }
    },

    mounted () {
      this.projectCode = this.$route.params.projectCode
      this.getScopedUsers()
      this.$store.dispatch('users/getUserInfo', AuthenticationService.getDetails().user)
      this.projectName = this.$route.params.projectName
    },

    beforeDestroy () {
      this.projectInfo = this.$route.params
    }
  }
</script>

<style scoped>
  .projectMemberFilter {
    width: 90%;
    margin: 0 auto;
  }

  .project-cta {
    margin-left: auto;
  }

  .ivu-radio-border {
    border: 1px solid #dcdee2;
    border-radius: 4px;
    height: 32px;
    line-height: 30px;
    padding: 0 15px;
    -webkit-transition: border .2s ease-in-out;
    transition: border .2s ease-in-out;
  }

  .ivu-radio-wrapper-checked.ivu-radio-border {
    border-color: #2d8cf0;
  }
</style>