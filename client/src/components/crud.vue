<template>
  <div>
    <div style="margin: 0 0 8px 20px; color: gray;">
      <Icon type="md-build" /> {{introduction}}
    </div>

    <Table border :columns="columns" :data="data" :no-data-text="(loadingTable ? '' : 'No data')" no-filtered-data-text="No result" :loading="loadingTable" />

    <Modal v-model="editing" @on-ok="save" @on-cancel="cancel" :width="900"
           :title="(editingNew ? 'Add' : 'Edit') + ' ' + titleCaseName"
           :okText="(editingNew ? 'Add' : 'Save')" :loading="loadingSaving" ref="editPopup">
      <Form :label-width="128">
        <Form-item v-for="field in fields"
                   v-if="field.type !== 'hidden'"
                   :key="field.code"
                   :label="(field.type === 'boolean' ? '' : field.name + ':')"
                   :required="field.required && (editingNew || (!field.primaryKey && !field.createOnly))">
          <form-field :field="field" v-model="editingData[field.code]" :editingNew="editingNew" :ref="field.code" v-on:enter="save"/>
          <div style="color: gray; line-height: 1.2; margin-top: 4px;">
            {{field.help}}
          </div>
        </Form-item>
      </Form>
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import formFieldComponent from '../components/form-field'
  import api from '../libs/api'
  import util from '../libs/util'

  export default {
    name: 'crud',

    components: {
      'form-field': formFieldComponent
    },

    props: {
      url: {
        // URL of the ressource where to GET (read), POST (create) entities, and the root URL for PUT (update) and DELETE (delete) (will append thee primary key to this URL for these two cases)
        type: String,
        required: true
      },
      name: {
        // lower-case name of the entity to manage
        type: String,
        required: true
      },
      titleCaseName: {
        // Capitalized name of the entity to manage
        type: String,
        required: true
      },
      introduction: {
        // A message to put at the top of the table to explain the goal of the configuration table
        type: String,
        required: true
      },
      fields: {
        // Each field in the array can have the following properties:
        // * code (String): the technical name of the field in the JSON to download/upload from/to server (eg. "name")
        // * name (String): the capitalized business-name of the field to display in the edit dialog (eg. "Name")
        // * columnTitle (String): the capitalized business-name of the field to display in the column of the table (eg. "Name" or "Country")
        // * type (String): the type of the field: possible values are:
        // ** 'string': a small text field (edited with an <input/>)
        // ** 'textarea': a bigger, multi-lines text field (edited with a <textarea/>)
        // ** 'select': the value is a code present in a list of choices (see the 'options' array property below)
        // ** 'boolean': the value is displayed as a check, if true, and edited with a checkbox
        // ** 'int': a number value (edited with a number-specialized <input/>)
        // ** 'hidden': the value is not displayed neither in the table nor in the edit box (eg. the unmodifiable and technical "Long id" primary key; it is not the case for eg. a "String code" primary key if the code has to be filled by user at entity creation time)
        // * options (Array): for a "type:'select'" field, provide the list of options for the edit combobox and the mapping of value=>label couples to display a user-friendly value in the table cell (eg. [ { value: '01', label: 'First option' } ])
        // * required (Boolean): true to display a red "*" in the edit box, to inform users this field is mandatory
        // * newValue (Any): the value with which to initialize the field while poping-up the edit dialog for a new entity creation
        // * primaryKey (Boolean): true for the field that is used in GET/PUT/DELETE URLs to identify an entity
        // * businessKey (Boolean): true for the field that uniquely identify the entity for users (eg. the name): displayed in the Delete confirmation dialog
        // * readOnly (Boolean): true to never be able to edit the field: it will be displayed as read-only text for information (eg. on an editOnly table)
        // * createOnly (Boolean): true to be able to type a value on creation, but never be able to edit it afterward (always implicitely true for primaryKey fields)
        // * createOnlyBecause (String): if createOnly, an help message says 'Once created, this field will be unmodifiable because it is referenced elsewhere.'. You can customize the 'it is referenced elsewhere' part of the sentence with a more descriptive 'because'-clause
        // * width (Number): the width in pixels of the column representing the field, or undefined to automatically use the remaining-space
        // * help (String): a little message to display below the edit field, explaining to users the goal of the field and the possible values or restrictions
        type: Array,
        required: true
      },
      // If true, do only display EDIT buttons, and hide ADD and DELETE buttons
      editOnly: Boolean,
      // If true, do only display ADD and EDIT buttons, and hide DELETE button
      disableDelete: Boolean
    },

    data () {
      let columns = []
      for (let field of this.fields) {
        if (field.type !== 'hidden') {
          columns.push({
            title: field.columnTitle,
            key: field.code,
            width: field.width,
            render: (h, params) => {
              let value = params.row[field.code]
              if (field.type === 'boolean') {
                if (value) {
                  return h('Icon', {
                    props: {
                      type: 'md-checkmark',
                      size: 16
                    },
                    attrs: {
                      title: field.name
                    }
                  })
                } else {
                  return null
                }
              } else if (field.type === 'select') {
                for (let option of field.options) {
                  if (option.value === value) {
                    return h('span', option.label)
                  }
                }
                return h('span', value)
              } else if (field.type === 'int') {
                return h('span', value) // This will escape HTML characters
              } else {
                let maxLength = 512
                let trimmed = value
                if (trimmed) {
                  trimmed = trimmed.replace(/\s\s+/g, ' ') // HTML values will have lot of spaces (+ tabulations and new lines) trimmed by browser
                  if (trimmed.length > maxLength) {
                    trimmed = trimmed.substr(0, maxLength) + '...'
                  }
                }
                return h('span', trimmed) // This will escape HTML characters
              }
            }
          })
        }
      }
      columns.push({
        title: ' ',
        key: 'action',
        width: (this.editOnly || this.disableDelete ? 104 : 190),
        align: 'center',
        render: (h, params) => {
          let buttons = [
            h('Button', {
              props: { type: 'primary', size: 'small', icon: 'md-create' },
              on: { click: () => { this.edit(params.row) } }
            }, 'EDIT')
          ]
          if (!this.editOnly && !this.disableDelete) {
            buttons.push(h('Button', {
              props: { type: 'error', size: 'small', icon: 'md-trash' },
              on: { click: () => { this.delete(params.index) } }
            }, 'DELETE'))
          }
          return h('div', [
            h('Button-group', {}, buttons)
          ])
        },
        renderHeader: (h, params) => {
          if (this.editOnly) {
            return null
          } else {
            return h('Button', {
              props: { type: 'success', size: 'small', icon: 'md-add' },
              on: { click: () => { this.add() } }
            }, 'ADD')
          }
        }
      })
      return {
        loadingTable: false,
        editing: false,
        loadingSaving: true,
        columns,
        data: [],
        editingData: {},
        editingNew: false
      }
    },

    computed: {
      /**
       * @return the field that has primaryKey set to true (eg. the 'id' or 'code' field)
       */
      primaryKeyField () {
        for (let field of this.fields) {
          if (field.primaryKey) {
            return field
          }
        }
        throw new Error('The table ' + this.name + ' has no primaryKey field')
      },

      /**
       * @return the field that has businessKey set to true:
       *         the field to uniquely dinstinguish an entity to the user in a frienly way
       *         (eg. the 'name' field)
       */
      businessKeyFields () {
        let businessKeyFields = []
        for (let field of this.fields) {
          if (field.businessKey) {
            businessKeyFields.push(field)
          }
        }
        if (businessKeyFields.length === 0) {
          throw new Error('The table ' + this.name + ' has no businessKey field')
        }
        return businessKeyFields
      }
    },

    methods: {
      load () {
        this.loadingTable = true
        Vue.http
          .get(this.url, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingTable = false
            this.data = response.body
            this.emitLoaded()
          }, (error) => {
            this.loadingTable = false
            api.handleError(error)
          })
      },

      newRowData () {
        let row = {}
        for (let field of this.fields) {
          row[field.code] = field.newValue
        }
        return row
      },

      add () {
        this.doEdit(this.newRowData(), true)
      },

      edit (row) {
        this.doEdit(row, false)
      },

      doEdit (row, editingNew) {
        this.editingData = { ...row }
        this.editingNew = editingNew
        this.editing = true
        this.$nextTick(() => this.$refs[this.firstVisibleFieldCode(editingNew)][0].focus())
      },

      firstVisibleFieldCode (editingNew) {
        for (let field of this.fields) {
          if (field.type !== 'hidden' && field.type !== 'select' && !field.readOnly && !field.readOnly && (editingNew || (!field.primaryKey && !field.createOnly))) { // Primary-key is read-only when editing
            return field.code
          }
        }
        throw new Error('The table ' + this.name + ' has no field, or they are all either of type hidden, selects, read-only or non-modifiable primary key')
      },

      save () {
        if (this.editingNew) {
          let row = { ...this.editingData }
          row.id = undefined
          Vue.http
            .post(this.url, row, api.REQUEST_OPTIONS)
            .then((response) => {
              this.$refs.editPopup.close()
              // No early-UI refresh, as we do not know where to put the new element (order is table-specific, handled by server)
              this.load()
            }, (error) => {
              this.loadingSaving = false
              api.handleError(error, () => {
                this.loadingSaving = true
              })
            })
        } else {
          let primaryKeyValue = this.editingData[this.primaryKeyField.code]
          let sentData = {}
          for (let field of this.fields) {
            let value = this.editingData[field.code]
            // On some screens, a field is an enum: if not provided, null must be sent, so JSON parsing does not fail
            // (but @NotNull annotation fails with a proper message)
            if (value !== '') {
              sentData[field.code] = value
            }
          }
          Vue.http
            .put(this.url + '/' + primaryKeyValue, sentData, api.REQUEST_OPTIONS)
            .then((response) => {
              this.$refs.editPopup.close()
              // No early-UI refresh, as we do not know where to put the new element (order is table-specific, handled by server)
              this.load()
            }, (error) => {
              this.loadingSaving = false
              api.handleError(error, () => {
                this.loadingSaving = true
              })
            })
        }
      },

      cancel () {
        this.editing = false
      },

      delete (index) {
        let self = this
        this.$Modal.confirm({
          title: 'Delete ' + this.titleCaseName,
          content: `<p>Delete ${this.name} ${this.businessKeyAsHtml(index)}?</p>`,
          okText: 'Delete',
          loading: true,
          onOk () {
            Vue.http
              .delete(self.url + '/' + self.data[index][self.primaryKeyField.code], api.REQUEST_OPTIONS)
              .then((response) => {
                self.$Modal.remove()
                self.data.splice(index, 1) // UI refresh: no need to reload for a delete
                self.emitLoaded()
              }, (error) => {
                self.$Modal.remove()
                api.handleError(error)
              })
          }
        })
      },

      emitLoaded () {
        this.$emit('loaded', this.data)
      },

      businessKeyAsHtml (index) {
        let libelle = ''
        for (let i = 0; i < this.businessKeyFields.length; i++) {
          libelle += `<strong>${util.escapeHtml(this.data[index][this.businessKeyFields[i].code])}</strong>`
          if (i < this.businessKeyFields.length - 1) {
            libelle += '/'
          }
        }
        return libelle
      }
    },

    mounted () {
      this.load()
    },

    watch: {
      url () {
        this.load()
      }
    }
  }
</script>
