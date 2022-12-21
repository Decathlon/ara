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
    <!--
      iView's Menu cannot Ctrl+click to open in a new tab,
      and we must compute active-name by ourself instead of relying on router-link putting the active-class on correct items.
      So we use Menu classes with router-links.
    -->
    <div style="background-color: #0082C3; display: flex;">
      <div style="flex: 0 0 auto;">
        <router-link v-if="!adminRight" :to="{ name: 'redirecter' }" id="home-logo">
          <Tooltip placement="bottom-start" :transfer="true">
            <div slot="content">
              ARA - AGILE REGRESSION ANALYZER<br>
              Fighting Against Regressions All Together
            </div>
            <img src="../assets/favicon-white.png" width="32" height="32"/></Tooltip></router-link><!-- No space between!
     --><projects-select :ghost="true" v-on:projectSelection="projectSelection" style="flex: 1 0 auto; margin-right: 10px;"/>
      </div>

      <div style="flex: 1 0 auto;">
        <!-- After deleting the demo project, if no other project exists, `projectCode` still exists but we should hide the menu anyway  -->
        <ul v-if="projectCode && projects && projects.length && !savedSingleUserConnections" class="ivu-menu ivu-menu-primary ivu-menu-horizontal dashboardHeader">
          <router-link v-for="link in links" @click.native="changeAdminState(link.routeName)" :key="link.name" :to="to(link)" class="ivu-menu-item" active-class="ivu-menu-item-active ivu-menu-item-selected">
            {{link.name}}
          </router-link>
        </ul>

        <ul v-else-if="projectCode && projects && projects.length && savedSingleUserConnections" class="ivu-menu ivu-menu-primary ivu-menu-horizontal dashboardHeader">
          <router-link v-for="link in adminMenu" class="ivu-menu-item" active-class="ivu-menu-item-active ivu-menu-item-selected" @click.native="changeAdminState(link.routeName)" :key="link.name" :to="to(link)" :class="link.name">
            {{link.name}}
          </router-link>
        </ul>
        <div :class="showSubMenuMembers ? 'membersSubMenu active' : 'membersSubMenu'">
          <p @click="showMembersType('individual')" :class="typeSelected == 'individual' ? 'selected' : ''">INDIVIDUAL</p>
          <p @click="showMembersType('machine')" :class="typeSelected == 'machine' ? 'selected' : ''">MACHINE</p>
        </div>
      </div>

      <div id="helps">
        <span v-if="isLoggedIn" style="margin-right: 10px" class="user-avatar">
          <Tooltip placement="bottom">
            <Avatar v-if="user && user.pictureUrl" :src="user.pictureUrl" size="large" />
            <Avatar v-else icon="md-person" style="color: #0082c3;background-color: white" size="large"/>
            <div slot="content">
              <p v-if="providerName">Connected via <strong>{{providerName}}</strong></p>
              <p v-if="user && user.login">> Login: <strong>{{user.login}}</strong></p>
              <p v-if="user && user.firstName && user.lastName">> Name: <strong>{{user.firstName + " " + user.lastName}}</strong></p>
              <p v-if="user && user.email">> Email: <strong>{{user.email}}</strong></p>
            </div>
          </Tooltip>
        </span>
        <Tooltip placement="bottom-end">
          <span id="current-version">V{{ appVersion }}</span>
          <div slot="content">
            <table aria-label="ARA versions">
              <tr v-if="webUIVersion">
                <th scope="row" style="text-align: right;">WEB-UI: </th>
                <td>{{ webUIVersion }}</td>
              </tr>
              <tr v-if="apiVersion">
                <th scope="row" style="text-align: right;">API: </th>
                <td>{{ apiVersion }}</td>
              </tr>
            </table>
          </div>
        </Tooltip>
        <!-- Keep the same width as logo+select: this is to center the menu when space is available -->
        <Tooltip content="How to use ARA?" placement="bottom-end" :transfer="true">
          <a href="https://github.com/Decathlon/ara/blob/main/doc/usage/main/UserDocumentation.adoc"
             rel="noopener" target="_blank"><Icon type="md-help-circle" size="24" style="padding: 0;"/></a>
        </Tooltip><!-- No space between items -->
        <Dropdown trigger="click" placement="bottom-end">
          <a><Icon type="md-settings" size="24"/></a>
          <DropdownMenu slot="list">
            <div class="parameters-box">
              <div class="parameter-box-title">Executed Scenarios</div>
              <div class="parameter-line">
                <div class="parameter-title">Media display</div>
                <div class="parameter-switch">
                  <Tooltip :transfer="true" placement="left">
                    <span class="parameter-switch-description">Open in <strong>{{ isMediaDisplayedOnSamePage ? "the same page" : "a new tab" }}</strong></span>
                    <div slot="content">
                      <p>Sometimes videos and images can't be displayed in the same page</p>
                      <p>(e.g. mixed content: Ara runs in a secure server (HTTPS) but media urls are not secure (HTTP)).</p>
                      <p>You can fix this issue by switching off this option.</p>
                    </div>
                  </Tooltip>
                  <i-switch v-model="isMediaDisplayedOnSamePage" @on-change="saveMediaDisplayState"/>
                </div>
              </div>
              <div class="parameter-line">
                <div class="parameter-title">History</div>
                <div class="parameter-switch">
                  <Tooltip content="Change this if you want to load fewer executed scenarios." :transfer="true" placement="left">
                    <span class="parameter-switch-description">{{selectedHistoryDurationDescription}}<strong></strong></span>
                  </Tooltip>
                  <i-switch v-model="duration.applied" @on-change="updateExecutedScenariosHistoryDuration"/>
                </div>
                <div class="parameter-inputs" v-if="duration.applied">
                  <InputNumber class="parameter-inputs-input" v-model="duration.value" controls-outside min="1" @on-change="updateExecutedScenariosHistoryDuration"></InputNumber>
                  <Select v-model="duration.type" filterable @on-change="updateExecutedScenariosHistoryDuration" :transfer="true">
                    <Option v-for="durationType in duration.availableTypes" :value="durationType.value" :key="durationType.value">{{durationType.label + (duration.value > 1 ? 's' : '')}}</Option>
                  </Select>
                </div>
              </div>
            </div>
          </DropdownMenu>
        </Dropdown>
        <Tooltip content="What's new in ARA?" placement="bottom-end" :transfer="false">
          <a :href="$sanitizeUrl('https://github.com/Decathlon/ara/releases/tag/ara-' + channel + '-v' + appVersion)"
             @click="setLatestChangelogVersion"
             rel="noopener" target="_blank"><Badge dot :count="changelogCount"><Icon type="md-notifications" size="24"/></Badge></a>
        </Tooltip>
        <Tooltip v-if="isLoggedIn" content="Logout from ARA" placement="bottom-end">
          <a @click="logout()">
            <Icon type="md-exit" size="24"></Icon>
          </a>
        </Tooltip>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'

  import Vue from 'vue'
  import api from '../libs/api'

  import projectsSelect from '../components/projects-select.vue'

  import constants from '../libs/constants.js'

  import { AuthenticationService } from '../service/authentication.service'
  import { LocalParameterService } from '../service/local-parameter.service'
  import _ from 'lodash'

  // Will contain the latest version when the user clicked to view the CHANGELOG:
  // a red badge will appear on the CHANGELOG icon when a new version will be available
  const LATEST_CHANGELOG_VERSION_COOKIE_NAME = 'lcv'

  export default {
    name: 'top-menu',

    mixins: [{
      created () {
        this.constants = constants
      }
    }],

    components: {
      projectsSelect
    },

    data () {
      return {
        configuration: this.$appConfig,
        isMediaDisplayedOnSamePage: true,
        duration: {
          applied: false,
          value: 1,
          type: '',
          availableTypes: [
            {
              'value': 'DAY',
              'label': 'day'
            },
            {
              'value': 'WEEK',
              'label': 'week'
            },
            {
              'value': 'MONTH',
              'label': 'month'
            },
            {
              'value': 'YEAR',
              'label': 'year'
            }
          ]
        },
        appVersion: undefined,
        channel: undefined,
        apiVersion: undefined,
        webUIVersion: process.env.VERSION,
        latestChangelogVersion: this.getCookie(LATEST_CHANGELOG_VERSION_COOKIE_NAME),
        projectCode: this.$route.params.projectCode || this.defaultProjectCode,
        isLoggedIn: AuthenticationService.isAlreadyLoggedIn()
      }
    },

    computed: {
      ...mapState('admin', ['savedSingleUserConnections', 'showSubMenuMembers', 'typeSelected']),
      ...mapState('projects', ['projects', 'defaultProjectCode']),
      ...mapState('users', ['userRole']),

      executedScenariosHistoryDurationIsApplied () {
        return this.duration.applied && this.duration.value && this.duration.type
      },

      selectedHistoryDurationDescription () {
        let finalDescription = 'Select'
        let totalDurationDescription = 'all'
        const durationType = this.duration.type
        const durationValue = this.duration.value
        if (this.executedScenariosHistoryDurationIsApplied) {
          const plural = this.duration.value > 1 ? 's' : ''
          const durationTypeDescription = _(this.duration.availableTypes)
            .filter([ 'value', durationType ])
            .map('label')
            .first()
          totalDurationDescription = `the last ${durationValue} ${durationTypeDescription}${plural}`
        }
        finalDescription += ` ${totalDurationDescription} history`
        return finalDescription
      },

      providerName () {
        const authenticationDetails = this.getAuthenticationDetails()
        if (authenticationDetails) {
          return authenticationDetails.providerName
        }
      },

      user () {
        const authenticationDetails = this.getAuthenticationDetails()
        if (authenticationDetails) {
          return authenticationDetails.user
        }
      },

      links () {
        return [
          { params: { projectCode: this.projectCode }, name: 'EXECUTIONS & ERRORS', routeName: 'executions' },
          { params: { projectCode: this.projectCode }, name: 'PROBLEMS', routeName: 'problems' },
          { params: { projectCode: this.projectCode }, name: 'FUNCTIONALITIES', routeName: 'functionalities' },
          { params: { projectCode: this.projectCode }, name: 'SCENARIOS', routeName: 'scenario-writing-helps' },
          { params: { projectCode: this.projectCode }, name: 'SETTINGS', routeName: 'active-admin' }
        ]
      },
      adminMenu () {
        return [
          { params: { projectCode: this.projectCode }, name: 'PROJECTS', routeName: 'active-admin' },
          { params: { projectCode: this.projectCode }, name: 'MEMBERS', routeName: 'members' },
          { params: { projectCode: this.projectCode }, name: 'CONFIGURATION', routeName: 'management' },
          { params: { projectCode: this.projectCode }, name: 'DASHBOARD', routeName: 'dashboard' }
        ]
      },

      changelogCount () {
        if (this.appVersion === undefined || // Current app version not downloaded yet
            this.latestChangelogVersion === this.appVersion) { // User already clicked on the CHANGLOG button for the current app version
          return 0 // Do not show the badge dot
        }
        return 1 // Show a red badge dot
      }
    },

    methods: {
      loadExecutedScenariosHistoryDuration () {
        const simplifiedDuration = LocalParameterService.getExecutedScenariosHistoryDuration()
        this.duration.applied = !!simplifiedDuration
        if (simplifiedDuration) {
          this.duration.value = simplifiedDuration.value
          this.duration.type = simplifiedDuration.type
        }
      },

      updateExecutedScenariosHistoryDuration () {
        if (this.executedScenariosHistoryDurationIsApplied) {
          const simplifiedDuration = {
            value: this.duration.value,
            type: this.duration.type
          }
          LocalParameterService.saveExecutedScenariosHistoryDuration(simplifiedDuration)
          return
        }
        LocalParameterService.clearExecutedScenariosHistoryDuration()
      },

      loadLocalParameters () {
        this.isMediaDisplayedOnSamePage = LocalParameterService.isMediaDisplayedOnSamePage()
        this.loadExecutedScenariosHistoryDuration()
      },

      saveMediaDisplayState (displayOnSamePage) {
        LocalParameterService.saveMediaDisplayValue(displayOnSamePage)
      },

      projectSelection (projectCode) {
        this.projectCode = projectCode
      },

      to (link) {
        return {
          name: link.routeName,
          params: {
            projectCode: this.projectCode
          }
        }
      },

      /**
       * Get the value of a cookie by its name.
       *
       * @param name the name of the cookie to get
       * @return the cookie value
       */
      getCookie (name) {
        let value = '; ' + document.cookie
        let parts = value.split('; ' + name + '=')
        if (parts.length === 2) {
          return parts.pop().split(';').shift()
        }
        return null
      },

      /**
       * Set a cookie value, and expire in one year.
       *
       * @param name the name of the cookie to set
       * @param value the value of the cookie to set
       */
      setCookie (name, value) {
        let expirationDate = new Date()
        expirationDate.setFullYear(expirationDate.getFullYear() + 1)
        document.cookie = name + '=' + value + '; expires=' + expirationDate.toGMTString() + ';'
      },

      /**
       * If the current application version has been downloaded, memorize it in cookies
       * and in variable, to hide the red badge dot.
       */
      setLatestChangelogVersion () {
        if (this.appVersion) {
          this.latestChangelogVersion = this.appVersion
          this.setCookie(LATEST_CHANGELOG_VERSION_COOKIE_NAME, this.latestChangelogVersion)
        }
      },

      getAuthenticationDetails () {
        this.$store.dispatch('admin/setRole', AuthenticationService.getDetails().user.id)
        return AuthenticationService.getDetails()
      },

      extractVersion (data, property) {
        if (data?.hasOwnProperty(property)) {
          return data[property].version
        }
      },

      extractChannel (data) {
        if (data?.hasOwnProperty('app')) {
          return data['app'].channel
        }
      },

      changeAdminState (data) {
        if (data === 'members') {
          this.$store.dispatch('admin/setTypeSelected', '')
          this.$store.dispatch('admin/showSubMenuMembers', true)
          this.$store.dispatch('admin/showChoice', false)
        } else {
          this.$store.dispatch('admin/showSubMenuMembers', false)
          this.$store.dispatch('admin/enableAdmin', data)
        }
      },
      showMembersType (data) {
        this.$store.dispatch('admin/setTypeSelected', data)
        if (data) {
          this.$store.dispatch('admin/showChoice', data)
        }
      },

      logout () {
        AuthenticationService.logout()
      }
    },

    beforeUpdate () {
      if (localStorage.adminRight === 'true') {
        this.$store.dispatch('admin/enableAdmin', 'active-admin')
      }
    },

    mounted () {
      Vue.http.get('api/users')
        .then((response) => {
          this.$store.dispatch('users/getAllUsers', response.body)
        })
      this.loadLocalParameters()
      Vue.http
        .get(api.paths.info(), api.REQUEST_OPTIONS)
        .then((response) => {
          const data = response?.data
          this.appVersion = this.extractVersion(data, 'app')
          this.channel = this.extractChannel(data)
          this.apiVersion = this.extractVersion(data, 'api')
          // If it is the first time the user opens ARA, make sure to remember the current version for future notification badge
          if (!this.latestChangelogVersion) {
            this.setLatestChangelogVersion()
          }
        }, (error) => {
          api.handleError(error)
        })
    }
  }
</script>

<style scoped>
  @keyframes loadHeader {
    from { transform: translateY(65px); }
    to   { transform: translateY(0); }
  }

  #home-logo {
    display: inline-block;
    margin: 0 8px;
  }
  #home-logo img {
    margin: 14px calc(14px - 8px);
    vertical-align: middle;
  }
  .ivu-menu-horizontal {
    text-align: center;
  }
  .ivu-menu-horizontal .ivu-menu-item {
    float: none;
    display: inline-block;
  }

  .ivu-menu-horizontal .ivu-menu-item:last-child {
    float: right;
  }

  .selected-project-management div {
    background-color: #135b95;
    border-radius: 50%;
  }

  #helps {
    flex: 0 1 auto;
    text-align: right;
    line-height: calc(30px - 14px);
    font-size: 14px;
    padding-right: 8px;
    white-space: nowrap;
  }

  #current-version {
    color: white;
  }

  #helps a {
    display: block;
    color: white;
    padding: 18px 6px;
  }
  #home-logo,
  #helps a {
    transition: all .2s ease-in-out;
  }
  #home-logo:hover,
  #helps a:hover {
    background-color: #2B85E4;
  }

  .user-avatar:hover {
    cursor: pointer;
  }

  .parameter-box-title {
    text-align: center;
    font-weight: bold;
    font-size: 15px;
    margin-bottom: 10px;
    border-bottom: 1px lightgrey solid;
    padding-bottom: 5px;
  }

  .parameters-box {
    display: flex;
    flex-direction: column;
    margin: 10px;
    width: 300px;
  }

  .parameter-line {
    display: flex;
    flex-direction: column;
    margin-bottom: 5px;
  }

  .parameter-title {
    text-align: center;
    font-weight: bold;
    margin-bottom: 25px;
    background-color: rgb(0, 130, 195);
    color: white;
    padding: 5px 0px 5px 0px;
  }

  .parameter-switch {
    display: flex;
    flex-flow: row wrap;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .parameter-switch-description {
    cursor: pointer;
  }

  .parameter-inputs {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
  }

  .parameter-inputs .parameter-inputs-input {
    margin-right: 5px;
  }

  .dashboardHeader {
    overflow: hidden;
  }
  .dashboardHeader a {
    animation: loadHeader .3s ease-in-out;
  }
  .membersSubMenu {
    position: absolute;
    display: flex;
    background-color: #ffffff;
    top: 0;
    left: 0;
    right: 0;
    margin: 0 auto;
    justify-content: center;
    width: 200px;
    height: 60px;
    border-radius: 0 0 20px 20px;
    box-shadow: 1px 1px 3px 0px #cacaca;
    transition: 200ms;
  }
  .membersSubMenu.active {
    top: 60px;
    transition: 200ms;
  }
  .membersSubMenu p {
    line-height: 1;
    align-self: center;
    width: 100%;
    text-align: center;
    color: #8c8c8c;
    font-weight: 500;
    cursor: pointer;
  }
  .membersSubMenu .selected {
    color: #007DBC;
    font-weight: 900;
  }
</style>
