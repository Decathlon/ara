<template>
  <div class="tableContent">
    <h1 class="adminTitle">Profile management</h1>

    <div v-for="user in userInfo" class="profile-infos">
      <p class="profile-header-left"><span class="user-avatar" :style="{ backgroundImage: `url(${user.pictureUrl})` }"></span><strong>{{ user.login }}</strong></p>
      <p class="profile-header-right"><Icon type="md-create" size="16" /><strong>{{ $t('profile.' + user.profile) }}</strong></p>
      <p class="user-details">
        <span><Icon type="md-globe" size="16" />Connected via <strong>{{ user.providerName }}</strong></span>
        <span><Icon type="md-mail" size="16" /> <strong>{{ user.email }}</strong></span>
      </p>
    </div>

    <Tabs class="adminTable" type="card" v-model="activeTab" :animated="false">
      <TabPane label="My projects">
        <table class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
              <th></th>
            </tr>
          </thead>

          <tbody v-for="(user, index) in userInfo" :key="index">
            <tr
              v-for="projects in user.scopes"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                {{ projects.project }}
              </td>
              <td>
                {{ projects.role }}
              </td>
              <td class="table-cta" align="right">
                <Icon type="md-eye" size="24" @click="openProjectDetails(projects)" />
                <Icon type="md-close-circle" size="24" @click="deleteProject(projects.project)" />
              </td>
            </tr>
          </tbody>
        </table>
      </TabPane>
      <TabPane label="My groups">
        <table class="tab-content" aria-label="Group's management">
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
      </TabPane>
    </Tabs>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  
  export default {
    name: 'management-profile',

    data () {
      return {
        userInfo: [],
        userGroups: []
      }
    },

    methods: {
      getUserInfos () {
        Vue.http
          .get(api.paths.currentUser, api.REQUEST_OPTIONS)
          .then((user) => {
            this.userInfo.push(user.body)
            Vue.http
              .get(api.paths.groupsManagedByUser(user.body.login), api.REQUEST_OPTIONS)
              .then((groups) => { this.userGroups = groups.body })
          })
      },

      openProjectDetails (project) {
        this.$router.push({ name: 'admin-project-details', params: { projectCode: project.project, projectName: project.name, userRole: this.user.profile } })
      },

      openGroupDetails () {

      }
    },

    created () {
      this.getUserInfos()
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
    padding-left: 30px;
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