<template>
  <div>
    <management-menu/>
    <Spin fix v-if="loadingSources" />
    <crud v-else :url="url" name="type" titleCaseName="Type" :introduction="introduction" :fields="fields" />
  </div>
</template>

<script>
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'
  import api from '../libs/api'
  import Vue from 'vue'

  export default {
    name: 'management-types',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        loadingSources: true, // True to not show crud at screenstart, hide it while loading sources and re-show it after (would result in two teams loadings)
        introduction: 'Types describe a kind of automated test. ' +
                      'At the very least, types mirror sources, but a source can be used by two different types, ' +
                      'to run a set of Cucumber scenarios and/or Postman requests on two different conditions (different browsers or devices). ' +
                      'The codes of types are used by continuous integration scripts to upload tests reports for them to be indexed into ARA.',
        fields: [
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'string',
            required: true,
            newValue: '',
            primaryKey: true,
            width: 256,
            createOnlyBecause: 'the code ends-up in ARA filtering URLs and in test-execution scripts outside of ARA (scripts upload results to ARA using this code)',
            help: '' +
              'A small technical code representing the test type. ' +
              'Eg. "api", "postman", "web-chrome", "web-firefox", "web-mobile"...'
          },
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Name',
            type: 'string',
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined,
            help: '' +
              'The unique user-visible name of the type to display in execution results. ' +
              'Eg. "API", "Postman", "Chrome", "Firefox", "Mobile"...'
          },
          {
            code: 'sourceCode',
            name: 'Source',
            columnTitle: 'Source',
            type: 'select',
            options: undefined, // Will be populated at mount time by getAllSource()
            required: false,
            newValue: '', // Same code as the empty first option added by getAllSource()
            width: 256,
            help: '' +
              'The Version Control System source hosting the test Cucumber .feature files or Postman .json collection files. ' +
              'Several test-types can use the same source (this is the main purpose of the source/type separation). ' +
              'For instance, a "Web" source can be tested twice, by the "Web Desktop" and "Web Mobile" test types (typically to make sure a website is really responsive). ' +
              'Or a "Web" source could also be tested by the "Firefox" and "Chrome" test types. ' +
              'If the source is not set, the type will not be indexed (eg. for performance tests played alongside Postman and Cucumber tests). ' +
              'If another unknown type is encountered while running tests, an error will occur: declare a type without source to remove the error while ignoring the unsupported type.'
          },
          {
            code: 'browser',
            name: 'Browser',
            columnTitle: 'Browser',
            type: 'boolean',
            required: false,
            newValue: false,
            width: 96,
            help: '' +
              'Check if the test type is a browser test (using eg. Selenium). ' +
              'Keep unchecked if the test type is an API test or a mobile native application test. ' +
              'This allows to create problem aggregation rules only targetting browser tests (on any browser or device type).'
          },
          {
            code: 'mobile',
            name: 'Mobile',
            columnTitle: 'Mobile',
            type: 'boolean',
            required: false,
            newValue: false,
            width: 96,
            help: '' +
              'Check if the test type is a mobile device test (eg. testing the mobile version of a responsive website). ' +
              'This allows to create problem aggregation rules only targetting mobile browser tests (grouping all mobile browsers together).'
          }
        ]
      }
    },

    computed: {
      url () {
        return api.paths.types(this)
      }
    },

    methods: {
      getAllSource () {
        this.loadingSources = true
        Vue.http
          .get(api.paths.sources(this), api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingSources = false
            let options = response.body
              .map(source => {
                return {
                  value: source.code,
                  label: source.name
                }
              })
            options.unshift({
              value: '', // null & undefined not permitted by iView's Select
              label: '\u00A0' // Unbreakable-space for the height of the option to be normal
            })

            this.fields
              .filter(field => field.code === 'sourceCode')[0]
              .options = options
          }, (error) => {
            this.loadingSources = false
            api.handleError(error)
          })
      }
    },

    mounted () {
      this.getAllSource()
    },

    watch: {
      '$route' () {
        this.getAllSource()
      }
    }
  }
</script>
