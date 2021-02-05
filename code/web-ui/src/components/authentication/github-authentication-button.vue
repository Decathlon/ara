<template>
  <div @click="goToGithubOAuthPage()">
    <em :class="'fa fa-' + provider.icon + ' fa-fw'"></em><span>{{provider.display}}</span>
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
