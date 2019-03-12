<template>
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
</template>

<script>
  export default {
    name: 'functionality-menu',

    computed: {
      links () {
        return [
          { name: 'COVERAGE', routeName: 'functionality-coverage' },
          { name: 'CARTOGRAPHY', routeName: 'functionality-cartography' },
          { name: 'ASSIGNATIONS', routeName: 'functionality-assignations' }
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
