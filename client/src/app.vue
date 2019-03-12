<template>
  <div id="app" :class="isFramed ? 'isFramed' : ''">
    <top-menu v-if='!isFramed' style="position: fixed; top: 0; left: 0; width: 100%; z-index: 1000;"></top-menu>
    <div class="layout-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
  import topMenu from './components/top-menu.vue'

  export default {
    name: 'app',

    components: {
      topMenu
    },

    computed: {
      isFramed () {
        // The VueJS application will start by loading '/' and THEN will load the requested view component
        // We do not want the menu to flicker (appear for just a few milliseconds) when loading framed window
        // '/' is always a temporary state: a redirection to a default or requested screen will happen shortly
        return this.$route.fullPath === '/' || this.$route.matched.some(record => record.meta.isFramed)
      }
    },

    mounted () {
      this.$store.dispatch('teams/ensureTeamsLoaded', this)
    },

    watch: {
      '$route' (to, from) {
        if (to.params.projectCode !== from.params.projectCode) {
          this.$store.dispatch('teams/ensureTeamsLoaded', this)
        }
      }
    }
  }
</script>

<style>
  @import './styles/common.css';

  html {
    height: 100%;
  }

  #app:not(.isFramed) {
    margin-top: 60px;
    height: calc(100% - 60px);
    min-height: 100%;
  }

  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2c3e50;
  }

  .layout-content {
    padding: 16px;
  }
</style>
