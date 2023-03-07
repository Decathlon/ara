<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (C) 2019 by the ARA Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ 	 http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
<template>
  <div class="tableContent">
    <h1 v-if="this.groupInfo[0]" class="adminTitle">
      {{ this.groupInfo[0].name }}
      <p class="title-description"><strong>{{ this.groupInfo[0].description }}</strong></p>
    </h1>
    
    <Tabs class="adminTable" type="card" v-model="activeTab" :animated="false">
      <TabPane label="Managers">
        <table class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
            </tr>
          </thead>

          <tbody v-for="member in groupInfo" :key="member.id">
            <tr
              v-for="(managers, index) in member.managers"
              :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ managers.login }}</p>
              </td>
            </tr>
          </tbody>
        </table>
      </TabPane>
      <TabPane label="Users">
        <table class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
            </tr>
          </thead>

          <tbody v-for="member in groupInfo" :key="member.id">
            <tr
              v-for="(user, index) in member.members"
              :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ user.login }}</p>
              </td>
            </tr>
          </tbody>
        </table>
      </TabPane>
      <TabPane label="Projects">
        <table class="tab-content" aria-label="Group's management">
          <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
            </tr>
          </thead>

          <tbody v-for="member in groupInfo" :key="member.id">
            <tr
              v-for="(projects, index) in member.scopes"
              :key="index"
              :class="index % 2 !== 0 ? 'lightGrey' : 'darkGrey'"
            >
              <td>
                <p>{{ projects.name }}</p>
              </td>
              <td>
                <p>{{ projects.role }}</p>
              </td>
            </tr>
          </tbody>
        </table>
      </TabPane>

      <Button @click="addToGroup" size="small" slot="extra">Add {{ groupTabs[activeTab] }}</Button>
    </Tabs>

    <Modal
      v-model="memberToAdd"
      title="Add Group"
      :width="900"
      :loading="loadingSaving"
      :footer-hide="true"
    >
      <Form
        ref="formValidate"
        :model="formValidate"
        :rules="ruleValidate"
        :label-width="128"
      >
        <Form-item label="Code" prop="code">
          <Input v-model="formValidate.code" />
        </Form-item>
        <Form-item class="modal-cta">
          <Button @click="closeModal(modalConfiguration[0])">Cancel</Button>
          <Button type="primary" @click="handleSubmit()">Submit</Button>
        </Form-item>
      </Form>
    </Modal>

    <Modal
      v-model="blockPopup"
      title="Block user"
      okText="Block user"
      @on-ok="confirmBlockUser"
      @close="memberToAdd = false"
      :width="900"
      :loading="loadingSaving"
      :footer-hide="!selectedBlockOption"
    >
      <p>Select what the user will be banned from:</p>

      <div class="banOptions">
        <RadioGroup v-model="selectedBlockOption" type="button">
          <Radio label="ARA"></Radio>
          <Radio label="Projects creation"></Radio>
          <Radio label="Groups creation"></Radio>
        </RadioGroup>
      </div>
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  export default {
    name: 'admin-management-groups',

    data () {
      return {
        members: [],
        memberToAdd: false,
        memberHeader: ['Name', 'Profile', 'Projects', ''],
        groupHeader: ['Name', 'Management', 'Users', 'Projects', ''],
        blockPopup: false,
        formValidate: {
          code: ''
        },
        ruleValidate: {
          code: { required: true, message: 'The code cannot be empty', trigger: 'blur' }
        },
        memberToBlock: {
          member: '',
          index: '',
          blockReason: ''
        },
        memberValues: [
          {
            value: 'member',
            label: 'Members'
          },
          {
            value: 'group',
            label: 'Groups'
          }
        ],
        groupTabs: ['Managers', 'Users', 'Projects'],
        selectedBlockOption: '',
        memberType: 'Members',
        searchElement: '',
        showError: false,
        groupInfo: [],
        activeTab: 0
      }
    },

    methods: {
      getGroupInfo () {
        Vue.http
          .get(api.paths.allGroups, api.REQUEST_OPTIONS)
          .then((groups) => {
            this.groupInfo = groups.body.filter(item => item.name === this.$route.params.groupName)
          })
      },

      addToGroup () {
        const update = this.groupTabs[this.activeTab]

        switch (update) {
          case 'Managers':
            Vue.http
              .put(api.paths.groupsMembersManagement('user1', this.groupInfo[0].id), api.REQUEST_OPTIONS)
              .then(response => console.log(response))
            break
          case 'Users':
            break
          case 'Projects':
            break
        }
      }
    },

    created () {
      this.getGroupInfo()
    }
  }
</script>

<style scoped>
.projectCTA {
  justify-content: right;
}

.projectCTA .spacing-btn {
  margin-right: 15px;
}

.tab-content {
  width: 100%;
  border-collapse: collapse;
}

.tab-content th:first-child {
  border-radius: 4px 0 0 0;
}
</style>
