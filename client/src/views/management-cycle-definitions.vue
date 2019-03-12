<template>
  <div>
    <management-menu/>
    <crud :url="url" name="cycle" titleCaseName="Cycle" :introduction="introduction" :fields="fields" />
  </div>
</template>

<script>
  import api from '../libs/api'
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'

  export default {
    name: 'management-cycle-definitions',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        introduction: 'Cycles let you group execution results in a logical manner: ' +
                      'you can run all your test batteries (a list of test-type & country couples) on several Version Control branches ("master", "develop"...) ' +
                      'and executions will be grouped as one cycle per branch. ' +
                      'For each branch, you also can have several cycles: they are sort of test profiles. ' +
                      'For instance, for the "master" branch, you can have a cycle "full" that runs ALL lengthy tests at night, ' +
                      'and another cycle "smoke-tests" that runs only a few high-severity tests quickly during working days to get quality results faster on that same branch.',
        fields: [
          {
            code: 'id',
            type: 'hidden',
            newValue: -1,
            primaryKey: true
          },
          {
            code: 'branchPosition',
            name: 'Branch position',
            columnTitle: 'Branch position',
            type: 'int',
            required: true,
            newValue: 1,
            width: 128,
            help: 'The position of the branch on the home screen. ' +
              'You can put the most important branch "master" at the position 1, even if it is alphabetically after "develop". ' +
              'Modifying the position will modify it for all cycles with the same branch, because all cycles of a branch are displayed together on the home screen.'
          },
          {
            code: 'branch',
            name: 'Branch',
            columnTitle: 'Branch',
            type: 'string',
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined,
            help: 'The name of the branch in Version Control System from which tests are executed in this cycle. ' +
              'Eg. "master", "develop"...'
          },
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Cycle',
            type: 'string',
            required: true,
            newValue: '',
            businessKey: true,
            width: undefined,
            help: 'The name of the cycle: one branch in Version Control System can be executed several times. ' +
              'For instance, one fast and blocking cycle can run a subset of tests after each commit, ' +
              'while a longer but complete and not-blocking cycle can execute tests once a day or at night. ' +
              'Eg. "day" and "night", or "full" and "fast".'
          }
        ]
      }
    },

    computed: {
      url () {
        return api.paths.cycleDefinitions(this)
      }
    }
  }
</script>
