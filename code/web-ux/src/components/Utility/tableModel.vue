<script setup lang="ts">
import { VtmnSearch, VtmnSelect, VtmnIcon, VtmnCheckbox } from "@vtmn/vue";
import { reactive } from "vue";

const featuresList = reactive([
  {
    name: "Feature 1 children 1",
    type: "folder",
    parent: true,
    opened: 'false',
    id: 1,
    children: [
      {
        name: "Feature 1 children 2",
        type: "folder",
        opened: false,
        id: 2,
        parentId: 1,
        children: [
          {
            name: "Feature 1 children 3",
            team: "S&C / Merch & Search",
            priority: "High",
            coverage: "Started",
            type: "feature",
            id: 3,
            parentId: 2,
          },
        ],
      },
    ],
  },
  {
    name: "Feature 2 children 1",
    type: "folder",
    opened: false,
    id: 4,
    parent: true,
    children: [
      {
        name: "Feature 2 children 2",
        type: "folder",
        opened: false,
        id: 5,
        parentId: 4,
        children: [
          {
            name: "Feature 2 children 3",
            type: "folder",
            opened: false,
            id: 6,
            parentId: 5,
            children: [
              {
                name: "Feature 2 children 4",
                team: "S&C / Merch & Search",
                priority: "High",
                coverage: "Started",
                type: "feature",
                id: 7,
                parentId: 6,
              },
            ],
          },
        ],
      },
    ],
  },
]);

const teams = reactive([
  { label: "All", value: "", disabled: true },
  { label: "High", value: "High" },
  { label: "Medium", value: "Medium" },
  { label: "Low", value: "Low" },
]);

const priority = reactive([
  { label: "All", value: "", disabled: true },
  { label: "High", value: "High" },
  { label: "Medium", value: "Medium" },
  { label: "Low", value: "Low" },
]);

const coverage = reactive([
  { label: "All", value: "", disabled: true },
  { label: "Cannot automate", value: "Cannot automate" },
  { label: "Deprecated", value: "Deprecated" },
]);

const openFeature = (feature) => {
  const index = featuresList
    .map((e) => e.id)
    .indexOf(
      feature.children[0] ? feature.children[0].parentId : feature.children.parentId
    );

  const duplicate = featuresList.some((e) => e.id === feature.children[0].id);

  if (duplicate) {
    return false;
  } else {
    feature.opened = true;
    return featuresList.splice(
      index + 1,
      0,
      feature.children[0] ? feature.children[0] : feature.children
    )
  }
};
</script>

<template>
  <div>
    <table class="vtmn-w-11/12 vtmn-m-auto">
      <thead>
        <tr class="table-header vtmn-h-10">
          <th>
            <span class="feature-name vtmn-flex vtmn-items-center vtmn-justify-around">
              Name
              <VtmnSearch
                class="feature-name-search"
                placeholder="Search a feature name..."
              />
            </span>
          </th>
          <th>
            <span class="feature-team vtmn-flex vtmn-items-center vtmn-justify-evenly">
              Team
              <VtmnSelect
                style="width: 200px"
                class="feature-team-select"
                :identifier="'team-select'"
                :options="teams"
              />
            </span>
          </th>
          <th>
            <span
              class="feature-priority vtmn-flex vtmn-items-center vtmn-justify-evenly"
            >
              Priority
              <VtmnSelect
                style="width: 90px"
                :identifier="'priority-select'"
                :options="priority"
              />
            </span>
          </th>
          <th>
            <span class="vtmn-flex vtmn-items-center vtmn-justify-evenly">
              Coverage
              <VtmnSelect
                style="width: 280px"
                :identifier="'coverage-select'"
                :options="coverage"
              />
            </span>
          </th>
          <th>
            <span class="vtmn-flex vtmn-items-center vtmn-justify-evenly vtmn-float-left">
              <VtmnIcon
                value="settings-line"
                class="vtmn-mx-4 vtmn-cursor-pointer"
                :size="24"
              />
              <VtmnCheckbox :identifier="'feature-select'" />
            </span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="features in featuresList"
          :key="features.id"
          class="vtmn-m-4"
          :class="features.type === 'folder' ? 'vtmn-w-full' : 'feature'"
        >
          <td
            v-if="features.type === 'folder'"
            class="feature-name vtmn-flex vtmn-items-center vtmn-typo_text-3"
            :class="!features.parent ? 'vtmn-pl-5 key' : 'vtmn-flex'"
          >
            <span
              v-if="features.children"
              class="vtmn-flex"
              @click="openFeature(features, features.children)"
            >
              <VtmnIcon
                value="play-fill"
                class="vtmn-mx-2 vtmn-cursor-pointer"
                :class="features.opened === true ? 'feature-opened' : 'feature-closed'"
                :size="18"
              />
            </span>
            {{ features.name }}
          </td>
          <td
            v-if="features.type === 'feature'"
            class="vtmn-text-center vtmn-typo_text-3"
          >
            {{ features.name }}
          </td>
          <td v-else />
          <td
            v-if="features.type === 'feature'"
            class="vtmn-text-center vtmn-typo_text-3"
          >
            {{ features.team }}
          </td>
          <td v-else />
          <td
            v-if="features.type === 'feature'"
            class="vtmn-text-center vtmn-typo_text-3"
          >
            {{ features.priority }}
          </td>
          <td v-else />
          <td
            v-if="features.type === 'feature'"
            class="vtmn-text-center vtmn-typo_text-3"
          >
            {{ features.coverage }}
          </td>
          <td :class="features.type === 'folder' ? 'vtmn-flex vtmn-text-right' : ''">
            <span>
              <VtmnIcon
                value="settings-line"
                class="vtmn-mx-4 vtmn-cursor-pointer"
                :size="24"
              />
              <VtmnCheckbox :identifier="'feature-select'" />
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.vtmn-select_container {
  transform: translateY(-10px);
}

.feature-name-search {
  width: 80%;
}

.hide-under-feature {
  display: none;
}

.feature-name {
  color: var(--vtmn-semantic-color_border-inactive);
}

.feature-opened {
  transform: rotate(90deg);
  transition: .3s;
}

.feature {
  border: 1px solid #cccccc;
}
</style>
