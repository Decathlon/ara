import _ from 'lodash'

function parseBoolean (booleanAsString) {
  return booleanAsString === 'true'
}

const config = {
  authentication: {
    get enabled () {
      const someProvidersEnabled = _(Object.values(this.providers)).some('enabled')
      return parseBoolean(process.env.AUTHENTICATION_ENABLED) && someProvidersEnabled
    },
    providers: {
      google: {
        name: 'google',
        display: 'Google',
        icon: 'google',
        enabled: parseBoolean(process.env.GOOGLE_ENABLED),
        clientId: process.env.GOOGLE_CLIENT_ID,
        redirectUri: process.env.GOOGLE_REDIRECT_URI,
        get uri () {
          const baseUrl = 'https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount'
          const responseTypeParameter = 'response_type=code'
          const flowNameParameter = `flowName=GeneralOAuthFlow`
          const scopeParameter = 'scope=https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email'
          const clientIdParameter = `client_id=${process.env.GOOGLE_CLIENT_ID}`
          const redirectUriParameter = `redirect_uri=${process.env.GOOGLE_REDIRECT_URI}`
          const finalUrl = `${baseUrl}?${clientIdParameter}&${responseTypeParameter}&${redirectUriParameter}&${scopeParameter}&${flowNameParameter}`
          return finalUrl
        }
      },
      github: {
        name: 'github',
        display: 'Github',
        icon: 'github',
        enabled: parseBoolean(process.env.GITHUB_ENABLED),
        clientId: process.env.GITHUB_CLIENT_ID,
        uri: 'https://github.com/login/oauth/authorize?client_id=' + process.env.GITHUB_CLIENT_ID
      },
      custom: {
        name: 'custom',
        display: process.env.CUSTOM_OAUTH_PROVIDER_NAME || 'Custom',
        icon: 'building',
        enabled: parseBoolean(process.env.CUSTOM_ENABLED),
        clientId: process.env.CUSTOM_OAUTH_PROVIDER_CLIENT_ID,
        uri: process.env.CUSTOM_OAUTH_PROVIDER_URI
      }
    }
  },
  getProvider: function (providerName) {
    if (providerName) {
      const providers = this.authentication.providers
      for (const providerKey in providers) {
        const provider = providers[providerKey]
        if (provider.name === providerName.toLowerCase()) {
          return provider
        }
      }
    }
  }
}

export {
  config
}
export default {
  install (Vue) {
    Vue.appConfig = config
    Vue.prototype.$appConfig = config
  }
}
