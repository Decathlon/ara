<template>
  <div @click="goToGoogleOAuthPage()">
    <i :class="'fa fa-' + provider.icon + ' fa-fw'"></i><span>{{provider.display}}</span>
  </div>
</template>

<script>

export default {
  name: 'google-authentication',

  data () {
    return {
      provider: this.$appConfig.authentication.providers.google
    }
  },

  methods: {
    goToGoogleOAuthPage () {
      window.location.href = this.provider.uri
    }

  }
}
</script>

<style>

</style>
