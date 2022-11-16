<script>
export default {
  data() {
    return {
      openedMenu: false,
    };
  },

  watch: {
    openedMenu() {
      this.$emit("opened-menu");
    },
  },
};
</script>

<template>
  <nav class="vtmn-navbar vtmn-fixed" aria-label="Navbar with icons">
    <div class="vtmn-navbar_left-navigation active">
      <button
        class="menu-btn vtmn-btn vtmn-btn--icon-alone vtmn-btn_variant--ghost vtmn-pl-0"
        aria-label="Go to menu"
        @click="openedMenu = !openedMenu"
      >
        <span class="vtmx-menu-line" role="presentation"></span>
      </button>
      <RouterLink to="/">
        <img src="../assets/img/logo.png" alt="ara logo" width="30" />
        <h5 class="vtmn-typo_title-5 vtmn-pl-1">ARA</h5>
      </RouterLink>
    </div>

    <div class="vtmn-w-2/5">
      <div class="nav-search-container vtmn-relative">
        <input
          type="text"
          class="nav-search vtmn-text-input vtmn-w-full"
          id="my-search-navbar1"
          placeholder="Search a project..."
        />
        <span class="vtmx-search-line"></span>
      </div>
    </div>
    <div class="vtmn-navbar_right-navigation">
      <span>V11.1.4</span>
      <a href="" class="vtmn-ml-2 vtmn-mr-2">
        <span class="vtmn-rounded-bg-icon vtmx-user-line"></span>
      </a>
      <a href="" class="vtmn-ml-2 vtmn-mr-2 vtmn-relative">
        <span class="vtmx-notification-line"></span>
        <span
          class="notification-circle vtmn-flex vtmn-absolute vtmn-h-3 vtmn-w-3 vtmn-top-2 vtmn-left-2 vtmn--mt-1 vtmn--mr-1"
        >
          <span
            class="vtmn-animate-ping vtmn-absolute vtmn-inline-flex vtmn-h-full vtmn-w-full vtmn-rounded-full vtmn-opacity-75"
          ></span>
          <span
            class="vtmn-relative vtmn-inline-flex vtmn-rounded-full vtmn-h-3 vtmn-w-3"
          ></span>
        </span>
      </a>
      <a href="" class="vtmn-ml-2 vtmn-mr-4">
        <span class="vtmx-shut-down-line"></span>
      </a>
    </div>
  </nav>
</template>

<style scoped>
.vtmn-navbar {
  box-shadow: 0px 0px 4px 0px #c1c1c1;
  padding: 0;
  height: 48px;
  z-index: 300;
}
.vtmn-typo_title-5 {
  color: var(--vtmn-semantic-color_content-inactive);
}

.notification-circle span {
  background-color: var(--vtmn-semantic-color_content-warning);
}

.vtmn-rounded-bg-icon {
  background-color: var(--vtmn-semantic-color_background-brand-primary);
  color: white;
  padding: 4px;
  border-radius: 100px;
}
</style>
