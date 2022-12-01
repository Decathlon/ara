<script setup>
import { VtmnProgressbar } from "@vtmn/vue";

const props = defineProps(["cardActive", "index", "sanityGroup", "cardInfo"]);

const emit = defineEmits(["cardSelected"]);
</script>

<template>
  <div
    class="vtmn-flex vtmn-justify-center vtmn-items-baseline"
    :class="props.cardActive ? ' vtmn-mt-8' : 'vtmn-mt-4'"
  >
    <VtmnProgressbar
      v-if="!props.cardActive"
      variant="circular"
      size="small"
      status="determinate"
      loadingText=""
      class="vtmn-cursor-pointer"
      :class="index === sanityGroup.length - 1 ? 'lastProgressBar' : ''"
      :value="Math.floor(Math.random() * 100) + 1"
      @click="emit('cardSelected', props.cardInfo)"
    />

    <VtmnProgressbar
      v-else
      variant="circular"
      size="medium"
      status="determinate"
      loadingText=""
      :class="index === sanityGroup.length - 1 ? 'lastProgressBar' : ''"
      :value="Math.floor(Math.random() * 100) + 1"
      @click="emit('cardSelected', props.cardInfo)"
    />
  </div>
  <div class="vtmn-flex vtmn-justify-center vtmn-mt-2 vtmn-mb-4">
    <p
      class="validTasks vtmn-flex vtmn-items-center vtmn-m-1 vtmn-px-1"
      :class="props.cardActive ? 'vtmn-typo_text-1' : 'vtmn-typo_caption-1'"
    >
      <span>49</span>
      <i class="vtmx-shield-check-fill vtmn-justify-end vtmn-pl-1"></i>
    </p>
    <p
      class="errorTasks vtmn-flex vtmn-items-center vtmn-m-1 vtmn-px-1"
      :class="props.cardActive ? 'vtmn-typo_text-1' : 'vtmn-typo_caption-1'"
    >
      <span>3</span>
      <i class="vtmx-close-circle-fill vtmn-justify-end vtmn-pl-1"></i>
    </p>
  </div>
</template>
