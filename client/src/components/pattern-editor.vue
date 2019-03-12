<template>
  <div v-if="form" :class="editMode ? 'edit' : ''">
    <h2 v-if="!editMode">AGGREGATION CRITERIA</h2>
    <div>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useRelease" @on-change="requestMatchingErrors">Release:</Checkbox>
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Select v-if="editMode && form.useRelease && distinctErrorProperties.releases" v-model="form.release" filterable :disabled="!form.useRelease" @on-change="requestMatchingErrors">
            <Option v-for="release in distinctErrorProperties.releases" :value="release" :key="release" :label="release" />
          </Select>
          <span v-else :class="form.useRelease ? '' : 'disabled'">{{form.release}}</span>
          <Button v-if="editMode && form.useRelease && !distinctErrorProperties.releases" @click="loadDistinctErrorProperties('releases')" :loading="loadingDistinctErrorProperties['releases']">CHANGE</Button>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useCountry" @on-change="requestMatchingErrors">Country:</Checkbox>
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Select v-if="editMode && form.useCountry && distinctErrorProperties.countries && form.country" v-model="form.country.code" filterable :disabled="!form.useCountry" @on-change="requestMatchingErrors">
            <Option v-for="country in distinctErrorProperties.countries" :value="country.code" :key="country.code" :label="country.name" />
          </Select>
          <span v-else :class="form.useCountry ? '' : 'disabled'">{{form.country ? form.country.name : ''}}</span>
          <Button v-if="editMode && form.useCountry && !distinctErrorProperties.countries" @click="loadDistinctErrorProperties('countries')" :loading="loadingDistinctErrorProperties['countries']">CHANGE</Button>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useType" @on-change="requestMatchingErrors">Type:</Checkbox>
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Select v-if="editMode && form.useType && distinctErrorProperties.types && form.type" v-model="form.type.code" filterable :disabled="!form.useType" @on-change="requestMatchingErrors">
            <Option v-for="type in distinctErrorProperties.types" :value="type.code" :key="type.code" :label="type.name" />
          </Select>
          <span v-else :class="form.useType ? '' : 'disabled'">{{form.type ? form.type.name : ''}}</span>
          <Button v-if="editMode && form.useType && !distinctErrorProperties.types" @click="loadDistinctErrorProperties('types')" :loading="loadingDistinctErrorProperties['types']">CHANGE</Button>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.usePlatform" @on-change="requestMatchingErrors">Platform:</Checkbox>
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Select v-if="editMode && form.usePlatform && distinctErrorProperties.platforms" v-model="form.platform" filterable :disabled="!form.usePlatform" @on-change="requestMatchingErrors">
            <Option v-for="platform in distinctErrorProperties.platforms" :value="platform" :key="platform" :label="platform" />
          </Select>
          <span v-else :class="form.usePlatform ? '' : 'disabled'">{{form.platform}}</span>
          <Button v-if="editMode && form.usePlatform && !distinctErrorProperties.platforms" @click="loadDistinctErrorProperties('platforms')" :loading="loadingDistinctErrorProperties['platforms']">CHANGE</Button>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useFeatureName" @on-change="requestMatchingErrors">Feature:</Checkbox>
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Select v-if="editMode && form.useFeatureName && distinctErrorProperties.featureNames" v-model="form.featureName" filterable :disabled="!form.useFeatureName" @on-change="requestMatchingErrors">
            <Option v-for="featureName in distinctErrorProperties.featureNames" :value="featureName" :key="featureName" :label="featureName" />
          </Select>
          <span v-else :class="form.useFeatureName ? '' : 'disabled'">{{form.featureName}}</span>
          <Button v-if="editMode && form.useFeatureName && !distinctErrorProperties.featureNames" @click="loadDistinctErrorProperties('featureNames')" :loading="loadingDistinctErrorProperties['featureNames']">CHANGE</Button>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useFeatureFile" @on-change="requestMatchingErrors">Feature file:</Checkbox>
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Select v-if="editMode && form.useFeatureFile && distinctErrorProperties.featureFiles" v-model="form.featureFile" filterable :disabled="!form.useFeatureFile" @on-change="requestMatchingErrors">
            <Option v-for="featureFile in distinctErrorProperties.featureFiles" :value="featureFile" :key="featureFile" :label="featureFile" />
          </Select>
          <span v-else :class="form.useFeatureFile ? '' : 'disabled'">{{form.featureFile}}</span>
          <Button v-if="editMode && form.useFeatureFile && !distinctErrorProperties.featureFiles" @click="loadDistinctErrorProperties('featureFiles')" :loading="loadingDistinctErrorProperties['featureFiles']">CHANGE</Button>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useScenarioName" @on-change="requestMatchingErrors" style="margin-right: 0;">
            Scenario{{!form.useScenarioName && form.scenarioNameStartsWith === 'true' ? ' starts with' : ''}}<!--
       --></Checkbox><!--
       --><Select v-if="form.useScenarioName"
                  @on-change="requestMatchingErrors"
                  v-model="form.scenarioNameStartsWith"
                  size="small"
                  style="width: auto; margin-left: 4px;">
            <Option :value="'false'" label="is"/>
            <Option :value="'true'" label="starts with"/>
          </Select><!-- KEEP ALL COMMENTS for the colon never to have a space before it
       -->:
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Input v-if="form.useScenarioName && form.scenarioNameStartsWith === 'true'" v-model="form.scenarioName" @on-change="requestMatchingErrors"/>
          <Select v-else-if="editMode && form.useScenarioName && distinctErrorProperties.scenarioNames" v-model="form.scenarioName" filterable :disabled="!form.useScenarioName" @on-change="requestMatchingErrors">
            <Option v-for="scenarioName in distinctErrorProperties.scenarioNames" :value="scenarioName" :key="scenarioName" :label="scenarioName" />
          </Select>
          <span v-else :class="form.useScenarioName ? '' : 'disabled'">{{form.scenarioName}}</span>
          <Button v-if="editMode && form.useScenarioName && form.scenarioNameStartsWith === 'false' && !distinctErrorProperties.scenarioNames" @click="loadDistinctErrorProperties('scenarioNames')" :loading="loadingDistinctErrorProperties['scenarioNames']">CHANGE</Button>
          <span v-if="form.useScenarioName && form.scenarioNameStartsWith === 'true'" class="tip">{{tip('scenarios')}}</span>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useStep" @on-change="requestMatchingErrors" style="margin-right: 0;">
            Step{{!form.useStep && form.stepStartsWith === 'true' ? ' starts with' : ''}}<!--
       --></Checkbox><!--
       --><Select v-if="form.useStep"
                  @on-change="requestMatchingErrors"
                  v-model="form.stepStartsWith"
                  size="small"
                  style="width: auto; margin-left: 4px;">
            <Option :value="'false'" label="is"/>
            <Option :value="'true'" label="starts with"/>
          </Select><!-- KEEP ALL COMMENTS for the colon never to have a space before it
       -->:
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Input v-if="form.useStep && form.stepStartsWith === 'true'" v-model="form.step" @on-change="requestMatchingErrors"/>
          <Select v-else-if="editMode && form.useStep && distinctErrorProperties.steps" v-model="form.step" filterable :disabled="!form.useStep" @on-change="requestMatchingErrors">
            <Option v-for="step in distinctErrorProperties.steps" :value="step" :key="step" :label="step" />
          </Select>
          <span v-else :class="form.useStep ? '' : 'disabled'">{{form.step}}</span>
          <Button v-if="editMode && form.useStep && form.stepStartsWith === 'false' && !distinctErrorProperties.steps" @click="loadDistinctErrorProperties('steps')" :loading="loadingDistinctErrorProperties['steps']">CHANGE</Button>
          <span v-if="form.useStep && form.stepStartsWith === 'true'" class="tip">{{tip('steps')}}</span>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useStepDefinition" @on-change="requestMatchingErrors" style="margin-right: 0;">
            Step definition{{!form.useStepDefinition && form.stepDefinitionStartsWith === 'true' ? ' starts with' : ''}}<!--
       --></Checkbox><!--
       --><Select v-if="form.useStepDefinition"
                  @on-change="requestMatchingErrors"
                  v-model="form.stepDefinitionStartsWith"
                  size="small"
                  style="width: auto; margin-left: 4px;">
            <Option :value="'false'" label="is"/>
            <Option :value="'true'" label="starts with"/>
          </Select><!-- KEEP ALL COMMENTS for the colon never to have a space before it
       -->:
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Input v-if="form.useStepDefinition && form.stepDefinitionStartsWith === 'true'" v-model="form.stepDefinition" @on-change="requestMatchingErrors"/>
          <Select v-else-if="editMode && form.useStepDefinition && distinctErrorProperties.stepDefinitions" v-model="form.stepDefinition" filterable :disabled="!form.useStepDefinition" @on-change="requestMatchingErrors">
            <Option v-for="stepDefinition in distinctErrorProperties.stepDefinitions" :value="stepDefinition" :key="stepDefinition" :label="stepDefinition" />
          </Select>
          <span v-else :class="form.useStepDefinition ? '' : 'disabled'">{{form.stepDefinition}}</span>
          <Button v-if="editMode && form.useStepDefinition && form.stepDefinitionStartsWith === 'false' && !distinctErrorProperties.stepDefinitions" @click="loadDistinctErrorProperties('stepDefinitions')" :loading="loadingDistinctErrorProperties['stepDefinitions']">CHANGE</Button>
          <span v-if="form.useStepDefinition && form.stepDefinitionStartsWith === 'true'" class="tip">{{tip('step definitions')}}</span>
        </i-col>
      </Row>
      <Row>
        <i-col :span="LABEL_SPAN" class="label">
          <Checkbox v-model="form.useException" @on-change="requestMatchingErrors">Exception <strong style="background-color: #2c3e50; color: white; border-radius: 3px; padding: 0 3px;">starts with</strong>:</Checkbox>
        </i-col>
        <i-col :span="VALUE_SPAN" class="value">
          <Input v-model="form.exception" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" :disabled="!form.useException" @on-change="requestMatchingErrors" />
          <span v-if="form.useException" class="tip">{{tip('exceptions')}}</span>
        </i-col>
      </Row>
    </div>

    <matching-errors :matchingErrors="matchingErrors" :showProblems="true" v-on:refresh="refreshMatchingErrors" />
  </div>
</template>

<script>
  import Vue from 'vue'

  import matchingErrorsComponent from '../components/matching-errors'

  import api from '../libs/api'

  const EMPTY_FORM = {
    useRelease: false,
    useCountry: false,
    useType: true,
    usePlatform: false,
    useFeatureName: false,
    useFeatureFile: false,
    useScenarioName: false,
    useStep: false,
    useStepDefinition: false,
    useException: false,

    scenarioNameStartsWith: 'false',
    stepStartsWith: 'false',
    stepDefinitionStartsWith: 'false',

    release: '',
    country: '',
    type: '',
    platform: '',
    featureName: '',
    featureFile: '',
    scenarioName: '',
    step: '',
    stepDefinition: '',
    exception: ''
  }

  export default {
    name: 'pattern-editor',

    components: {
      'matching-errors': matchingErrorsComponent
    },

    data () {
      return {
        // Form fields should never be undefined, especially for <Select/> ones, because they are used as v-model
        form: { ...EMPTY_FORM },
        matchingErrors: matchingErrorsComponent.methods.newData(),
        requestedMatchingErrors: false,
        loadingDistinctErrorProperties: [],
        distinctErrorProperties: []
      }
    },

    computed: {
      LABEL_SPAN: () => 5,
      VALUE_SPAN: () => 19
    },

    props: [ 'initialForm', 'editMode' ],

    methods: {
      setForm (initialForm) {
        for (let key in EMPTY_FORM) {
          this.$set(this.form, key, EMPTY_FORM[key])
        }
        for (let key in initialForm) {
          let value = initialForm[key]
          // <Select/> do not allow boolean values
          if (key.endsWith('StartsWith')) {
            value = (value ? 'true' : 'false')
          }
          this.$set(this.form, key, value)
        }

        // These objects should not be undefined, for the Select v-models to work
        if (!this.form.country) {
          this.form.country = {
            code: null,
            name: null
          }
        }
        if (!this.form.type) {
          this.form.type = {
            code: null,
            name: null
          }
        }

        this.requestMatchingErrors()
      },

      checkPattern (callBack) {
        let pattern = this.toPattern()
        if (pattern.exception && pattern.exception.length > 300) {
          this.$Modal.confirm({
            title: 'Exception Criterion Quite Long',
            content: '<p>The exception criterion is quite long: it has ' + pattern.exception.length + ' characters.<br>' +
                     'It <b>might</b> be a sign it contains too much technical details that make the criterion very specific to only a few errors.</p>' +
                     '<p></p>' +
                     '<p>Please make sure it contains:</p>' +
                     '<ul>' +
                     '<li>* No source-code file or line number (.java:... or .feature:...)</li>' +
                     '<li>* No environment-dependent URLs</li>' +
                     '<li>* No actual list of found elements when failed to find one particular element in this list</li>',
            okText: 'Continue Anyway',
            onOk () {
              callBack(pattern)
            }
          })
        } else {
          callBack(pattern)
        }
      },

      toPattern () {
        let pattern = {}
        if (this.form.useFeatureFile) {
          pattern.featureFile = this.form.featureFile
        }
        if (this.form.useFeatureName) {
          pattern.featureName = this.form.featureName
        }
        if (this.form.useScenarioName) {
          pattern.scenarioName = this.form.scenarioName
          if (this.form.scenarioNameStartsWith === 'true') {
            pattern.scenarioNameStartsWith = true
          }
        }
        if (this.form.useStep) {
          pattern.step = this.form.step
          if (this.form.stepStartsWith === 'true') {
            pattern.stepStartsWith = true
          }
        }
        if (this.form.useStepDefinition) {
          pattern.stepDefinition = this.form.stepDefinition
          if (this.form.stepDefinitionStartsWith === 'true') {
            pattern.stepDefinitionStartsWith = true
          }
        }
        if (this.form.useException) {
          pattern.exception = (this.form.exception || '').replace('\r\n', '\n').trim()
        }
        if (this.form.useRelease) {
          pattern.release = this.form.release
        }
        if (this.form.useCountry) {
          pattern.country = { code: this.form.country.code }
        }
        if (this.form.useType) {
          pattern.type = { code: this.form.type.code }
        }
        // TODO typeIsBrowser
        // TODO typeIsMobile
        if (this.form.usePlatform) {
          pattern.platform = this.form.platform
        }
        return pattern
      },

      requestMatchingErrors () {
        if (this.matchingErrors.loading) {
          this.requestedMatchingErrors = true
        } else {
          this.loadMatchingErrors()
        }
      },

      loadMatchingErrors () {
        let pattern = this.toPattern()
        this.requestedMatchingErrors = false
        let url = api.pageUrl(api.paths.errors(this) + '/matching', this.matchingErrors.paging)
        this.matchingErrors.loading = true
        Vue.http
          .post(url, pattern, api.REQUEST_OPTIONS)
          .then((response) => {
            this.matchingErrors.loading = false
            this.matchingErrors.data = response.body
            if (this.requestedMatchingErrors) {
              this.loadMatchingErrors()
            }
          }, (error) => {
            this.matchingErrors.loading = false
            api.handleError(error)
            if (this.requestedMatchingErrors) {
              this.loadMatchingErrors()
            }
          })
      },

      refreshMatchingErrors (newPaging) {
        this.matchingErrors.paging = newPaging
        this.requestMatchingErrors()
      },

      loadDistinctErrorProperties (property) {
        this.$set(this.loadingDistinctErrorProperties, property, true)
        Vue.http
          .get(api.paths.errors(this) + '/distinct/' + property, api.REQUEST_OPTIONS)
          .then((response) => {
            this.$set(this.loadingDistinctErrorProperties, property, false)
            this.$set(this.distinctErrorProperties, property, response.body[property])
          }, (error) => {
            this.$set(this.loadingDistinctErrorProperties, property, false)
            api.handleError(error)
          })
      },

      tip (entitiesName) {
        return '(use "%" for a generic text to match several ' + entitiesName +
               '; eg. "%foo%bar" for "contains \'foo\' somewhere in the middle, and then \'bar\' somewhere after \'foo\', ' +
               'with or without anything inbetween"; note: search will be slower with a % at or near the start)'
      }
    },

    mounted () {
      if (this.initialForm) {
        this.setForm(this.initialForm)
      }
    },

    watch: {
      initialForm () {
        this.setForm(this.initialForm ? this.initialForm : {})
      }
    }
  }
</script>

<style scoped>
  .ivu-col {
    padding: 0;
    line-height: 32px;
    vertical-align: middle;
  }
  .ivu-col button {
    margin-left: 8px;
  }
  .edit .ivu-row {
    margin-bottom: 6px !important;
  }
  span.disabled {
    color: lightgray;
  }
  .tip {
    color: gray;
    display: block;
    margin: 2px 8px 6px 8px;
    line-height: 1.2em;
  }
</style>
