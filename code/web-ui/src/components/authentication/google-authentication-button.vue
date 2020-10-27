<template>
  <div>
    <Button @click="login()">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18" aria-hidden="true"><title>Google</title><g fill="none" fill-rule="evenodd"><path fill="#4285F4" d="M17.64 9.2045c0-.6381-.0573-1.2518-.1636-1.8409H9v3.4814h4.8436c-.2086 1.125-.8427 2.0782-1.7959 2.7164v2.2581h2.9087c1.7018-1.5668 2.6836-3.874 2.6836-6.615z"></path><path fill="#34A853" d="M9 18c2.43 0 4.4673-.806 5.9564-2.1805l-2.9087-2.2581c-.8059.54-1.8368.859-3.0477.859-2.344 0-4.3282-1.5831-5.036-3.7104H.9574v2.3318C2.4382 15.9832 5.4818 18 9 18z"></path><path fill="#FBBC05" d="M3.964 10.71c-.18-.54-.2822-1.1168-.2822-1.71s.1023-1.17.2823-1.71V4.9582H.9573A8.9965 8.9965 0 0 0 0 9c0 1.4523.3477 2.8268.9573 4.0418L3.964 10.71z"></path><path fill="#EA4335" d="M9 3.5795c1.3214 0 2.5077.4541 3.4405 1.346l2.5813-2.5814C13.4632.8918 11.426 0 9 0 5.4818 0 2.4382 2.0168.9573 4.9582L3.964 7.29C4.6718 5.1627 6.6559 3.5795 9 3.5795z"></path></g></svg>
      Google
    </Button>
  </div>
</template>

<script>
import { AuthenticationService } from '../../service/authentication.service.js'

export default {
  name: 'google-authentication',

  data () {
    return {
      provider: 'google',
      googleAuth: undefined
    }
  },

  methods: {
    initGoogleAuthentication () {
      window.gapi.load('client:auth2', () => {
        const scope = 'email profile openid'
        const that = this
        window.gapi.client.init({
          'clientId': 'YOUR_CLIENT_ID',
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
        provider: this.provider,
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
      const hasBeenLoggedInToARAWithAGoogleAccount = AuthenticationService.isAlreadyLoggedIn() && authenticationDetails && authenticationDetails.provider === this.provider
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
