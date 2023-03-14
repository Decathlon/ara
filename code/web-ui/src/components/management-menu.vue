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
    <ul class="subMenu ivu-menu ivu-menu-light ivu-menu-horizontal">
      <router-link v-for="(link,index) in links" :key="link.name" :to="to(link)"
                  class="ivu-menu-item" :class="$route.path.includes([link.routeName]) ? 'ivu-menu-item-active ivu-menu-item-selected' 
                  : userAuthorization && link.name === 'SETTINGS' ? 'hidden' 
                  : userAuthorization && link.name === 'TECHNOLOGIES' ? 'hidden'
                  : ''"
      >
        {{link.name}}
      </router-link>
    </ul>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { AuthenticationService } from '../service/authentication.service'
  import { USER } from '../libs/constants'

  export default {
    name: 'management-menu',

    data () {
      return {
        links: [
          // Safe & frequent modifications
          { routeName: 'management-communications', name: 'COMMUNICATIONS' },

          // Safe modifications
          { routeName: 'management-teams', name: 'TEAMS' },
          { routeName: 'management-root-causes', name: 'ROOT CAUSES' },

          // Dangerous initialization-time configurations
          { routeName: 'management-sources', name: 'SOURCES' },
          { routeName: 'management-types', name: 'TYPES' },
          { routeName: 'management-countries', name: 'COUNTRIES' },
          { routeName: 'management-severities', name: 'SEVERITIES' },
          { routeName: 'management-cycle-definitions', name: 'CYCLES' },

          // FOR TESTING, FOR NOW
          { routeName: 'management-technologies', name: 'TECHNOLOGIES' },
          { routeName: 'management-settings', name: 'SETTINGS' }
        ],
        userRole: null
      }
    },

    methods: {
      to (link) {
        return {
          name: link.routeName,
          params: {
            projectCode: this.$route.params.projectCode
          }
        }
      }
    },

    created () {
      this.userRole = AuthenticationService.getDetails().user
    },

    computed: {
      ...mapState('users', ['userRole']),

      projectSelected () {
        return this.$route.params.projectCode
      },

      userAuthorization () {
        const user = this.userRole.scopes.find((item) => item.project === this.projectSelected)
        if (user?.role === USER.ROLE_ON_PROJECT.MEMBER) {
          return true
        }
      }
    }
  }
</script>

<style scoped>
  .ivu-menu-item.hide, .hidden {
    display: none;
  }
</style>
