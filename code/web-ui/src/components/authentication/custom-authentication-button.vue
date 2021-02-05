<template>
  <div @click="goToAuthenticationURI()">
    <em :class="'fa fa-' + provider.icon + ' fa-fw'"></em><span>{{provider.display}}</span>
  </div>
</template>

<script>

export default {
  name: 'custom-authentication-button',

  data () {
    return {
      provider: this.$appConfig.authentication.providers.custom
    }
  },

  methods: {
    goToAuthenticationURI () {
      window.location.href = this.provider.uri
    }
  }
}
</script>
