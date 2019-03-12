<template>
  <div>
    <management-menu />
    <crud :url="url" name="team" titleCaseName="Team" :introduction="introduction" :fields="fields" v-on:loaded="loaded" />
  </div>
</template>

<script>
  import api from '../libs/api'
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'

  export default {
    name: 'management-teams',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        introduction: 'Teams are groups of people on your project, to which you can assign ' +
                      'functionalities (and thus test scenarios) and/or problems to be fixed.',
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
            columnTitle: 'Team',
            type: 'string',
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined,
            help: 'The visible name of the team, in functionalities, excecution results and problems.'
          },
          {
            code: 'assignableToProblems',
            name: 'Assignable to problems',
            columnTitle: 'Assignable to Problems',
            type: 'boolean',
            required: false,
            newValue: true,
            width: 256,
            help: '' +
              'Checked for the team to appear in problem properties. ' +
              'The team having an assigned problem is responsible to solve this problem. ' +
              'In the general case, all teams can be assigned problems.'
          },
          {
            code: 'assignableToFunctionalities',
            name: 'Assignable to functionalities',
            columnTitle: 'Assignable to Functionalities',
            type: 'boolean',
            required: false,
            newValue: true,
            width: 256,
            help: '' +
              'Checked for the team to appear in functionality properties. ' +
              'The team having an assigned functionality is responsible to develop this functionality. ' +
              'In the general case, most teams can be assigned functionalities. ' +
              'But some technical teams are only responsible for runtime problems during test executions ' +
              '(ops maintaining the continuous integration platforms, for instance): ' +
              'these teams cannot develop new functionalities, so the checkbox is left unchecked for them.'
          }
        ]
      }
    },

    methods: {
      loaded (data) {
        this.$store.commit('teams/setTeams', { projectCode: this.$route.params.projectCode, teams: data })
      }
    },

    computed: {
      url () {
        return api.paths.teams(this)
      }
    }
  }
</script>
