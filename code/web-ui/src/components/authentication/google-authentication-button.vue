<template>
  <div @click="login()">
    <i :class="'fa fa-' + provider.icon + ' fa-fw'"></i><span>{{provider.display}}</span>
  </div>
</template>

<script>
import { AuthenticationService } from '../../service/authentication.service.js'

export default {
  name: 'google-authentication',

  data () {
    return {
      provider: this.$appConfig.authentication.providers.google,
      googleAuth: undefined
    }
  },

  methods: {
    initGoogleAuthentication () {
      window.gapi.load('client:auth2', () => {
        const scope = 'email profile openid'
        const that = this
        window.gapi.client.init({
          'clientId': this.provider.clientId,
          'scope': scope
        }).then(function () {
          that.googleAuth = window.gapi.auth2.getAuthInstance()

          that.googleAuth.isSignedIn.listen(that.updateSigninStatus)
        })
      })
    },

    login () {
      const userIsAlreadySignedIn = this.googleAuth.isSignedIn.get()
      if (userIsAlreadySignedIn) {
        const currentUser = this.googleAuth.currentUser.get()

        const title = 'Already connected to a Google account'
        const userProfile = currentUser.getBasicProfile()
        const email = userProfile.getEmail()
        const name = userProfile.getName()
        const message = `You are already logged in to a Google account <b>${email}</b> as <b>${name}</b>.<br>Keep this connexion or sign in with another account ?`
        this.$Modal.confirm({
          title: title,
          content: message,
          okText: `Login to ARA as ${email}`,
          onOk: () => {
            this.loginAs(currentUser)
          },
          cancelText: 'Sign in with another account',
          onCancel: () => {
            this.openSigninPage()
          },
          width: 700
        })
      } else {
        this.openSigninPage()
      }
    },

    openSigninPage () {
      this.googleAuth
        .signIn()
        .then(googleUser => {
          this.loginAs(googleUser)
        })
        .catch(error => {
          const errorCode = error.error
          const errorMessage = `You are not connected because an error occurred while signing in to your Google account: <b>${errorCode}</b>`
          this.$Message.error({
            content: errorMessage,
            duration: 10,
            closable: true
          })
        })
    },

    loginAs (googleUser) {
      const googleUserProfile = googleUser.getBasicProfile()
      const googleAuthenticationResponse = googleUser.getAuthResponse()
      const authenticationDetails = {
        provider: this.provider.name,
        user: {
          id: googleUser.getId(),
          name: googleUserProfile.getName(),
          login: googleUserProfile.getName(),
          picture: googleUserProfile.getImageUrl(),
          email: googleUserProfile.getEmail()
        },
        token: {
          id: googleAuthenticationResponse.id_token,
          access: googleAuthenticationResponse.access_token,
          expiration: {
            duration: googleAuthenticationResponse.expires_in,
            timestamp: googleAuthenticationResponse.expires_at
          },
          type: googleAuthenticationResponse.token_type,
          scope: googleAuthenticationResponse.scope
        }
      }

      AuthenticationService.login(authenticationDetails)
    },

    updateSigninStatus () {
      const authenticationDetails = AuthenticationService.getDetails()
      const hasBeenLoggedInToARAWithAGoogleAccount = AuthenticationService.isAlreadyLoggedIn() && authenticationDetails && authenticationDetails.provider === this.provider.name
      const isNoLongerLoggedInToGoogle = !this.googleAuth.isSignedIn.get()
      if (hasBeenLoggedInToARAWithAGoogleAccount && isNoLongerLoggedInToGoogle) {
        this.$Notice.open({
          title: 'You\'ve been disconnected from ARA',
          desc: 'You need to login again to ARA because you are no longer connected to your Google account.',
          duration: 0
        })
        AuthenticationService.logout()
      }
    }

  },

  mounted () {
    this.initGoogleAuthentication()
  }
}
</script>

<style>

</style>
