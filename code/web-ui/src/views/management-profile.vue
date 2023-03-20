<template>
  <div class="tableContent">
    <h1 class="adminTitle">Profile management</h1>

    <div class="profile-infos">
      <p class="profile-header-left"><span class="user-avatar" :style="{ backgroundImage: `url(${userInfo.pictureUrl})` }"></span><strong>{{ userInfo.login }}</strong></p>
      <p class="profile-header-right"><strong>{{ $t('profile.' + userInfo.profile) }}</strong></p>
      <p class="user-details">
        <span><Icon type="md-globe" size="16" />Connected via <strong>{{ userInfo.providerName }}</strong></span>
        <span><Icon type="md-mail" size="16" /> <strong>{{ userInfo.email }}</strong></span>
      </p>
    </div>

    <Tabs class="adminTable" type="card" v-model="activeTab" :animated="false">
      <TabPane label="Scopes management">
        <table class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th v-if="isNotAdmin">Role</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="projects in filteredScopes"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
              :key="projects.id"
            >
              <td>
                {{ projects.name }}
              </td>
              <td v-if="isNotAdmin">
                {{ projects.role }}
              </td>
              <td class="table-cta" align="right">
                <Icon type="md-eye" size="24" @click="openProjectDetails(projects)" />
                <Icon type="md-close-circle" size="24" @click="deleteProject(projects.code)" />
              </td>
            </tr>
          </tbody>
        </table>
      </TabPane>
      <TabPane label="Groups management">
        <table v-if="userGroups.length > 0" class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="(group, index) in userGroups" :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ group.name }}</p>
              </td>
              <td class="table-cta" align="right">
                <Icon type="md-eye" size="24" @click="openGroupDetails(group)" />
                <Icon type="md-close-circle" size="24" @click="deleteGroup(group.name)" />
              </td>
            </tr>
          </tbody>
        </table>

        <p v-else>You don't have any groups to manage yet.</p>
      </TabPane>
    </Tabs>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import { USER } from '../libs/constants'
  
  export default {
    name: 'management-profile',

    data () {
      return {
        userInfo: [],
        userGroups: [],
        projectList: [],
        user: USER
      }
    },

    methods: {
      getUserInfos () {
        Vue.http
          .get(api.paths.currentUser, api.REQUEST_OPTIONS)
          .then((user) => {
            this.userInfo = user.body
            Vue.http
              .get(api.paths.allGroups, api.REQUEST_OPTIONS)
              .then((response) => {
                this.userGroups = response.body
              })
          })
      },

      getProjects () {
        Vue.http
          .get(api.paths.projects, api.REQUEST_OPTIONS)
          .then((project) => { this.projectList = project.body })
      },

      openProjectDetails (project) {
        this.$router.push({ name: 'admin-project-details', params: { projectCode: project.code, projectName: project.name, userRole: this.userInfo.profile } })
      },

      deleteProject (code) {
        let self = this
        this.$Modal.confirm({
          title: 'Delete a project',
          content: `<p>Do you really want to delete <strong>` + code + `</strong>?</p>`,
          okText: 'Delete',
          loading: true,
          onOk () {
            Vue.http
              .delete(api.paths.projectByCode(code), api.REQUEST_OPTIONS)
              .then(() => {
                self.$Modal.remove()
                self.showLoader = true
                setTimeout(() => {
                  self.showLoader = false
                  self.getUserInfos()
                }, 500)
              }, (error) => {
                self.showLoader = false
                api.handleError(error)
              })
          }
        })
      },

      openGroupDetails (group) {
        this.$router.push({ name: 'group-details', params: { groupName: group.name } })
      }
    },

    computed: {
      filteredScopes () {
        if (this.isNotAdmin) {
          return this.projectList.filter(item => this.userInfo.scopes.some(p => p.project === item.code))
        } else {
          return this.projectList
        }
      },

      filteredGroups () {
        if (this.isNotAdmin) {
          Vue.http
            .get(api.paths.groupsManagedByUser(this.userInfo.login), api.REQUEST_OPTIONS)
            .then((response) => {
              this.userGroups = response.body
              return this.userGroups
            })
        } else {
          return this.userGroups
        }
      },

      isNotAdmin () {
        return this.userInfo.profile !== USER.PROFILE.SUPER_ADMIN
      }
    },

    created () {
      this.getUserInfos()
      this.getProjects()
    }
  }
</script>

<style scoped>
  .tab-content {
    width: 100%;
    border-collapse: collapse;
  }

  .profile-infos {
    display: flex;
    flex-direction: column;
    background-color: #ffffff;
    width: 18%;
    height: 180px;
    margin: auto;
    border: 2px solid #cdcdcd;
    border-radius: 5px;
    padding: 10px;
    position: relative;
  }

  .profile-infos .profile-header-left {
    position: absolute;
    top: 5px;
    left: 10px;
    display: flex;
    align-items: center;
  }

  .profile-infos .profile-header-right {
    position: absolute;
    top: 15px;
    right: 10px;
  }

  .profile-header-right i {
    color: #ef8854;
    margin-right: 10px;
  }

  .user-details {
    display: flex;
    padding-top: 70px;
    flex-direction: column;
    padding-left: 15px;
    text-align: left;
  }

  .user-details i, .profile-header-left, .profile-header-right {
    color: #0182c3;
  }

  .user-details i {
    margin-right: 10px;
  }

  .user-details > * {
    margin-bottom: 20px;
  }
 
  .user-avatar {
    display: flex;
    align-items: center;
  }

  .user-avatar {
    display: block;
    width: 38px;
    height: 38px;
    background-size: cover;
    border-radius: 100px;
    margin: 0 5px;
  }
</style>