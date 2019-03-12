<template>
  <div>
    <h2>
      MATCHING ERRORS<span v-if="errors && errors.totalElements">
      ({{errors.totalElements}})</span>
    </h2>
    <div class="spinnable">
      <table border class="table">
        <thead>
          <tr>
            <th style="white-space: nowrap;">
              Date<br>
              Branch | Version<br>
              Country | Type | Platform
            </th>
            <th>Feature</th>
            <th>Scenario</th>
            <th>Step</th>
            <th>Exception</th>
            <th>
              Screenshot<br>
              Video<br>
              History<br>
              Debug information
            </th>
            <th v-if="showProblems">Problem(s)</th>
          </tr>
        </thead>
        <tbody v-if="errors">
          <tr v-for="matchingError in errors.content" :key="matchingError.id">
            <td style="white-space: nowrap; width: 1px;">
              <span :class="{discarded: isDiscarded(matchingError)}">
                {{formatDate(matchingError.executedScenario.run.execution.testDateTime)}}<br>
                {{matchingError.executedScenario.run.execution.branch}} |
                {{matchingError.executedScenario.run.execution.version}}<br>
                {{matchingError.executedScenario.run.country.name}} |
                {{matchingError.executedScenario.run.type.name}} |
                {{matchingError.executedScenario.run.platform}}<br>
              </span>
              <span v-if="isDiscarded(matchingError)" style="white-space: normal; color: #ED3F14;">
                Discarded: {{matchingError.executedScenario.run.execution.discardReason}}
              </span>
            </td>
            <td>{{matchingError.executedScenario.featureName}}</td>
            <td>{{matchingError.executedScenario.name}}</td>
            <td><a @click="showMatchingErrorScenario(matchingError)">{{matchingError.step}}</a></td>
            <td><a @click="showMatchingErrorException(matchingError)">{{matchingError.miniException}}</a></td>
            <td style="width: 200px; text-align: center;">
              <Button-group shape="circle">
                <Button v-if="matchingError.executedScenario.screenshotUrl" @click="showMatchingErrorScreenshot(matchingError)" icon="md-image" title="Screenshot" />
                <Button v-if="matchingError.executedScenario.videoUrl" @click="showMatchingErrorVideo(matchingError)" icon="logo-youtube" title="Video" />
                <Button @click="showMatchingErrorHistory(matchingError)" icon="md-time" title="History" />
                <error-debug-dropdown :executedScenario="matchingError.executedScenario" :error="matchingError" buttonTitle="Debug information" :showGoToErrorPage="true" style="display: inline-block;"/>
              </Button-group>
            </td>
            <td v-if="showProblems">
              <problem-tags :problems="matchingError.problems"/>
            </td>
          </tr>
        </tbody>
      </table>
      <Spin fix v-if="matchingErrors.loading" />
    </div>
    <Page v-if="matchingErrors.data"
      style="float: right;"
      :total="matchingErrors.data.totalElements"
      :current="matchingErrors.paging.page + 1"
      :page-size="matchingErrors.paging.size"
      show-total
      show-sizer
      size="small"
      @on-change="onPageChange"
      @on-page-size-change="onPageSizeChange">
        &nbsp; <!-- Don't show "Total X items", as it's already in title -->
    </Page>
    <div style="clear: both;"></div>

    <error-popups ref="errorPopups" />
  </div>
</template>

<script>
  import errorPopupsComponent from '../components/error-popups'
  import errorDebugDropdownComponent from './error-debug-dropdown'
  import problemTagsComponent from '../components/problem-tags'

  import util from '../libs/util'
  import exceptionUtil from '../libs/exception-util'

  export default {
    name: 'matching-errors',

    mixins: [{
      methods: util
    }],

    props: [ 'matchingErrors', 'showProblems' ],

    components: {
      'error-popups': errorPopupsComponent,
      'error-debug-dropdown': errorDebugDropdownComponent,
      'problem-tags': problemTagsComponent
    },
    computed: {
      errors () {
        if (this.matchingErrors.data) {
          // We were on one of the last of many pages, and after reloading, the number of page is now small? Go to last one
          let pageCount = this.matchingErrors.data.totalPages
          let lastPage = (pageCount === 0 ? 0 : pageCount - 1)
          if (this.matchingErrors.data.number > lastPage) {
            this.emitRefresh({ ...this.matchingErrors.paging, page: lastPage })
          }

          // Decorate each error with a summary of the full exception message
          for (let i in this.matchingErrors.data.content) {
            let matchingError = this.matchingErrors.data.content[i]
            matchingError.miniException = exceptionUtil.getErrorSummary(matchingError.exception)
          }
        }

        return this.matchingErrors.data
      }
    },

    methods: {
      newData () {
        return {
          loading: false,
          data: null,
          paging: {
            page: 0,
            size: 10
          }
        }
      },
      isDiscarded (matchingError) {
        return matchingError.executedScenario.run.execution.acceptance === 'DISCARDED'
      },
      emitRefresh (newPaging) {
        this.$emit('refresh', newPaging)
      },

      onPageChange (pageNumber) {
        this.emitRefresh({ ...this.matchingErrors.paging, page: pageNumber - 1 })
      },

      onPageSizeChange (pageSize) {
        this.emitRefresh({ page: 0, size: pageSize })
      },

      showMatchingErrorScreenshot (matchingError) {
        this.$refs.errorPopups.showScreenshot(matchingError.executedScenario)
      },

      showMatchingErrorVideo (matchingError) {
        this.$refs.errorPopups.showVideo(matchingError.executedScenario)
      },

      showMatchingErrorHistory (matchingError) {
        this.$refs.errorPopups.showHistory(matchingError.executedScenario)
      },

      showMatchingErrorScenario (matchingError) {
        this.$refs.errorPopups.showScenario(matchingError.executedScenario, matchingError)
      },

      showMatchingErrorException (matchingError) {
        this.$refs.errorPopups.showException(matchingError.executedScenario, matchingError)
      }
    }
  }
</script>
<style scoped>
  .discarded {
    text-decoration: line-through;
    color: #ED3F14;
  }
</style>
