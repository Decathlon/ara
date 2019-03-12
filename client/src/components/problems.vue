<template>
  <Spin fix v-if="!teamsAssignableToProblems || !teamsAssignableToProblems.length || !rootCauses"/>
  <div v-else>
    <div :class="'subMenu' + (inPopup ? ' inPopup' : '')" style="border-bottom: 1px solid #dddee1;">
      Status:
      <Select v-model="filter.status" placeholder="All" style="width: 190px; text-align: left;"
              @on-change="requestProblems">
        <Option value="" :label="'\u00A0'"/>
        <Option value="OPEN" label="OPEN"/>
        <Option value="CLOSED" label="CLOSED"/>
        <Option value="REAPPEARED" label="REAPPEARED"/>
        <Option value="OPEN_OR_REAPPEARED" label="OPEN or REAPPEARED"/>
        <Option value="CLOSED_OR_REAPPEARED" label="CLOSED or REAPPEARED"/>
      </Select>
      &nbsp;

      For team:
      <Select v-model="filter.blamedTeamId" filterable placeholder="All" style="width: 200px; text-align: left;"
              @on-change="requestProblems">
        <Option value="" :label="'\u00A0'"/>
        <Option v-for="team in teamsAssignableToProblems" :value="team.id" :key="team.id" :label="team.name"/>
      </Select>
      &nbsp;

      Problem name:
      <Input ref="name" v-model="filter.name" style="width: 100px; text-align: left;" @on-change="requestProblems"/>
      &nbsp;

      Work item:
      #<Input v-model="filter.defectId" style="width: 50px; text-align: left;" @on-change="requestProblems" title="TIP: type 'none' to show problems without work item"/>
      <Checkbox v-model="filter.defectExistence" true-value="NONEXISTENT" @on-change="requestProblems">Nonexistent</Checkbox>
      &nbsp;

      Root cause:
      <Select v-model="filter.rootCauseId" placeholder="All" style="width: 200px; text-align: left;"
              @on-change="requestProblems">
        <Option value="" :label="'\u00A0'"/>
        <Option v-for="rootCause in rootCauses" :value="rootCause.id" :key="rootCause.id" :label="rootCause.name"/>
      </Select>
    </div>

    <div class="spinnable">
      <problem-list :problems="problems.content" :selectButtonText="selectButtonText" :showProblemNames="true" v-on:select="reemitSelect"/>
      <Spin fix v-if="loadingProblems"/>
    </div>
    <Page
        style="float: right;"
        :total="problems.totalElements"
        :current="problemsPaging.page + 1"
        :page-size="problemsPaging.size"
        show-total
        show-sizer
        size="small"
        @on-change="onProblemsPageChange"
        @on-page-size-change="onProblemsPageSizeChange"/>
    <div style="clear: both;"></div>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'
  import problemListComponent from '../components/problem-list'

  const DEFAULT_PAGE_SIZE = 10

  export default {
    name: 'problems-component',

    props: ['inPopup', 'filtersInQueryString', 'selectButtonText'],

    computed: {
      teamsAssignableToProblems () {
        return this.$store.getters['teams/teamsAssignableToProblems'](this)
      },

      rootCauses () {
        return this.$store.getters['rootCauses/rootCauses'](this)
      }
    },

    components: {
      'problem-list': problemListComponent
    },

    data () {
      return {
        filter: {
          status: '',
          blamedTeamId: '',
          name: '',
          defectId: '',
          defectExistence: '',
          rootCauseId: ''
        },
        loadingProblems: false,
        requestedProblems: false,
        problems: [],
        problemsPaging: {
          page: 0,
          size: DEFAULT_PAGE_SIZE
        },

        selectedProblemId: null,
        loadingSelect: false
      }
    },

    methods: {
      requestProblems () {
        if (this.filtersInQueryString) {
          this.recomputeQueryString()
        } else {
          if (this.loadingProblems) {
            this.requestedProblems = true
          } else {
            this.loadProblems()
          }
        }
      },

      loadProblems () {
        this.$store.dispatch('rootCauses/ensureRootCausesLoaded', this)

        this.requestedProblems = false
        let url = api.pageUrl(api.paths.problems(this) + '/filter', this.problemsPaging)
        let filter = { // `|| null` to not send '' (default filter values are empty strings)
          status: (this.filter.status || null),
          blamedTeamId: (this.filter.blamedTeamId || null),
          name: (this.filter.name || null),
          defectId: (this.filter.defectId || null),
          defectExistence: (this.filter.defectExistence || null),
          rootCauseId: (this.filter.rootCauseId || null)
        }
        this.loadingProblems = true
        Vue.http
        .post(url, filter, api.REQUEST_OPTIONS)
        .then((response) => {
          this.loadingProblems = false
          this.problems = response.body

          let pageCount = this.problems.totalPages
          let lastPage = (pageCount === 0 ? 0 : pageCount - 1)
          if (this.problems.number > lastPage) {
            // There was a lot of page, we were on one of the last pages, and after reloading, the number of page is now small: go to last one
            this.problemsPaging.page = lastPage
            if (this.filtersInQueryString) {
              this.recomputeQueryString()
            } else {
              this.requestedProblems = true
            }
          }

          if (this.requestedProblems) {
            this.loadProblems()
          }
        }, (error) => {
          this.loadingProblems = false
          api.handleError(error)
          if (this.requestedProblems) {
            this.loadProblems()
          }
        })
      },

      onProblemsPageChange (pageNumber) {
        this.problemsPaging.page = pageNumber - 1
        this.recomputeQueryString()
        if (!this.filtersInQueryString) {
          this.loadProblems()
        }
      },

      onProblemsPageSizeChange (pageSize) {
        this.problemsPaging.page = 0
        this.problemsPaging.size = pageSize
        this.recomputeQueryString()
        if (!this.filtersInQueryString) {
          this.loadProblems()
        }
      },
      focus () {
        this.$nextTick(() => this.$refs.name.focus())
      },

      recomputeQueryString () {
        if (!this.filtersInQueryString) {
          return
        }
        let query = {}
        for (var propertyName in this.filter) {
          if (this.filter[propertyName]) {
            query[propertyName] = this.filter[propertyName]
          }
        }
        if (this.problemsPaging.page > 0) {
          query['page'] = this.problemsPaging.page + 1
        }
        if (this.problemsPaging.size !== DEFAULT_PAGE_SIZE) {
          query['pageSize'] = this.problemsPaging.size
        }
        this.$router.replace({ query })
      },

      reemitSelect () {
        this.$emit('select', ...arguments)
      },

      fromQueryString () {
        if (!this.filtersInQueryString) {
          return
        }
        let query = this.$route.query
        if (query) {
          for (var propertyName in this.filter) {
            let value = query[propertyName] || ''
            if (value) {
              this.filter[propertyName] = (propertyName === 'blamedTeamId' || propertyName === 'rootCauseId' ? parseInt(value, 10) : value)
            } else {
              this.filter[propertyName] = ''
            }
          }
          if (query['page']) {
            this.problemsPaging.page = parseInt(query['page'], 10) - 1
          }
          if (query['pageSize']) {
            this.problemsPaging.size = parseInt(query['pageSize'], 10)
          }
        }
      }
    },

    mounted () {
      if (this.filtersInQueryString) {
        this.fromQueryString()
        this.loadProblems()
      }
    },

    watch: {
      '$route' () {
        if (this.filtersInQueryString) {
          this.fromQueryString()
          if (this.loadingProblems) {
            this.requestedProblems = true
          } else {
            this.loadProblems()
          }
        }
      }
    }
  }
</script>
