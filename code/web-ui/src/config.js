import _ from 'lodash'

const config = {
  authentication: {
    get enabled () {
      const atLeastOneProvidersEnabled = _(Object.values(this.providers)).some('enabled')
      return atLeastOneProvidersEnabled
    }
  },
  downloadError: false,
  get isComplete () {
    return this.authentication.providers
  },
  getProvider: function (providerName) {
    if (this.isComplete && providerName) {
      const providers = this.authentication.providers
      for (const providerKey in providers) {
        const provider = providers[providerKey]
        if (provider.name === providerName.toLowerCase()) {
          return provider
        }
      }
    }
  },
  getProviderUrls: function () {
    if (this.isComplete) {
      return _(this.authentication.providers).filter('enabled').map('uri').value()
    }
    return []
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
