<script>
import headerNav from "./components/headerNav.vue";
import sideNav from "./components/sideNav.vue";

export default {
  name: "App",

  data() {
    return {
      signedIn: true,
      sideOpen: false,
    };
  },

  components: {
    headerNav,
    sideNav,
  },

  methods: {
    openSidemenu() {
      this.sideOpen = !this.sideOpen;
    },
  },
};
</script>

<template>
  <div>
    <headerNav
      @opened-menu="openSidemenu"
      v-if="this.$route.name !== 'login'"
    />
    <sideNav :sideOpen="sideOpen" v-if="this.$route.name !== 'login'" />

    <RouterView />
  </div>
</template>
