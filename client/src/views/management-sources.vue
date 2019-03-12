<template>
  <div>
    <management-menu/>
    <crud :url="url" name="source" titleCaseName="Source" :introduction="introduction" :fields="fields" />
  </div>
</template>

<script>
  import api from '../libs/api'
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'

  export default {
    name: 'management-sources',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        introduction: 'Sources are Version Control System repositories hosting the tests ' +
                      '(either as Cucumber .feature or as Postman .json collection files). ' +
                      'A source of tests can be used by several types of tests ' +
                      '(for instance, a "Web" source can be tested twice: by the "Web Desktop" and "Web Mobile" test types, ' +
                      'or by the "Firefox" and "Chrome" test types). ' +
                      'Sources are also used to upload known Cucumber scenarios and Postman requests to associate them with functionalities ' +
                      'and compute functionality coverage by automated tests. ' +
                      'The code of the source is used in the HTTP call made to upload scenarios and requests.',
        fields: [
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'string',
            required: true,
            newValue: '',
            primaryKey: true,
            width: 128,
            createOnlyBecause: 'the code ends-up in URLs of ARA in continuous integration scripts',
            help: 'The technical code of the source, to be used by continuous integration scripts to upload scenarios or requests after a new push or merge request on the source\'s branch. Eg. "api", "iphone", "postman", "web"...'
          },
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Name',
            type: 'string',
            required: true,
            newValue: '',
            businessKey: true,
            width: 256,
            help: 'The unique user-visible name of the source to display in coverage details of the Functionality Cartography screen, or on the "Ignored Scenarios" section of the Executions screen. Eg. "API", "iPhone", "Postman", "Web"...'
          },
          {
            code: 'letter',
            name: 'Letter',
            columnTitle: 'Letter',
            type: 'string',
            required: true,
            newValue: '',
            width: 96,
            help: 'The unique user-visible letter of the source to display in the column "Coverage by scenarios" of the Functionality Cartography screen. Eg. "A" for "API", "I" for iPhone", "P" for "Postman", "W" for "Web"...'
          },
          {
            code: 'technology',
            name: 'Technology',
            columnTitle: 'Technology',
            type: 'select',
            options: [
              { value: 'CUCUMBER', label: 'Cucumber' },
              { value: 'POSTMAN', label: 'Postman' }
            ],
            required: true,
            newValue: undefined,
            width: 128,
            help: 'The technology used for tests stored in this VCS source: Cucumber .feature files, Postman .json collections...'
          },
          {
            code: 'vcsUrl',
            name: 'VCS URL',
            columnTitle: 'Version Control System URL',
            type: 'string',
            required: true,
            newValue: '',
            width: undefined,
            help: '' +
              'The root Version Control System URL in which Cucumber feature files can be edited or Postman JSON files can be downloaded. ' +
              'It MUST contain a {{branch}} placeholder because it will be used to construct the scenario edit URL in DEBUG INFORMATION buttons of a scenario having been executed. ' +
              'The feature file of scenarios and requests will be appended to this base URL. ' +
              'Eg. "https://git.company.com/project/blob/{{branch}}/src/main/resources/collections/" for Postman collections whose names are eg. "collection1.json", or ' +
              '"https://git.company.com/project/edit/{{branch}}/src/main/resources/" for Cucumber feature files whose names are eg. "com/company/project/features/feature1.feature".'
          },
          {
            code: 'defaultBranch',
            name: 'Default branch',
            columnTitle: 'Default Branch',
            type: 'string',
            required: true,
            newValue: '',
            width: 128,
            help: '' +
              'The branch where scenarios or requests are pulled from. ' +
              'This will be used to replace the {{branch}} parameter in the VCS URL ' +
              'for the Edit Scenario link in the Functionality Coverage details popup, ' +
              'on the erroneous scenarios of the Functionality Assignation screen or ' +
              'on the Ignored Scenarios scenarios on the Executions screen. ' +
              'Eg. "master" or "develop".'
          },
          {
            code: 'postmanCountryRootFolders',
            name: 'Postman country root folders',
            columnTitle: 'Country Root Folders',
            type: 'boolean',
            required: false,
            newValue: false,
            width: 120,
            help: '' +
              'For a source using the Postman technology, check for the root folders of the collections to represent country code(s). ' +
              'When checked, root folders are eg. "us" (for US country) or "fr+us" (to run the requests on FR and US countries) or "all" (to run the requests on all countries). ' +
              'If not checked, all requests in a collection are run for all countries, and the folder name is not interpreted as a country code. ' +
              'Has no effect on Cucumber sources.'
          }
        ]
      }
    },

    computed: {
      url () {
        return api.paths.sources(this)
      }
    }
  }
</script>
