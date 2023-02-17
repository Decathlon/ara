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
    </div>

    <div class="tableContent">
      <span class="breadcrumbLink" @click="$router.go(-1)">
        <Icon type="md-home" />
        Project list
      </span>

      <div class="projectCTA">
        <h2>Project's members</h2>

        <span :class="isMember ? 'hidden' : ''">
          <Button title="Add" type="primary" ghost class="btn-group-right" @click="memberToAdd = true">Add member</Button>
          <Button title="Edit" type="primary" class="btn-group-right" ghost>Edit project</Button>
          <Button title="Delete project" class="btn-group-right" type="error">Remove project</Button>
        </span>
      </div>

      <table class="adminTable" aria-label="Project's members name and role">
        <thead>
          <tr>
            <th>Name</th>
            <th>Role</th>
            <th></th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(member, index) in usersList" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
            <td >{{ member.login }}</td>
            <td>{{ getUserRole(member.scopes) }}</td>
            <td class="table-cta">
              <Icon type="md-close-circle" class="crossIcon" size="24" @click="removeUserFromProject()" :class="isMember ? 'hidden' : ''"/>
              <Icon type="md-create" size="24" @click="openProjectDetails(project)" :class="isMember ? 'hidden' : ''"/>
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
          <RadioGroup v-model="formValidate.scope">
            <Radio v-for="user in isAlreadyMember" class="ivu-radio-border" :label="user"></Radio>
          </RadioGroup>
        </FormItem>
        <Form-item class="modal-cta">
          <Button @click="memberToAdd = false">Cancel</Button>
          <Button type="primary" @click="handleSubmit('formValidate')">Add</Button>
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
        users: [],
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
      newRowData () {
        let row = {}
        for (let field of this.fields) {
          row[field.code] = field.newValue
        }
        return row
      },

      async getAllUsers () {
        await Vue.http
          .get(api.paths.allUsers, api.REQUEST_OPTIONS)
          .then((response) => { this.usersList = response.body })
      },

      async getScopedUsers () {
        await Vue.http
          .get(api.paths.scopedUsersByProject(this.projectCode), api.REQUEST_OPTIONS)
          .then((response) => {
            this.usersList = response.body
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
              .then(() => this.$Message.success('User successfully added to ' + this.projectCode))
          } else {
            this.$Message.error('Something went wrong, please contact an admin')
          }
        })
      }
    },

    computed: {
      ...mapState('users', ['user']),

      isAlreadyMember () {
        const newUser = this.users?.map(item => item.login)
        return this.usersList.filter(item => newUser.some(itemToBeRemoved => itemToBeRemoved !== item))
      },

      isSuperAdmin () {
        if (this.user.profile === USER.PROFILE.SUPER_ADMIN) {
          return true
        }
      },

      isMember () {
        if (this.$route.params.role === USER.ROLE_ON_PROJECT.MEMBER) {
          return true
        }
      }
    },

    mounted () {
      this.$store.dispatch('users/getUserInfo', AuthenticationService.getDetails().user)
      this.projectName = this.$route.params.projectName
      this.projectCode = this.$route.params.projectCode
      if (this.isSuperAdmin) {
        this.getAllUsers()
      } else {
        this.getScopedUsers()
      }
      Vue.http
        .get(api.paths.scopedUsersByProject(this.projectCode), api.REQUEST_OPTIONS)
        .then((response) => {
          this.users = response.body
        })
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