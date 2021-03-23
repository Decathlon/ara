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
        <router-link :to="{ name: 'redirecter' }" id="home-logo">
          <Tooltip placement="bottom-start" :transfer="true">
            <div slot="content">
              ARA - AGILE REGRESSION ANALYZER<br>
              Fighting Against Regressions All Together
            </div>
            <img src="../assets/favicon-white.png" width="32" height="32"/></Tooltip></router-link><!-- No space between!
     --><projects-select :ghost="true" v-on:projectSelection="projectSelection" style="flex: 1 0 auto; margin-right: 14px;"/>
      </div>

      <div style="flex: 1 0 auto;">
        <!-- After deleting the demo project, if no other project exists, `projectCode` still exists but we should hide the menu anyway  -->
        <ul v-if="projectCode && projects && projects.length" class="ivu-menu ivu-menu-primary ivu-menu-horizontal">
          <router-link v-for="link in links" :key="link.name" :to="to(link)"
                      class="ivu-menu-item" active-class="ivu-menu-item-active ivu-menu-item-selected">
            {{link.name}}
          </router-link>
        </ul>
      </div>

      <div id="helps">
        <span v-if="isLoggedIn" style="margin-right: 10px" class="user-avatar">
          <Tooltip placement="bottom">
            <Avatar v-if="user && user.picture" :src="user.picture" size="large" />
            <Avatar v-else icon="md-person" style="color: #0082c3;background-color: white" size="large"/>
            <div slot="content">
              <p v-if="provider && provider.name">Connected via <strong>{{provider.name}}</strong></p>
              <p v-if="user && user.login">> Login: <strong>{{user.login}}</strong></p>
              <p v-if="user && user.name">> Name: <strong>{{user.name}}</strong></p>
              <p v-if="user && user.email">> Email: <strong>{{user.email}}</strong></p>
            </div>
          </Tooltip>
        </span>
        <Tooltip placement="bottom-end">
          <span>V{{ appVersion }}</span>
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
          <a :href="'https://github.com/decathlon/ara/blob/master/doc/user/main/UserDocumentation.adoc'"
             target="_blank"><Icon type="md-help-circle" size="24" style="padding: 0;"/></a>
        </Tooltip><!-- No space between items -->
        <Dropdown trigger="click" placement="bottom-start">
          <a><Icon type="md-settings" size="24"/></a>
          <DropdownMenu slot="list">
            <div class="parameters-box">
              <div class="parameter-line">
                <div class="parameter-title">Media display</div>
                <div class="parameter-switch">
                  <span>Open in <strong>{{ isMediaDisplayedOnSamePage ? "the same page" : "a new tab" }}</strong></span>
                  <i-switch v-model="isMediaDisplayedOnSamePage" @on-change="saveMediaDisplayState"/>
                </div>
                <div class="parameter-description">
                  <Alert closable>
                    Sometimes videos and images can't be displayed in the same page (e.g. mixed content: Ara runs in a secure server (HTTPS) but media urls are not secure (HTTP)).
                    You can fix this issue by switching off this option.
                  </Alert>
                </div>
              </div>
            </div>
          </DropdownMenu>
        </Dropdown>
        <Tooltip content="What's new in ARA?" placement="bottom-end" :transfer="false">
          <a :href="'https://github.com/Decathlon/ara/releases/tag/ara-' + appVersion"
             @click="setLatestChangelogVersion"
             target="_blank"><Badge dot :count="changelogCount"><Icon type="md-notifications" size="24"/></Badge></a>
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
        isMediaDisplayedOnSamePage: true,
        appVersion: undefined,
        apiVersion: undefined,
        webUIVersion: undefined,
        latestChangelogVersion: this.getCookie(LATEST_CHANGELOG_VERSION_COOKIE_NAME),
        projectCode: this.$route.params.projectCode || this.defaultProjectCode,
        isLoggedIn: AuthenticationService.isAlreadyLoggedIn()
      }
    },

    computed: {
      provider () {
        const authenticationDetails = this.getAuthenticationDetails()
        if (authenticationDetails) {
          return authenticationDetails.provider
        }
      },

      user () {
        const authenticationDetails = this.getAuthenticationDetails()
        if (authenticationDetails) {
          return authenticationDetails.user
        }
      },

      ...mapState('projects', [
        'projects',
        'defaultProjectCode'
      ]),

      links () {
        return [
          { params: { projectCode: this.projectCode }, name: 'EXECUTIONS & ERRORS', routeName: 'executions' },
          { params: { projectCode: this.projectCode }, name: 'PROBLEMS', routeName: 'problems' },
          { params: { projectCode: this.projectCode }, name: 'FUNCTIONALITIES', routeName: 'functionalities' },
          { params: { projectCode: this.projectCode }, name: 'SCENARIOS', routeName: 'scenario-writing-helps' }
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
      loadLocalParameters () {
        this.isMediaDisplayedOnSamePage = LocalParameterService.isMediaDisplayedOnSamePage()
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
        return AuthenticationService.getDetails()
      },

      logout () {
        AuthenticationService.logout()
      }
    },

    mounted () {
      this.loadLocalParameters()
      Vue.http
        .get(api.paths.info(), api.REQUEST_OPTIONS)
        .then((response) => {
          this.appVersion = response.data.app.version
          this.webUIVersion = response.data['web-ui'].version
          this.apiVersion = response.data.api.version
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
  #helps {
    flex: 0 1 auto;
    text-align: right;
    line-height: calc(30px - 14px);
    font-size: 14px;
    padding-right: 8px;
    white-space: nowrap;
  }

  #helps > span {
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

  .parameters-box {
    margin: 10px;
    width: 300px;
  }

  .parameter-line {
    margin-bottom: 5px;
  }

  .parameter-title {
    text-align: center;
    font-weight: bold;
    margin-bottom: 5px;
  }

  .parameter-description {
    margin-top: 10px;

    text-align: start;
    white-space: initial;
    font-size: 12px;
    font-style: italic;
  }

  .parameter-switch {
    display: flex;
    flex-flow: row wrap;
    justify-content: space-between;
    align-items: center;
  }
</style>
