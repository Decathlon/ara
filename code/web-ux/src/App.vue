<script setup>
import headerNav from "./components/Nav/TheHeader.vue";
import sideNav from "./components/Nav/TheSideNav.vue";
import { ref } from "vue";

let sideOpen = ref(false);

const openSidemenu = () => {
  sideOpen.value = !sideOpen.value;
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
