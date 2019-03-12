<template>
  <div>
    <management-menu/>

    <div style="margin: 0 0 8px 20px; color: gray;">
      <Icon type="md-build" />
      Settings let you customize various details of the project in ARA:
      how test execution reports are indexed into ARA,
      how ARA sends report emails,
      and how problems are linked to a bug tracker system.
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
            </div>
            <div v-else>
              <Button :disabled="!!editingCode" icon="md-create" :title="(editingCode ? 'Another setting is currently being edited' : 'Edit')" @click="edit(setting)" style="margin-right: 4px;"/>
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
            <div style="color: gray; line-height: 1.2; margin-top: 4px;">
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
        savingEdit: false
      }
    },

    methods: {
      loadSettings () {
        this.loadingSettings = true
        Vue.http
          .get(api.paths.settings(this), api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingSettings = false
            this.settingGroups = response.body
          }, (error) => {
            this.loadingSettings = false
            api.handleError(error)
          })
      },

      edit (setting) {
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
        let url = api.paths.settings(this) + '/' + this.editingCode
        let data = { value: (this.editingValue === undefined ? '' : '' + this.editingValue) } // boolean and int => string _but_ undefined => ''
        Vue.http
          .put(url, data, api.REQUEST_OPTIONS)
          .then((response) => {
            this.savingEdit = false
            this.settingGroups = response.body
            this.editingCode = null
          }, (error) => {
            this.savingEdit = false
            api.handleError(error)
          })
      },

      cancelEdit () {
        this.editingCode = null
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
