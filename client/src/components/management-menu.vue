<template>
  <div>
    <!--
      iView's Menu cannot Ctrl+click to open in a new tab,
      and we must compute active-name by ourself instead of relying on router-link putting the active-class on correct items.
      So we use Menu classes with router-links.
    -->
    <ul class="subMenu ivu-menu ivu-menu-light ivu-menu-horizontal">
      <router-link v-for="link in links" :key="link.name" :to="to(link)"
                   class="ivu-menu-item" active-class="ivu-menu-item-active ivu-menu-item-selected">
        {{link.name}}
      </router-link>
    </ul>
  </div>
</template>

<script>
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
          { routeName: 'management-settings', name: 'SETTINGS' }
        ]
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
    }
  }
</script>
