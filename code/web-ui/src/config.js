const config = {
  authentication: {
    providers: {
      google: {
        name: 'google',
        display: 'Google',
        icon: 'google',
        clientId: process.env.GOOGLE_CLIENT_ID
      },
      github: {
        name: 'github',
        display: 'Github',
        icon: 'github',
        clientId: process.env.GITHUB_CLIENT_ID,
        uri: 'https://github.com/login/oauth/authorize?client_id=' + process.env.GITHUB_CLIENT_ID
      },
      custom: {
        name: 'custom',
        display: process.env.CUSTOM_OAUTH_PROVIDER_NAME,
        icon: 'building',
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
