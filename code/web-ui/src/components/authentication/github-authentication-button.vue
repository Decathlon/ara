<template>
  <div @click="goToGithubOAuthPage()">
    <i :class="'fa fa-' + provider.icon + ' fa-fw'"></i><span>{{provider.display}}</span>
  </div>
</template>

<script>

export default {
  name: 'github-authentication-button',

  data () {
    return {
      provider: this.$appConfig.authentication.providers.github
    }
  },

  methods: {
    goToGithubOAuthPage () {
      window.location.href = this.provider.uri
    }
  }
}
</script>

<style>

</style>
