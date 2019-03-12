<template>
  <Form :label-width="120">
    <Form-item label="For team:">
      <Select v-model="teamId" clearable filterable placeholder="None">
        <Option v-for="team in teamsAssignableToProblems" :value="team.id" :key="team.id" :label="team.name" />
      </Select>
    </Form-item>
    <Form-item label="Name:" :required="true">
      <Input ref="name" v-model="name" @on-enter="submit" />
    </Form-item>
    <Form-item label="Comment:">
      <Input v-model="comment" type="textarea" :autosize="{ minRows: 2, maxRows: 15 }" />
    </Form-item>
    <Form-item label="Work item id:">
      <Input v-model="defectId" @on-enter="submit" />
    </Form-item>
    <Form-item label="Root cause:" :required="isClosed" style="position: relative;">
      <Select v-model="rootCauseId" clearable placeholder="None">
        <Option v-for="rootCause in rootCauses" :value="rootCause.id" :key="rootCause.id" :label="rootCause.name" />
      </Select>
      <Spin fix v-if="!rootCauses"/>
    </Form-item>
    <Form-item v-if="okText" label="">
      <Button type="primary" @click="submit" :loading="loadingSubmit">{{okText}}</Button>
    </Form-item>
  </Form>
</template>

<script>
  export default {
    name: 'problem-properties-editor',

    props: [ 'okText', 'isClosed' ],

    data () {
      return {
        teamId: null,
        name: null,
        comment: null,
        defectId: null,
        rootCauseId: null,

        loadingSubmit: false
      }
    },

    computed: {
      teamsAssignableToProblems () {
        return this.$store.getters['teams/teamsAssignableToProblems'](this)
      },

      rootCauses () {
        return this.$store.getters['rootCauses/rootCauses'](this)
      }
    },

    methods: {
      doInit (form) {
        this.$store.dispatch('rootCauses/ensureRootCausesLoaded', this)
        this.teamId = form.teamId
        this.name = form.name
        this.comment = form.comment
        this.defectId = form.defectId
        this.rootCauseId = form.rootCauseId
      },

      focus () {
        this.$nextTick(() => this.$refs.name.focus())
      },

      submit () {
        if (!this.name) {
          this.$Modal.error({
            title: 'Error',
            content: 'You must provide a name for the problem.'
          })
        } else {
          let problem = {
            name: this.name,
            comment: this.comment,
            blamedTeam: (this.teamId ? {
              id: this.teamId
            } : null),
            defectId: this.defectId,
            rootCause: (this.rootCauseId ? {
              id: this.rootCauseId
            } : null)
          }
          let onStartCallback = () => {
            this.loadingSubmit = true
          }
          let onDoneCallback = () => {
            this.loadingSubmit = false
          }
          this.$emit('submit', problem, onStartCallback, onDoneCallback)
        }
      }
    }
  }
</script>
