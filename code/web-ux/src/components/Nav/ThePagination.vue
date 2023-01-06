<script setup>
import { computed } from "vue";
import { VtmnIcon } from "@vtmn/vue";
import { useRoute } from "vue-router";

const emit = defineEmits(["page-change"]);
const props = defineProps(["cardsLength", "tabLimit", "pageNumber"]);
const route = useRoute();

const changePage = (cards) => {
  emit("page-change", cards);
};

const currentPage = computed(() =>
  route.query.page ? JSON.parse(route.query.page) : 1
);
</script>

<template>
  <div class="vtmn-w-full vtmn-mt-4">
    <div
      class="vtmn-flex vtmn-float-left vtmn-align-middle vtmn-typo_text-2 pagination-details"
    >
      <p>
        Showing 1 to {{ props.tabLimit }} of {{ props.cardsLength }} results
      </p>
    </div>

    <div
      class="pagination vtmn-flex vtmn-rounded-md vtmn-float-right vtmn-align-middle"
    >
      <VtmnIcon
        value="chevron-left-fill"
        class="vtmn-px-4 vtmn-py-2 vtmn-m-auto vtmn-cursor-pointer"
        :size="24"
      ></VtmnIcon>
      <span
        v-for="(cards, index) in pageNumber"
        :key="index"
        class="vtmn-px-4 vtmn-py-2 vtmn-relative vtmn-cursor-pointer"
        @click="changePage(cards)"
        :class="currentPage === cards ? 'current' : 'unactive'"
        >{{ cards }}
      </span>
      <VtmnIcon
        value="chevron-right-fill"
        class="vtmn-px-4 vtmn-py-2 vtmn-m-auto vtmn-cursor-pointer unactive-nav"
        :size="24"
      ></VtmnIcon>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  background-color: var(--vtmn-semantic-color_background-primary);
  box-shadow: 0 2px 3px 0 #e7e7e7;
}

.pagination .unactive {
  color: var(--vtmn-semantic-color_content-inactive);
}

.pagination .unactive-nav {
  color: var(--vtmn-semantic-color_content-inactive) !important;
}

.current::before {
  content: "";
  position: absolute;
  width: 40px;
  height: 40px;
  left: 0;
  top: 0;
  border: 2px solid var(--vtmn-semantic-color_content-active);
  border-radius: 5px;
}

.pagination-details {
  line-height: 44px;
  color: var(--vtmn-semantic-color_content-secondary);
}
</style>
