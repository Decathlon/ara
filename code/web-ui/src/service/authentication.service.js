import Vue from 'vue'
import iView from 'iview'
import api from '../libs/api'
import RememberUrlService from './rememberurl.service'
import { config } from '../config.js'

const CURRENT_USER = 'current_user'
const PROVIDER_NAME = 'provider_name'

const conf = config

let isLogged = false

class AuthenticationServiceClass {
  setLoggedOutInterceptor = (router) => {
    Vue.http.interceptors.push((request, next) => {
      next((response) => {
        if (response.url !== api.paths.loggedStatus && response.status === 401) {
          router.push({ name: 'login' })
        }
      })
    })
  }

  isAlreadyLoggedIn = async () => {
    return new Promise((resolve) => {
      if (isLogged) {
        resolve(true)
      } else {
        this.updateAuthenticationStatus().then(() => {
          resolve(isLogged)
        })
      }
    })
  }

  updateAuthenticationStatus = () => {
    return Vue.http.get(api.paths.loggedStatus)
      .then(answer => answer.body)
      .then(body => {
        return body === true
      })
      .then(logged => {
        isLogged = logged
        if (logged) {
          return Vue.http.get(api.paths.currentUser)
            .then(answer => answer.body)
            .then(userData => this.saveCurrentUser(userData))
        }
      }).catch((e) => {
        isLogged = false
      })
  }

  refreshUser = async function () {
    const self = this
    let refreshedUser
    await Vue.http.get(api.paths.currentUser)
      .then(response => response.body)
      .then(user => {
        self.saveCurrentUser(user)
        refreshedUser = user
      })
    return refreshedUser
  }

  getOauthProviders = async () => {
    return Vue.http.get(api.paths.authenticationConfiguration, api.REQUEST_OPTIONS)
      .then(response => response.body)
      .then(content => {
        const res = {
          loginStartingUrl: content.loginUrl,
          logoutProcessingUrl: content.logoutUrl,
          providers: content.providers.reduce((previous, current) => {
            previous[current.type] = {
              uri: `${content.loginUrl}/${current.name}`,
              display: current.displayValue,
              icon: current.type === 'custom' ? 'building' : current.type,
              name: current.name
            }
            return previous
          }, {})
        }
        return res
      })
  }

  manageLoginRedirection = async (to, from, next) => {
    RememberUrlService.keepLastUrl(to)

    const isPublic = to.matched.some(record => record.meta.public)
    const onlyWhenLoggedOut = to.matched.some(record => record.meta.onlyWhenLoggedOut)

    if (isPublic) {
      return next()
    }
    if (config.downloadError) {
      return next('/login')
    }

    const loggedIn = await this.isAlreadyLoggedIn()
    const needToDownloadConfig = !(config.isComplete)
    if (needToDownloadConfig) {
      try {
        config.authentication = await this.getOauthProviders()
        config.downloadError = false
      } catch (err) {
        config.downloadError = true
        return next('/login')
      }
    }

    const requireLogin = !loggedIn && config.authentication.enabled
    if (!loggedIn) {
      localStorage.setItem('adminRight', false)
      iView.Notice.open({
        title: 'Access denied',
        desc: 'You need to login first if you want to access this page.'
      })
      return next('/login')
    }

    const loggedInButTryingToReachALoggedOutPage = loggedIn && onlyWhenLoggedOut
    if (loggedInButTryingToReachALoggedOutPage) {
      iView.Notice.open({
        title: 'You are already connected!',
        desc: 'Logged out pages (such as the login page) can\'t be viewed if you are already connected. Please logout from ARA first.',
        duration: 0
      })
      return next('/')
    }

    const loginNotRequiredButTryingToAccessLogin = !requireLogin && onlyWhenLoggedOut
    if (loginNotRequiredButTryingToAccessLogin) {
      iView.Notice.open({
        title: 'Login not required!',
        desc: 'You cannot access the login page because the authentication is not enabled.<br>If you want to enable it, please check your configuration files.',
        duration: 0
      })
      return next('/')
    }
  }

  logout = () => {
    this.clearCurrentUser()
    window.location.href = conf.authentication.logoutProcessingUrl
  }

  getDetails = () => {
    return {
      user: JSON.parse(localStorage.getItem(CURRENT_USER)),
      providerName: JSON.parse(localStorage.getItem(PROVIDER_NAME))
    }
  }

  saveCurrentUser = (user) => {
    localStorage.setItem(CURRENT_USER, JSON.stringify(user))
  }

  saveProviderName = (providerName) => {
    localStorage.setItem(PROVIDER_NAME, JSON.stringify(providerName))
  }

  clearCurrentUser = () => {
    localStorage.removeItem(CURRENT_USER)
  }
}

const AuthenticationService = new AuthenticationServiceClass()
export { AuthenticationService }
