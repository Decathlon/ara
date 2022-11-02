<script>
import { VtmnBreadcrumb, VtmnBreadcrumbItem } from "@vtmn/vue";

export default {
  components: {
    VtmnBreadcrumb,
    VtmnBreadcrumbItem,
  },

  data() {
    return {};
  },

  methods: {
    breadCrumbChange() {},
  },
};
</script>

<template>
  <div class="block vtmn-pt-10 vtmn-ml-10">
    <VtmnBreadcrumb aria-label="Breadcrumb">
      <VtmnBreadcrumbItem
        icon="home-line"
        @click="this.$router.push({ path: '/quality-validation' })"
        >Home</VtmnBreadcrumbItem
      >
      <VtmnBreadcrumbItem aria-current="page"
        >Quality Details</VtmnBreadcrumbItem
      >
    </VtmnBreadcrumb>
  </div>
</template>
