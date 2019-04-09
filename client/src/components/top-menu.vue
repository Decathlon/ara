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
        <!-- Keep the same width as logo+select: this is to center the menu when space is available -->
        <Tooltip content="How to use ARA?" placement="bottom-end" :transfer="true">
          <a :href="'https://github.com/decathlon/ara/blob/master/doc/user/main/UserDocumentation.adoc'"
             target="_blank"><Icon type="md-help-circle" size="24" style="padding: 0;"/></a>
        </Tooltip><!-- No space between items
     --><Tooltip content="What's new in ARA?" placement="bottom-end" :transfer="false">
          <a :href="'https://github.com/decathlon/ara/blob/master/CHANGELOG.adoc'"
             @click="setLatestChangelogVersion"
             target="_blank"><Badge dot :count="changelogCount"><Icon type="md-notifications" size="24"/></Badge></a>
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
        appVersion: undefined,
        latestChangelogVersion: this.getCookie(LATEST_CHANGELOG_VERSION_COOKIE_NAME),
        projectCode: this.$route.params.projectCode || this.defaultProjectCode
      }
    },

    computed: {
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
      }
    },

    mounted () {
      Vue.http
        .get(api.paths.info(), api.REQUEST_OPTIONS)
        .then((response) => {
          this.appVersion = response.data.app.version
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
    width: 274px;
    text-align: right;
    line-height: calc(30px - 14px);
    font-size: 14px;
    padding-right: 8px;
    white-space: nowrap;
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
</style>
