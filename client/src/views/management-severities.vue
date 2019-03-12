<template>
  <div>
    <management-menu/>
    <crud :url="url" name="severity" titleCaseName="Severity" :introduction="introduction" :fields="fields" v-on:loaded="loaded" />
  </div>
</template>

<script>
  import api from '../libs/api'
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'

  export default {
    name: 'management-severities',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        introduction: 'Severities allow to group Cucumber scenarios and Postman requests in different priority-groups. ' +
                      'Each severity can have a different quality requirement: ' +
                      'higher severities need more scenarios/requests to pass in order to validate a test execution, ' +
                      'while lower severities will allow more failing scenarios/requests in order not to block a test execution for inconsequential regressions.',
        severities: []
      }
    },

    computed: {
      fields () {
        let nextPosition = 1
        for (let severity of this.severities) {
          if (nextPosition <= severity.position) {
            nextPosition = severity.position + 1
          }
        }
        return [
          {
            code: 'position',
            name: 'Position',
            columnTitle: 'Position',
            type: 'int',
            required: true,
            newValue: nextPosition,
            width: 96,
            help: 'The order in which the severities should appear: the lowest position should be for the highest severity.'
          },
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'string',
            required: true,
            newValue: '',
            primaryKey: true,
            width: undefined,
            createOnlyBecause: 'it is referenced in Cucumber/Postman tags in your sources',
            help: '' +
              'The code used to identify the severity outside ARA. The most important one is the Cucumber and Postman tags. ' +
              'Eg. "sanity-check" is the code of the severity described by the "@severity-sanity-check" Cucumber/Postman tag.'
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
            help: 'The full name (eg. "Sanity Check").'
          },
          {
            code: 'shortName',
            name: 'Short name',
            columnTitle: 'Short Name',
            type: 'string',
            required: true,
            newValue: '',
            width: undefined,
            help: 'The shorter name (but still intelligible) to display on table column headers where space is constrained (eg. "Sanity Ch.").'
          },
          {
            code: 'initials',
            name: 'Initials',
            columnTitle: 'Initials',
            type: 'string',
            required: true,
            newValue: '',
            width: 96,
            help: 'The shortest name to display on email subjects to help keep it very short (eg. "S.C.").'
          },
          {
            code: 'defaultOnMissing',
            name: 'Default',
            columnTitle: 'Default',
            type: 'boolean',
            required: false,
            newValue: false,
            width: 96,
            help: '' +
              'Check to use that severity as a default one when a Cucumber scenario or Postman request does not declare its severity or has a nonexistent one. ' +
              'Only one severity can be declared as the default.'
          }
        ]
      },

      url () {
        return api.paths.severities(this)
      }
    },

    methods: {
      loaded (data) {
        this.severities = data
        this.$store.commit('severities/setSeverities', { projectCode: this.$route.params.projectCode, severities: data })
      }
    }
  }
</script>
