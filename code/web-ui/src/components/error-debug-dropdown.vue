<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (C) 2019 by the ARA Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ 	 http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
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

      <a v-if="executedScenario.diffReportUrl" :href="sanitizeARAUrl(executedScenario.diffReportUrl)" rel="noopener" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW DIFF REPORT
        </DropdownItem>
      </a>

      <a v-if="executedScenario.logsUrl" :href="sanitizeARAUrl(executedScenario.logsUrl)" rel="noopener" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW LOGS
        </DropdownItem>
      </a>

      <a v-if="executedScenario.httpRequestsUrl" :href="sanitizeARAUrl(executedScenario.httpRequestsUrl)" rel="noopener" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW HTTP REQUESTS
        </DropdownItem>
      </a>

      <a v-if="executedScenario.javaScriptErrorsUrl" :href="sanitizeARAUrl(executedScenario.javaScriptErrorsUrl)" rel="noopener" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW JAVASCRIPT ERRORS
        </DropdownItem>
      </a>

      <a v-if="executedScenario.cucumberReportUrl" :href="sanitizeARAUrl(executedScenario.cucumberReportUrl)" rel="noopener" target="_blank">
        <DropdownItem>
          <Icon type="md-open"/> SHOW CUCUMBER REPORT
        </DropdownItem>
      </a>

      <a :href="sanitizeARAUrl(editUrl)" rel="noopener" target="_blank">
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
      },
      sanitizeARAUrl (url) {
        return this.$sanitizeUrl(url)
      }
    }
  }
</script>
