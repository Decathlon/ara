<template>
  <div class="main-container">
    <div class="login-box">
      <div class="login-title">
        <img src="../assets/favicon.png" width="32" height="32"/>
        <span>Login to ARA</span>
      </div>
      <div class="signin-selection">
        <div class="signin-title"><span>Sign in with</span></div>
        <div class="authentication-buttons-container">
          <custom-authentication-button v-if="providers.custom.enabled" class="authentication-button"></custom-authentication-button>
          <google-authentication v-if="providers.google.enabled" class="authentication-button"></google-authentication>
          <github-authentication-button v-if="providers.github.enabled" class="authentication-button"></github-authentication-button>
        </div>
      </div>
    </div>
    <Spin fix v-if="authenticating"/>
  </div>
</template>

<script>
import GoogleAuthentication from '../components/authentication/google-authentication-button'
import GithubAuthenticationButton from '../components/authentication/github-authentication-button'
import CustomAuthenticationButton from '../components/authentication/custom-authentication-button'

import { AuthenticationService } from '../service/authentication.service'
import api from '../libs/api'
import Vue from 'vue'

export default {
  name: 'login',

  components: {
    CustomAuthenticationButton,
    GithubAuthenticationButton,
    GoogleAuthentication
  },

  data () {
    return {
      providers: this.$appConfig.authentication.providers,
      authenticating: false
    }
  },

  methods: {
    loginAs (user) {
      const tokenDetails = user.token
      const userDetails = user.user
      const authenticationDetails = {
        provider: user.provider,
        user: {
          id: userDetails.id,
          name: userDetails.name,
          login: userDetails.login,
          picture: userDetails.picture,
          email: userDetails.email
        },
        token: {
          id: tokenDetails.id,
          access: tokenDetails.accessToken,
          refresh: tokenDetails.refreshToken,
          expiration: {
            duration: tokenDetails.expirationDuration,
            timestamp: tokenDetails.expirationTimestamp
          },
          type: tokenDetails.type,
          scope: tokenDetails.scope
        }
      }

      AuthenticationService.login(authenticationDetails)
    },

    authenticate () {
      const providerName = this.$route.params.provider
      const provider = this.$appConfig.getProvider(providerName)
      const providerGivenIsUnknown = providerName && !provider
      if (providerGivenIsUnknown) {
        this.$Notice.open({
          title: 'Unknown OAuth2 provider',
          desc: `The provider <b>'${providerName}'</b> is not supported by ARA!`,
          duration: 0
        })
        this.backToLogin()
      }

      if (provider) {
        if (!provider.enabled) {
          this.$Notice.open({
            title: 'Authentication forbidden...',
            desc: `You cannot authenticate to <b>${provider.display}</b> because it is not enabled.<br>Check your configuration files if you want to enable it.`,
            duration: 0
          })
          this.backToLogin()
          return
        }
        const code = this.$route.query.code
        if (!code) {
          this.$Notice.open({
            title: 'Authentication code required...',
            desc: `You need a code to authenticate to <b>${provider.display}</b>`,
            duration: 0
          })
          this.backToLogin()
          return
        }
        const url = api.paths.authentication()
        const loginRequest = {
          code: code,
          provider: provider.name,
          clientId: provider.clientId
        }
        this.authenticating = true
        Vue.http
          .post(url, loginRequest, api.REQUEST_OPTIONS)
          .then(response => {
            this.authenticating = false
            const user = response.body
            this.loginAs(user)
          }, () => {
            this.authenticating = false
            this.$Notice.open({
              title: 'Login attempt failed...',
              desc: `You were not able to login to ARA through <b>${provider.display}</b>.<br>It may be linked to your configuration files.`,
              duration: 0
            })
            this.backToLogin()
          })
      }
    },

    backToLogin () {
      this.$router.push({ name: 'login' })
    }
  },

  mounted () {
    this.authenticate()
  }
}
</script>

<style>

.main-container {
  position: relative;
  border: darkgrey solid 1px;
  border-radius: 5px;
  background-color: #145c93;
  margin: 0 auto;
  width: 75%;
  height: 500px;
}

.login-box {
  background-color: white;
  position: absolute;
  width: 50%;
  margin: 0;
  top: 50%;
  left: 50%;
  -ms-transform: translate(-50%, -50%);
  transform: translate(-50%, -50%);
}

.login-title {
  width: 50%;
  text-align: center;
  font-weight: 300;
  color: #444;
  margin: 10px auto 0;
  padding: 20px 0 45px 0;
  font-size: 35px;
  line-height: 38px;
  text-transform: none;
  letter-spacing: 0;
}

.signin-selection {
  margin-bottom: 15px;
}

.signin-title {
  margin: 0 auto;
  width: 50%;
  text-align: center;
}

.signin-title span {
  color: #5b6987;
  display: -ms-grid;
  display: grid;
  font-size: 20px;
  width: 100%;
  line-height: 40px;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  text-align: center;
  -ms-grid-columns: minmax(20px,1fr) auto minmax(20px,1fr);
  grid-template-columns: minmax(20px,1fr) auto minmax(20px,1fr);
  grid-gap: 19px;
}

.signin-title span:before, .signin-title span:after {
  content: "";
  border-top: 1px solid #5b6987;
}

.authentication-buttons-container {
  margin: 0 auto;
  padding: 10px 0 10px;
  width: 50%;
}

.authentication-button {
  margin: 10px auto 0;
  width: 100%;

  color: #031b4e;
  background: #f2f8ff;
  border: 1px solid rgba(0, 105, 255, 0.2);
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  border-radius: 3px;
  display: inline-block;
  padding: 15px;
  text-align: center;
  position: inherit;
  font-size: 15px;
}

.authentication-button:hover {
  cursor: pointer;
  background: #dbecff;
}

.authentication-button span {
  margin-left: 5px;
}

</style>
