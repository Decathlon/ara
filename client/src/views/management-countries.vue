<template>
  <div>
    <management-menu />
    <crud :url="url" name="country" titleCaseName="Country" :introduction="introduction" :fields="fields" />
  </div>
</template>

<script>
  import api from '../libs/api'
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'

  export default {
    name: 'management-countries',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        introduction: 'Countries are used to run a set of test types against several configurations. ' +
                      'Eg. run "API", "Firefox", "Chrome" and "iOS Mobile" test types several times, using different countries or configurations. ' +
                      'At least one country is mandatory. ' +
                      'The code is used by continuous integration scripts to upload reports, and shown in functionalities attached to countries.',
        fields: [
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'string',
            required: true,
            newValue: '',
            primaryKey: true,
            width: 96,
            createOnlyBecause: 'the code ends-up in ARA filtering URLs and in source-codes outside of ARA',
            help: '' +
              'The two-letters code to use in Cucumber "@country-xx" tags (where "xx" is the country code) ' +
              'or in Postman root folder names (if the source holding the collection has the "Postman country root folders" box checked). ' +
              'Displayed on very constrained spaces like the execution table, the functionality "countries" column. ' +
              'Eg. "fr" for "France" or "us" for "United States".'
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
            help: 'The full name (eg. "France" or "United States").'
          }
        ]
      }
    },

    computed: {
      url () {
        return api.paths.countries(this)
      }
    }
  }
</script>
