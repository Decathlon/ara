<template>
  <div>
    <Dropdown title="Actions &amp; Job Reports" trigger="click" placement="bottom-end">
      <Button>
        <Icon type="md-menu"/>
      </Button>
      <DropdownMenu slot="list" style="text-transform: initial;" title="">
        <DropdownItem :disabled="true" style="font-weight: bold;">
          <Icon type="md-open" style="visibility: hidden;"/>
          ACTIONS
        </DropdownItem>

        <DropdownItem><div @click="discard(execution)"><Icon type="md-flag"/> {{execution.discardReason ? 'CHANGE DISCARD REASON' : 'DISCARD EXECUTION'}}</div></DropdownItem>
        <DropdownItem v-if="execution.discardReason"><div @click="unDiscard(execution.id)"><Icon type="md-checkmark"/> UN-DISCARD EXECUTION</div></DropdownItem>

        <DropdownItem :disabled="true" style="border-top: 1px solid #DDDEE1; font-weight: bold;">
          <Icon type="md-open" style="visibility: hidden;"/>
          JOB REPORTS
        </DropdownItem>

        <a :href="execution.jobUrl ? (execution.jobUrl + 'console') : null" target="_blank">
          <DropdownItem :disabled="!execution.jobUrl">
            <Icon type="md-open"/>
            Execution
            <job-status-result :job="execution" :showDuration="true"/>
          </DropdownItem>
        </a>
        <div v-for="countryDeployment in execution.countryDeployments" :key="countryDeployment.id">
          <a :href="countryDeployment.jobUrl ? (countryDeployment.jobUrl + 'console') : null" target="_blank">
            <DropdownItem :disabled="!countryDeployment.jobUrl" style="border-top: 1px dashed #DDDEE1;">
              <Icon type="md-open"/>
              {{countryDeployment.country.code.toUpperCase()}} Deployment
              <job-status-result :job="countryDeployment" :showDuration="true"/>
            </DropdownItem>
          </a>
          <a v-for="run in execution.runs" :key="run.id" v-if="run.country.code === countryDeployment.country.code"
              :href="run.jobUrl ? (run.jobUrl + 'cucumber-html-reports/overview-features.html') : null"
              target="_blank">
            <DropdownItem :disabled="!run.jobUrl">
              <Icon type="md-open"/>
              {{run.country.code.toUpperCase()}} {{run.type.name}}
              <job-status-result :job="run" :showDuration="true"/>
            </DropdownItem>
          </a>
        </div>
      </DropdownMenu>
    </Dropdown>

    <Modal v-model="showDiscardDialog" @on-ok="confirmDiscard"
           title="Discard Execution"
           okText="Discard" :loading="loadingDiscard" ref="discardPopup">
      <p class="help">
        Sometimes, an execution is exceptionaly wrong:
        a general network issue, a version that renders the server unusable...
      </p>
      <p class="help">
        Mark such execution as "not worth analyzing" by discarding it,
        and telling the reason to others.
      </p>
      <p class="help">
        The execution will not appear as the default one on the home screen of ARA,
        revealing the previous not-discarded one for your team to analyze current regressions instead of loosing time by focusing on a bad execution.
      </p>
      Discard reason: <Input ref="name" v-model="discardReason" @on-enter="confirmDiscard" />
    </Modal>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  import jobStatusResultComponent from '../components/job-status-result'

  export default {
    name: 'execution-actions-button',

    props: [ 'execution' ],

    components: {
      'job-status-result': jobStatusResultComponent
    },

    data () {
      return {
        loadingDiscard: false,
        executionToDiscard: null,
        showDiscardDialog: false,
        discardReason: ''
      }
    },

    methods: {
      discard (execution) {
        this.discardReason = execution.discardReason
        this.executionToDiscard = execution
        this.loadingDiscard = true
        this.showDiscardDialog = true
        this.$nextTick(() => this.$refs.name.focus())
      },

      confirmDiscard () {
        if (this.discardReason) {
          let url = api.paths.executions(this) + '/' + this.executionToDiscard.id + '/discard'
          Vue.http
            .put(url, this.discardReason, api.REQUEST_OPTIONS)
            .then((response) => {
              this.loadingDiscard = false
              this.showDiscardDialog = false
              this.$emit('change')
            }, (error) => {
              this.loadingDiscard = false
              api.handleError(error, () => {
                this.loadingDiscard = true
              })
            })
        } else {
          this.loadingDiscard = false
          setTimeout(() => {
            this.loadingDiscard = true // This would not work on okOk() of Modal, so we are forced to use a timeout
            this.$Modal.error({
              title: 'Error',
              content: 'A reason is mandatory when discarding an execution.'
            })
          }, 0)
        }
      },

      unDiscard (executionId) {
        let self = this
        this.$Modal.confirm({
          title: 'Un-Discard Execution',
          content: '<p>Un-discard this execution to mark it as relevant again?</p>',
          okText: 'Un-Discard',
          loading: true,
          onOk () {
            let url = api.paths.executions(self) + '/' + executionId + '/un-discard'
            Vue.http
              .put(url, api.REQUEST_OPTIONS)
              .then((response) => {
                self.$Modal.remove()
                self.$emit('change')
              }, (error) => {
                self.$Modal.remove()
                api.handleError(error)
              })
          }
        })
      }
    }
  }
</script>

<style scoped>
  p.help {
    margin-bottom: 8px;
    color: gray;
  }
</style>

