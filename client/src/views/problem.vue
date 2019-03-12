<template>
  <div v-if="problem">
    <h2 style="margin-top: 0;">
      PROBLEM
      <problem-tag :problem="problem"/>
      <a v-if="problem.defectUrl && problem.defectExistence !== 'NONEXISTENT'" :href="problem.defectUrl" target="_blank">
        <Button icon="md-open" type="info">GO TO WORK ITEM #{{problem.defectId}}</Button>
      </a>
      <span v-if="problem.effectiveStatus === 'OPEN'">({{problem.effectiveStatus}})</span>
    </h2>

    <!-- Comment -->
    <div v-if="problem.comment" style="margin: 8px 0; position: relative; border: 1px solid #E9EAEC; border-radius: 2px; background: white; padding: 4px 4px 4px 20px; width: 60%;">
      <!-- Page-corner effect -->
      <div style="width: 14px; height: 14px; position: absolute; top: -1px; left: -1px; background: #F5F7F9;">
        <div style="width: 0; height: 0; border-style: solid; border-width: 0 0 14px 14px; border-color: transparent transparent #E9EAEC transparent;"></div>
        <div style="position: absolute; top: 2.4px; left: 2.4px; width: 0; height: 0; border-style: solid; border-width: 0 0 10.6px 10.6px; border-color: transparent transparent #FCFCFC transparent;"></div>
      </div>
      <!-- The comment in itself -->
      <strong>Comment:</strong>
      <div style="white-space: pre-line;">{{problem.comment}}</div>
    </div>

    <div style="margin: 8px 0 8px 21px;">
      <strong>Creation:</strong>
      <div>{{util.formatDate(problem.creationDateTime)}}</div>
    </div>

    <div v-if="problem.effectiveStatus !== 'OPEN' && problem.closingDateTime" style="margin: 8px 0 8px 21px;">
      <strong>Closing:</strong>
      <div>
        {{util.formatDate(problem.closingDateTime)}}
        <strong v-if="problem.effectiveStatus === 'REAPPEARED' && problem.lastSeenDateTime" style="color: #ED3F14;">
          (last reappeared on {{util.formatDate(problem.lastSeenDateTime)}})
        </strong>
      </div>
    </div>

    <div v-if="problem.rootCause" style="margin: 8px 0 8px 21px;">
      <strong>Root cause:</strong>
      <div>{{problem.rootCause.name}}</div>
    </div>

    <Alert type="error" v-if="problem.effectiveStatus === 'REAPPEARED'" class="reappearedAlert">
      <p>This problem <strong>reappeared</strong> in an execution launched after the closing date of this problem.</p>
      <p>An action is required to handle the problem again:</p>
      <ul>
        <li>
          <strong>The problem really reappeared and is not fixed yet?</strong><br>
          => reoppen the {{problem.defectUrl ? 'associated defect' : 'problem'}};
        </li>
        <li>
          <strong>A new error match the problem but has a different cause (a new regression or a new correction to be made to test scenarios)?</strong><br>
          => create a new problem (or a new pattern to an existing problem): the new problem will hide this reappeared problem.<br>
          => You could also modify the patterns of this problem to be less broad, to not match the errors of the new cause.
        </li>
      </ul>
    </Alert>

    <div style="text-align: center; margin-top: 8px;">
      <Button type="error" @click="deleteProblem" icon="md-trash" style="float: right;" :loading="loadingDelete">DELETE PROBLEM</Button>
      <Button type="primary" @click="editProperties" icon="md-create" style="float: left;">EDIT PROBLEM PROPERTIES</Button>

      <Button type="default" @click="refreshDefect" icon="md-refresh" v-if="problem.defectUrl" title="Status is automatically refreshed from the work-item.
But this can take a minute.
If you updated the work-item a few seconds ago, you may want to speed up the ARA update by clicking this button.">REFRESH STATUS FROM WORK-ITEM NOW</Button>
      <Button type="default" @click="showCloseProblemDialog" icon="md-close-circle" v-else-if="problem.effectiveStatus === 'OPEN'">CLOSE PROBLEM</Button>
      <Button type="default" @click="reopenProblem" icon="md-medical" v-else>REOPEN PROBLEM</Button>
    </div>

    <h2>STATISTICS</h2>
    <problem-list :problems="[problem]" :selectButtonText="selectButtonText" :showProblemNames="false"/>

    <h2>PATTERNS</h2>
    <table border class="table">
      <thead>
        <tr>
          <th>Release</th>
          <th>Country</th>
          <th>Type</th>
          <th>Platform</th>
          <th>Feature</th>
          <th>Feature File</th>
          <th>Scenario</th>
          <th>Step</th>
          <th>Step Definition</th>
          <th>Exception</th>
          <th style="width: 1px; text-align: center;">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="pattern in problem.patterns" :key="pattern.id">
          <td>{{pattern.release}}</td>
          <td><span v-if="pattern.country">{{pattern.country.name}}</span></td>
          <td>
            <span v-if="pattern.type">{{pattern.type.name}}</span>
            <span v-if="pattern.typeIsBrowser">Desktop</span>
            <span v-if="pattern.typeIsMobile">Mobile</span>
          </td>
          <td>{{pattern.platform}}</td>
          <td>{{pattern.featureName}}</td>
          <td>{{pattern.featureFile}}</td>
          <td><strong v-if="pattern.scenarioNameStartsWith">Starts with: </strong>{{pattern.scenarioName}}</td>
          <td><strong v-if="pattern.stepStartsWith">Starts with: </strong>{{pattern.step}}</td>
          <td><strong v-if="pattern.stepDefinitionStartsWith">Starts with: </strong>{{pattern.stepDefinition}}</td>
          <td style="white-space: pre-line;"><strong v-if="pattern.exception">Starts with: </strong>{{pattern.exception}}</td>
          <td style="width: 1px; padding: 8px;">
            <Dropdown title="Actions" trigger="click" placement="bottom-end">
              <Button type="primary">
                <Icon type="md-menu"/>
              </Button>
              <DropdownMenu slot="list">
                <DropdownItem><div @click="editPattern(pattern.id)"><Icon type="md-create"/> EDIT PATTERN</div></DropdownItem>
                <DropdownItem><div @click="movePattern(pattern.id)"><Icon type="ios-undo"/> MOVE PATTERN TO ANOTHER EXISTING PROBLEM</div></DropdownItem>
                <DropdownItem><div @click="deletePattern(pattern.id)"><Icon type="md-trash"/> DELETE PATTERN</div></DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </td>
        </tr>
      </tbody>
    </table>

    <matching-errors :matchingErrors="matchingErrors" :showProblems="false" v-on:refresh="refreshMatchingErrors" />

    <Modal v-model="showingEditor" @on-ok="requestSaveProblemProperties" @on-cancel="showingEditor = false"
           title="Edit Problem Properties" okText="Save" cancelText="Cancel"
           width="500" :loading="loadingProperties">
      <problem-properties-editor ref="problemProperties" :isClosed="problem && problem.effectiveStatus !== 'OPEN'" v-on:submit="saveProblemProperties"/>
    </Modal>

    <Modal v-model="showingCloseDialog" @on-ok="submitCloseProblemDialog" @on-cancel="showingCloseDialog = false"
           title="Close Problem" okText="Assign Root Cause &amp; Close Problem" cancelText="Cancel"
           width="500" :loading="loadingClose">
      <Form :label-width="120">
        <Form-item label="Root cause:" style="position: relative;">
          <Select v-model="rootCauseIdForClose" clearable placeholder="None">
            <Option v-for="rootCause in rootCauses" :value="rootCause.id" :key="rootCause.id" :label="rootCause.name" />
          </Select>
          <Spin fix v-if="!rootCauses"/>
        </Form-item>
      </Form>
    </Modal>

    <Modal v-model="showingProblems" @on-ok="showingProblems = false" @on-cancel="showingProblems = false"
           title="Move Pattern to Another Existing Problem" okText="Close" cancelText=""
           width="auto" class="problemsModal noFooter">
      <problems ref="problems" selectButtonText="MOVE TO" :inPopup="true" v-on:select="movePatternToProblem"/>
      <div style="clear: both;"></div>
    </Modal>

    <Spin fix v-if="loadingProblem"/>
  </div>
  <p v-else-if="deletedProblem" style="text-align: center">
    Problem deleted.<br>
    <router-link :replace="true" :to="{ name: 'problems' }">Show all problems</router-link>
  </p>
  <div v-else>
    <Spin fix v-if="loadingProblem"/>
  </div>
</template>

<script>
  import Vue from 'vue'

  import api from '../libs/api'
  import util from '../libs/util'

  import matchingErrorsComponent from '../components/matching-errors'
  import problemsComponent from '../components/problems'
  import problemTagComponent from '../components/problem-tag'
  import problemPropertiesEditorComponent from '../components/problem-properties-editor'
  import problemListComponent from '../components/problem-list'

  export default {
    name: 'problem',

    props: ['selectButtonText'],

    mixins: [{
      created () {
        this.util = util
      }
    }],

    components: {
      'matching-errors': matchingErrorsComponent,
      'problems': problemsComponent,
      'problem-tag': problemTagComponent,
      'problem-properties-editor': problemPropertiesEditorComponent,
      'problem-list': problemListComponent
    },

    computed: {
      rootCauses () {
        return this.$store.getters['rootCauses/rootCauses'](this)
      }
    },

    data () {
      return {
        // Data to display
        loadingProblem: false,
        problem: null,
        matchingErrors: matchingErrorsComponent.methods.newData(),

        // PROPERTIES EDITOR dialog
        showingEditor: false,
        loadingProperties: true,

        // MOVE TO ANOTHER PROBLEM dialog
        showingProblems: false,
        patternIdToMove: null,

        // Problem deletion success feedback
        loadingDelete: false,
        deletedProblem: false,

        // CLOSE PROBLEM dialog
        showingCloseDialog: false,
        loadingClose: true,
        rootCauseIdForClose: null
      }
    },

    methods: {
      loadProblem (skipMatchingErrorsReloading) {
        this.loadingProblem = true
        Vue.http
          .get(api.paths.problems(this) + '/' + this.$route.params.id, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingProblem = false
            this.problem = response.body
            if (!skipMatchingErrorsReloading) {
              this.matchingErrors.data = []
              this.loadMatchingErrors()
            }
          }, (error) => {
            this.loadingProblem = false
            api.handleError(error)
          })
      },

      loadMatchingErrors () {
        let url = api.pageUrl(api.paths.problems(this) + '/' + this.$route.params.id + '/errors', this.matchingErrors.paging)
        this.matchingErrors.loading = true
        Vue.http
          .get(url, api.REQUEST_OPTIONS)
          .then((response) => {
            this.matchingErrors.loading = false
            this.matchingErrors.data = response.body
          }, (error) => {
            this.matchingErrors.loading = false
            api.handleError(error)
          })
      },

      refreshMatchingErrors (newPaging) {
        this.matchingErrors.paging = newPaging
        this.loadMatchingErrors()
      },

      editProperties () {
        this.$refs.problemProperties.doInit({
          teamId: (this.problem.blamedTeam ? this.problem.blamedTeam.id : null),
          name: this.problem.name,
          status: this.problem.status,
          comment: this.problem.comment,
          defectId: this.problem.defectId,
          rootCauseId: (this.problem.rootCause ? this.problem.rootCause.id : null)
        })
        this.loadingProperties = true
        this.showingEditor = true
        this.$refs.problemProperties.focus()
      },

      requestSaveProblemProperties () {
        this.$refs.problemProperties.submit()
        return false
      },

      saveProblemProperties (problem, onDoneCallback) {
        Vue.http
          .put(api.paths.problems(this) + '/' + this.problem.id, problem, api.REQUEST_OPTIONS)
          .then((response) => {
            onDoneCallback()
            this.showingEditor = false
            this.loadingProperties = false
            this.loadProblem(true)
          }, (error) => {
            onDoneCallback()
            this.loadingProperties = false
            api.handleError(error, () => {
              this.loadingProperties = true
            })
          })
      },

      deleteProblem () {
        let url = api.paths.problems(this) + '/' + this.problem.id
        let self = this
        this.$Modal.confirm({
          title: 'Delete ' + util.escapeHtml(this.problem.name),
          content: '<p>This will delete all patterns for this problem, and dissosiate all errors from this problem.</p><p>Are you sure?</p>',
          okText: 'Delete Problem',
          onOk () {
            self.loadingDelete = true
            Vue.http
              .delete(url, api.REQUEST_OPTIONS)
              .then((response) => {
                self.loadingDelete = false
                // If go(-1) has no previous page to go to, we cannot verify that, so offer a link for the user to manually go to all problems
                self.problem = null
                self.deletedProblem = true
                self.$router.go(-1)
              }, (error) => {
                self.loadingDelete = false
                api.handleError(error)
              })
          }
        })
      },

      movePattern (patternId) {
        if (this.problem.patterns.length > 1) {
          this.reallyMovePattern(patternId)
        } else {
          let self = this
          this.$Modal.confirm({
            title: 'The Current Problem will be Deleted',
            content: '<p>The current problem only has this single pattern.<br>' +
                     'Moving this pattern to another problem will delete the current problem.</p>',
            okText: 'Move Pattern & Delete Problem',
            onOk () {
              self.reallyMovePattern(patternId)
            }
          })
        }
      },

      reallyMovePattern (patternId) {
        this.patternIdToMove = patternId
        this.showingProblems = true
        this.$refs.problems.requestProblems()
        this.$refs.problems.focus()
      },

      movePatternToProblem (destinationProblemId, destinationProblemName, onStartCallback, onDoneCallback) {
        onStartCallback()
        Vue.http
          .post(api.paths.problems(this) + '/' + destinationProblemId + '/pick-up-pattern/' + this.patternIdToMove, null, api.REQUEST_OPTIONS)
          .then((response) => {
            onDoneCallback()
            this.showingProblems = false
            if (response.body.deletedProblem) {
              this.$router.replace({ params: { id: destinationProblemId } })
              this.$Message.success({ closable: true, content: 'The initial problem has been deleted because it now has no criteria. Redirecting to the problem <b>' + util.escapeHtml(destinationProblemName) + '</b>' })
            } else {
              this.$Message.success({ closable: true, content: 'Criteria assigned to the problem <b>' + util.escapeHtml(destinationProblemName) + '</b>' })
              this.loadProblem() // Update the view to see deleted pattern and updated matching errors list
            }
          }, (error) => {
            onDoneCallback()
            api.handleError(error)
          })
      },

      editPattern (patternId) {
        this.$router.push({ name: 'problem-pattern', params: { problemId: this.problem.id, patternId } })
      },

      deletePattern (patternId) {
        let self = this
        if (this.problem.patterns.length > 1) {
          this.$Modal.confirm({
            title: 'Delete Pattern',
            content: '<p>This will dissosiate all errors from this pattern.</p><p>Are you sure?</p>',
            okText: 'Delete Pattern',
            loading: true,
            onOk () {
              self.reallyDeletePattern(patternId)
            }
          })
        } else {
          this.$Modal.confirm({
            title: 'The Current Problem will be Deleted',
            content: '<p>The current problem only has this single pattern.<br>' +
                     'Deleting this pattern will delete the current problem too.</p>',
            okText: 'Delete Pattern & Problem',
            loading: true,
            onOk () {
              self.reallyDeletePattern(patternId)
            }
          })
        }
      },

      reallyDeletePattern (patternId) {
        let self = this
        // Modal just set the loading flag on the DELETE button
        Vue.http
          .delete(api.paths.problemPatterns(this) + '/' + patternId, api.REQUEST_OPTIONS)
          .then((response) => {
            self.$Modal.remove()
            if (response.body.deletedProblem) {
              this.$Message.success({ closable: true, content: 'The problem has been deleted because it now has no criteria.' })
              // If go(-1) has no previous page to go to, we cannot verify that, so offer a link for the user to manually go to all problems
              self.problem = null
              self.deletedProblem = true
              self.$router.go(-1)
            } else {
              this.$Message.success({ closable: true, content: 'Pattern deleted.' })
              this.loadProblem() // Update the view to see deleted pattern and updated matching errors list
            }
          }, (error) => {
            self.$Modal.remove()
            api.handleError(error)
          })
        this.showingProblems = false
      },

      reopenProblem () {
        this.loadingProblem = true
        Vue.http
          .put(api.paths.problems(this) + '/' + this.problem.id + '/reopen', null, api.REQUEST_OPTIONS)
          .then((response) => {
            // Will set loadingProblem = false after success or failure of refreshing the problem:
            this.loadProblem(true)
          }, (error) => {
            this.loadingProblem = false
            api.handleError(error)
          })
      },

      refreshDefect () {
        this.loadingProblem = true
        Vue.http
          .put(api.paths.problems(this) + '/' + this.problem.id + '/refresh-defect-status', null, api.REQUEST_OPTIONS)
          .then((response) => {
            this.loadingProblem = false
            if (this.problem.effectiveStatus !== response.body.effectiveStatus) {
              // Will set loadingProblem = false after success or failure of refreshing the problem:
              this.loadProblem(true)
            }
          }, (error) => {
            this.loadingProblem = false
            let araError = error.headers.map['x-ara-error']
            if (araError && araError.length && araError[0] === 'error.no_defect_tracking_system') {
              let self = this
              this.$Modal.info({
                title: 'Unlinked Defect Tracking System',
                content: '' +
                  'The project has no defect tracker system link configured anymore in ARA. ' +
                  'The page will be refreshed: you will be able to close or open the problem manually.',
                okText: 'OK',
                onOk: () => {
                  self.loadProblem(true)
                }
              })
            } else {
              api.handleError(error)
            }
          })
      },

      showCloseProblemDialog () {
        this.rootCauseIdForClose = (this.problem && this.problem.rootCause ? this.problem.rootCause.id : null)
        this.loadingClose = true
        this.showingCloseDialog = true
        this.$store.dispatch('rootCauses/ensureRootCausesLoaded', this)
      },

      submitCloseProblemDialog () {
        if (this.rootCauseIdForClose) {
          Vue.http
            .put(api.paths.problems(this) + '/' + this.problem.id + '/close/' + this.rootCauseIdForClose, null, api.REQUEST_OPTIONS)
            .then((response) => {
              this.showingCloseDialog = false
              this.loadingClose = false
              this.loadProblem(true)
            }, (error) => {
              this.loadingClose = false
              api.handleError(error, () => {
                this.loadingClose = true
              })
            })
        } else {
          this.loadingClose = false
          setTimeout(() => {
            this.loadingClose = true // This would not work on okOk() of Modal, so we are forced to use a timeout
            this.$Modal.error({
              title: 'Error',
              content: 'You must provide a root cause before closing the problem.'
            })
          }, 0)
        }
      },
      withEllipsis (string) {
        const MAX_LENGTH = 32 // Max 3 lines of text, like other columns, etc.
        const SUFFIX = '...'

        if (!string) {
          return string
        }

        let trimmed = string.trim()
        if (trimmed.length > MAX_LENGTH - SUFFIX.length) {
          return trimmed.substr(0, MAX_LENGTH - SUFFIX.length).trim() + SUFFIX
        }
        return trimmed
      }
    },

    mounted () {
      this.loadProblem()
    },

    watch: {
      '$route' () {
        this.loadProblem()
      }
    }
  }
</script>

<style scoped>
  .problemsModal >>> .ivu-modal {
    margin: 0 16px;
  }

  .reappearedAlert p {
    margin: 8px 5px;
  }
  .reappearedAlert ul {
    margin: 8px 20px;
  }
</style>
