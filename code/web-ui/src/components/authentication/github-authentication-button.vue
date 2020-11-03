<template>
  <div @click="goToGithubOAuthPage()">
    <i class="fa fa-github fa-fw"></i><span>Github</span>
  </div>
</template>

<script>
import Vue from 'vue'
import api from '../../libs/api'
import { AuthenticationService } from '../../service/authentication.service'

export default {
  name: 'github-authentication-button',

  data () {
    return {
      provider: 'github',
      clientId: 'YOUR_CLIENT_ID'
    }
  },

  methods: {
    goToGithubOAuthPage () {
      window.location.href = 'https://github.com/login/oauth/authorize?client_id=' + this.clientId
    },

    loginAs (githubUser) {
      const tokenDetails = githubUser.token
      const userDetails = githubUser.user
      const authenticationDetails = {
        provider: this.provider,
        user: {
          id: userDetails.id,
          login: userDetails.login,
          picture: userDetails.picture,
          email: userDetails.email
        },
        token: {
          access: tokenDetails.accessToken,
          type: tokenDetails.type,
          scope: tokenDetails.scope
        }
      }

      AuthenticationService.login(authenticationDetails)
    }
  },

  mounted () {
    const code = this.$route.query.code
    if (code) {
      const url = api.paths.authentication()
      const loginRequest = {
        code: code,
        clientId: this.clientId,
        provider: this.provider
      }
      Vue.http
        .post(url, loginRequest, api.REQUEST_OPTIONS)
        .then(response => {
          const githubUser = response.body
          this.loginAs(githubUser)
        }, (error) => {
          api.handleError(error)
        })
    }
  }
}
</script>

<style>

</style>
