<template>
  <div>
    <management-menu />
    <crud :url="url" name="root cause" titleCaseName="Root Cause" :introduction="introduction" :fields="fields" v-on:loaded="loaded" />
  </div>
</template>

<script>
  import api from '../libs/api'
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'

  export default {
    name: 'management-root-causes',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        introduction: 'Root causes describe categories of modifications needed to fix problems ' +
                      '(eg. a test was not updated to reflect new code, a new regression was introduced, ' +
                      'or the environment on which tests are executed was not up to date).',
        fields: [
          {
            code: 'id',
            type: 'hidden',
            newValue: -1,
            primaryKey: true
          },
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Root Cause',
            type: 'string',
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined,
            help: 'The name of the root cause to be assigned to problems.'
          }
        ]
      }
    },

    methods: {
      loaded (data) {
        this.$store.commit('rootCauses/setRootCauses', { projectCode: this.$route.params.projectCode, rootCauses: data })
      }
    },

    computed: {
      url () {
        return api.paths.rootCauses(this)
      }
    }
  }
</script>
