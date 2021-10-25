<template>
  <div class="main-container">
    <div class="login-box">
      <div class="login-title">
        <img src="../assets/favicon.png" width="32" height="32" alt="ARA logo"/>
        <span>Login to ARA</span>
      </div>
      <div class="signin-selection" v-if="configuration.isComplete">
        <div class="signin-title"><span>Sign in with</span></div>
        <div class="authentication-buttons-container">
          <authentication-button v-if="isEnabled('custom')" :provider="getProvider('custom')" class="authentication-button"></authentication-button>
          <authentication-button v-if="isEnabled('google')"  :provider="getProvider('google')" class="authentication-button"></authentication-button>
          <authentication-button v-if="isEnabled('github')" :provider="getProvider('github')" class="authentication-button"></authentication-button>
        </div>
      </div>
      <div v-else-if="configuration.downloadError" class="configuration-not-loaded info-box">
        Configuration not found, you can't login to ARA.
      </div>
    </div>
  </div>
</template>

<script>
import AuthenticationButton from '../components/authentication/authentication-button'

export default {
  name: 'login',

  components: {
    AuthenticationButton
  },

  data () {
    return {
      configuration: this.$appConfig
    }
  },

  methods: {
    isEnabled (providerName) {
      return this.getProvider(providerName) !== undefined
    },

    getProvider (providerName) {
      return this.configuration.authentication.providers[providerName]
    },

    tryAutoLogin () {
      const activeProviders = ['custom', 'google', 'github'].filter((providerName) => this.isEnabled(providerName))
      if (activeProviders.length === 1) {
        window.location.href = this.getProvider(activeProviders[0]).uri
      }
    }
  },

  mounted () {
    this.tryAutoLogin()
  }
}
</script>

<style>

.main-container {
  position: relative;
  border: darkgrey solid 1px;
  border-radius: 7px;
  background-color: #145c93;
  margin: 0 auto;
  width: 75%;
  height: 500px;
}

.login-box {
  background-color: white;
  position: absolute;
  border-radius: 5px;
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

.configuration-loading {
  color: #00529B;
  background-color: #BDE5F8;
  border-color: #00529B;
}

.configuration-not-loaded {
  color: #D8000C;
  background-color: #FFBABA;
  border-color: #D8000C;
}

.info-box {
  border: 1px solid;
  font-size: 16px;

  padding: 10px 0 10px 0;
  margin: 0 auto 15px;
  width: 75%;
  text-align: center;
}

</style>
