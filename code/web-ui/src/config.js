const config = {
  authentication: {
    google: {
      clientId: process.env.GOOGLE_CLIENT_ID
    },
    github: {
      clientId: process.env.GITHUB_CLIENT_ID
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
