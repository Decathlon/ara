<template>
  <Dropdown trigger="click" placement="bottom-end" style="display: block;">
    <Button v-if="buttonName" icon="md-menu" :title="buttonTitle" :class="buttonClass" :size="size">{{buttonName}}</Button>
    <Button v-else            icon="md-menu" :title="buttonTitle" :class="buttonClass" :size="size"/> <!-- Do not generate a one-space span -->
    <DropdownMenu slot="list">

      <div v-if="showGoToErrorPage && error" @click="goToErrorsPage(error.id)">
        <router-link :to="{ name: 'error', params: { id: error.id } }">
          <DropdownItem>
            <Icon type="md-bug"/> GO TO ERROR'S PAGE
          </DropdownItem>
        </router-link>
      </div>

      <DropdownItem v-if="executedScenario.startDateTime" disabled>
        Execution of scenario started at {{formatDate(executedScenario.startDateTime, true).toUpperCase()}}
      </DropdownItem>

      <DropdownItem v-if="executedScenario.apiServer" disabled>
        This HTTP session used {{executedScenario.apiServer}}
      </DropdownItem>

      <DropdownItem v-if="executedScenario.seleniumNode" disabled>
        This scenario used selenium node {{executedScenario.seleniumNode}}
      </DropdownItem>

      <a v-if="executedScenario.diffReportUrl" :href="executedScenario.diffReportUrl" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW DIFF REPORT
        </DropdownItem>
      </a>

      <a v-if="executedScenario.logsUrl" :href="executedScenario.logsUrl" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW LOGS
        </DropdownItem>
      </a>

      <a v-if="executedScenario.httpRequestsUrl" :href="executedScenario.httpRequestsUrl" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW HTTP REQUESTS
        </DropdownItem>
      </a>

      <a v-if="executedScenario.javaScriptErrorsUrl" :href="executedScenario.javaScriptErrorsUrl" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW JAVASCRIPT ERRORS
        </DropdownItem>
      </a>

      <a v-if="executedScenario.cucumberReportUrl" :href="executedScenario.cucumberReportUrl" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW CUCUMBER REPORT
        </DropdownItem>
      </a>

      <a :href="editUrl" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> EDIT SCENARIO
        </DropdownItem>
      </a>

    </DropdownMenu>
  </Dropdown>
</template>

<script>
  import util from '../libs/util'

  export default {
    name: 'error-debug-dropdown',

    mixins: [
      {
        methods: util
      }
    ],

    props: [ 'executedScenario', 'error', 'buttonName', 'buttonTitle', 'buttonClass', 'showGoToErrorPage', 'size' ],

    computed: {
      editUrl () {
        let source = this.executedScenario.run.type.source
        let branch = this.executedScenario.run.execution.branch
        let featureFile = this.executedScenario.featureFile
        let folderUrl = source.vcsUrl.replace('{{branch}}', branch)
        return folderUrl + featureFile
      }
    },

    methods: {
      goToErrorsPage (errorId) {
        this.$router.push({ name: 'error', params: { id: errorId } })
      }
    }
  }
</script>
