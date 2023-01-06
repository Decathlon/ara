<script setup>
import { VtmnBreadcrumb, VtmnBreadcrumbItem } from "@vtmn/vue";
import tableModel from "../components/Utility/tableModel.vue";
</script>

<template>
  <div class="vtmn-pt-10 vtmn-ml-10">
    <VtmnBreadcrumb aria-label="Breadcrumb">
      <VtmnBreadcrumbItem icon="home-line" @click="qualityView()">
        Home
      </VtmnBreadcrumbItem>
    </VtmnBreadcrumb>
    <h1 class="vtmn-text-center vtmn-typo_title-1 vtmn-mb-6">Features</h1>
    <tableModel />
  </div>
</template>
