<script>
import headerNav from "./components/headerNav.vue";
import sideNav from "./components/sideNav.vue";
import Mgr from "./services/SecurityService";

export default {
  name: "App",

  data() {
    return {
      mgr: new Mgr(),
      signedIn: true,
      sideOpen: false,
    };
  },

  components: {
    headerNav,
    sideNav,
  },

  mounted() {
    this.mgr.getSignedIn().then(
      (signIn) => {
        this.signedIn = signIn;
      },
      (err) => {
        console.log(err);
      }
    );
  },

  methods: {
    openSidemenu() {
      this.sideOpen = !this.sideOpen;
    },
  },
};
</script>

<template>
  <headerNav @opened-menu="openSidemenu" v-if="this.$route.name !== 'login'" />
  <sideNav :sideOpen="sideOpen" v-if="this.$route.name !== 'login'" />

  <RouterView />
</template>
