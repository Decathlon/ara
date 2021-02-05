<template>
  <div @click="goToGoogleOAuthPage()">
    <em :class="'fa fa-' + provider.icon + ' fa-fw'"></em><span>{{provider.display}}</span>
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
