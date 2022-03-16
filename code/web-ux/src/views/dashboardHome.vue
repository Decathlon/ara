<script>
export default {
  data() {
    return {
      breadCrumb: this.$route.fullPath,
    };
  },
};
</script>

<template>
  <headerNav @opened-menu="openSidemenu" />
  <sideNav :sideOpen="sideOpen" />
  <div class="block vtmn-flex vtmn-pt-10 vtmn-ml-10">
    <div class="block">
      <nav aria-label="Breadcrumb" class="vtmn-breadcrumb">
        <ol>
          <li>
            <a href="">{{ breadCrumb }}</a>
          </li>
        </ol>
      </nav>
    </div>
  </div>
</template>
