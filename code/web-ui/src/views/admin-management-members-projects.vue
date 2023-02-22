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
      <div v-if="this.getMemberInfo.scopes.length > 0">
        <div v-if="isSuperAdmin || isAdmin" class="projectCTA">
          <h2>Project's roles</h2>

          <Button type="primary" class="btn-group-right" @click="userToProject = true">
            Affect to a new project
          </Button>
        </div>
        <table class="adminTable" aria-label="User's project and his role for each of them">
          <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
              <th></th>
            </tr>
          </thead>
          <tbody v-if="members">
            <tr v-for="(scope, index) in (isAdmin ? sortedProjects : this.getMemberInfo.scopes)" :key="index" :class="index %2 !== 0 ? 'lightGrey' : 'darkGrey'">
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
                  <Icon type="md-close-circle" size="32" @click="changeProfile({
                    remove: true,
                    project: scope.project
                  })" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="projectCTA">
        <span>
          <Alert type="warning">This member is not affected to any project yet. Click the button below:</Alert>
          <Button type="primary" @click="userToProject = true" >
            Affect to a new project
          </Button>
        </span>
      </div>
    </div>

    <Modal v-model="userToProject" title="Affect to a new project" okText="Add" footer-hide @on-ok="changeProfile" @close="userToProject = false" :width="900"
      :loading="loadingSaving" ref="editPopup">
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="128">
        <FormItem>
          <Alert>You are about to add <strong>{{ getMemberInfo.login }}</strong><span v-if="formValidate.project"> to <strong>{{ formValidate.project }}</strong></span></Alert>
        </FormItem>
        <FormItem label="Project" prop="project">
          <Select v-model="formValidate.project" filterable>
            <Option v-for="item in projectAvailable" :value="item.project" :key="item.project">{{ item.project }}</Option>
          </Select>
        </FormItem>
        <FormItem label="Role" prop="role">
          <RadioGroup v-model="formValidate.role">
            <Radio v-for="role in userRole" class="ivu-radio-border" :label="role"></Radio>
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
  import { USER } from '../libs/constants'

  export default {
    name: 'admin-management-members',
    components: {
      formField
    },
    data () {
      return {
        memberInfo: [],
        members: [],
        userToProject: false,
        editingData: {},
        editingNew: false,
        editing: false,
        groupName: this.$route.query.groupName,
        groupMembers: [],
        memberType: localStorage.getItem('memberType'),
        currentProfile: '',
        userRole: ['Member', 'Maintainer', 'Admin'],
        formValidate: {
          project: '',
          role: ''
        },
        ruleValidate: {
          project: [
            { required: true, message: 'You need to choose a project', trigger: 'blur' }
          ],
          role: [
            { required: true, message: 'You need to assign your user a role', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      changeProfile (changeType) {
        const newRole = {
          role: changeType.role,
          project: changeType.project
        }
        if (changeType.remove) {
          Vue.http
            .delete(api.paths.userProjectScopeManagement(this.getMemberInfo.login, changeType.project), api.REQUEST_OPTIONS)
            .then(() => this.getMemberInfo)
        } else {
          Vue.http
            .put(api.paths.userProjectScopeManagement(changeType.member.login, changeType.project), newRole, api.REQUEST_OPTIONS)
            .then((response) => this.getMemberInfo)
        }
      },

      handleSubmit (name) {
        this.$refs[name].validate((valid) => {
          if (valid) {
            const update = {
              project: this.formValidate.project,
              role: this.formValidate.role.toUpperCase(),
              member: {
                login: this.getMemberInfo.login
              }
            }
            this.changeProfile(update)
          } else {
            this.$Message.error({
              content: 'Please fill all required fields',
              duration: 2
            })
          }
        })
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
      },

      sortedProjects () {
        const Array = this.currentProfile.scopes.filter(project => this.getMemberInfo.scopes.some(scope => scope.project === project.project))
        const newArray = Array.filter(project => project.role === USER.ROLE_ON_PROJECT.ADMIN)

        return this.getMemberInfo.scopes.filter(scope => newArray.some(item => item.project === scope.project))
      },

      projectAvailable () {
        const projectsSet = this.sortedProjects.map(item => item.project)
        const projects = this.currentProfile.scopes.filter(item => !projectsSet.includes(item.project))
        const newProject = projects.filter(project => project.role === USER.ROLE_ON_PROJECT.ADMIN)

        return newProject.sort((a, b) => a.project.localeCompare(b.project))
      },

      isSuperAdmin () {
        return this.currentProfile.profile === USER.PROFILE.SUPER_ADMIN
      },

      isAdmin () {
        return this.currentProfile.profile === USER.PROFILE.SCOPED_USER
      }
    }
  }
</script>

<style scoped>
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
        display: flex;
        justify-content: space-evenly;
    }
    .remove-member-project-btn {
      cursor: pointer;
    }
</style>
