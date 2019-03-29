<template>
  <div v-if="active">
    <slot></slot>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  export default {
    name: 'if-feature-enabled',
    props: ['code'],
    data () {
      return {
        active: null
      }
    },
    created () {
      Vue.http
           .get(api.paths.features() + '/' + this.$props.code + '/state', api.REQUEST_OPTIONS)
                .then((response) => {
                  this.active = response.body.enabled
                }, (error) => {
                  console.err(error)
                  // Disable the feature if an error occurs : no risks.
                  this.active = false
                })
    }
  }
</script>
