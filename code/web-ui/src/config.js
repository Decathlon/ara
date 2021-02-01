import _ from 'lodash'

const config = {
  authentication: {
    get enabled () {
      const atLeastOneProvidersEnabled = _(Object.values(this.providers)).some('enabled')
      return atLeastOneProvidersEnabled
    }
  },
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
