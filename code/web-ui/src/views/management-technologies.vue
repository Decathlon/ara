<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (C) 2020 by the ARA Contributors
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
  <div>
    <management-menu/>

    <div style="margin: 0 0 8px 20px; color: gray;">
      <Icon type="md-build" />
      Technologies let you customize all the settings related to the test technologies (e.g. Cucumber or Postman) supported by ARA.
    </div>

    <div v-for="group in settingGroups" :key="group.name">
      <h2 style="padding: 0 17px; margin-top: 0;">
        {{group.name}}
      </h2>

      <Card :bordered="true" dis-hover style="height: 100%; margin-bottom: 8px; padding: 0px;">
        <Form :label-width="196">
          <Form-item v-for="setting in group.settings" :key="setting.code" :label="setting.name + ':'" :required="setting.required">
            <div v-if="editingCode === setting.code">
              <form-field ref="formField"
                          :field="setting"
                          :hideCheckboxLabel="true"
                          v-model="editingValue"
                          :style="'display: inline-block; margin-right: 4px;' + (setting.type === 'int' ? '' : ' width: 70%;')"
                          v-on:enter="saveEdit"
                          v-on:escape="cancelEdit"/>
              <Button-group>
                <Button icon="md-checkmark" type="primary" @click="saveEdit" :loading="savingEdit">SAVE</Button>
                <Button icon="md-close-circle" @click="cancelEdit" :disabled="savingEdit">CANCEL</Button>
              </Button-group>
              <span v-if="errorText" class="error-message">You don't have the rights to edit this field!</span>
            </div>
            <div v-else>
              <Button v-if="userRole === 'ADMIN' || 'SUPER_ADMIN'" :disabled="!!editingCode" icon="md-create" :title="(editingCode ? 'Another setting is currently being edited' : 'Edit')" @click="edit(setting, group.technology)" style="margin-right: 4px;"/>
              <span v-if="setting.type === 'boolean'">
                <Icon v-if="setting.value === 'true'" type="md-checkmark" size="16"/>
                <span v-else>-</span>
              </span>
              <span v-else-if="setting.type === 'select'">
                <span v-if="!setting.value">-</span>
                <span v-else v-for="option in setting.options" :key="option.value">
                  <span v-if="option.value === setting.value">{{option.label}}</span>
                </span>
              </span>
              <span v-else>{{setting.value ? setting.value : '-'}}</span>
            </div>
            <div class="hints">
              {{setting.help}}
            </div>
          </Form-item>
        </Form>
      </Card>
    </div>
    <Spin fix v-if="loadingSettings" />
  </div>
</template>

<script>
  import Vue from 'vue'
  import managementMenuComponent from '../components/management-menu'
  import formFieldComponent from '../components/form-field'
  import api from '../libs/api'
  import { mapState } from 'vuex'

  export default {
    name: 'management-projects',

    components: {
      'management-menu': managementMenuComponent,
      'form-field': formFieldComponent
    },

    data () {
      return {
        loadingSettings: true,
        settingGroups: null,
        editingCode: null,
        editingValue: null,
        editingTechnology: null,
        savingEdit: false,
        errorText: false
      }
    },

    computed: {
      ...mapState('users', ['userRole'])
    },

    methods: {
      loadSettings () {
        this.loadingSettings = true
        Vue.http
          .get(api.paths.settings(this) + '/technology', api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingSettings = false
            this.settingGroups = response.body
          }, (error) => {
            this.loadingSettings = false
            api.handleError(error)
          })
      },

      edit (setting, technology) {
        this.editingTechnology = technology
        this.editingCode = setting.code
        if (setting.type === 'password') {
          // Passwords are write-only: values are '*****' when retreived
          this.editingValue = ''
        } else if (setting.type === 'int') {
          // Setting values are strings: number input is of number type
          this.editingValue = (setting.value ? parseInt(setting.value, 10) : 0)
        } else if (setting.type === 'boolean') {
          // Setting values are strings: checkbox input is of boolean type
          this.editingValue = (setting.value === 'true')
        } else {
          this.editingValue = setting.value
        }
        this.$nextTick(() => this.$refs.formField[0].focus())
      },

      saveEdit () {
        this.savingEdit = true
        let url = api.paths.settings(this) + '/' + this.editingCode + '/technology/' + this.editingTechnology
        let data = { value: (this.editingValue === undefined ? '' : '' + this.editingValue) } // boolean and int => string _but_ undefined => ''
        Vue.http
          .put(url, data, api.REQUEST_OPTIONS)
          .then((response) => {
            this.savingEdit = false
            this.settingGroups = response.body
            this.editingCode = null
            this.editingTechnology = null
          }, (error) => {
            this.savingEdit = false
            this.errorText = true
            api.handleError(error)
          })
      },

      cancelEdit () {
        this.editingCode = null
        this.editingTechnology = null
      }
    },

    mounted () {
      this.loadSettings()
    },

    watch: {
      '$route' () {
        this.loadSettings()
      }
    }
  }
</script>

<style scoped>
  .error-message {
    display: flex;
    color: #ed4014;
    font-weight: 900;
  }
</style>
