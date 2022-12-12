import Vue from 'vue'
import iView from 'iview'
import api from '../libs/api'
import RememberUrlService from './rememberurl.service'
import { config } from '../config.js'

const USER_DETAILS = 'user_details'
const PROVIDER_NAME = 'provider_name'

const conf = config

let isLogged = false

class AuthenticationServiceClass {
  setLogged = (logged) => {
    isLogged = logged
  }

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
          return Vue.http.get(api.paths.userDetails)
            .then(answer => answer.body)
            .then(userData => this.saveUserDetails(userData))
        }
      }).catch((e) => {
        isLogged = false
      })
  }

  getOauthProviders = async () => {
    return Vue.http.get(api.paths.authenticationConfiguration(), api.REQUEST_OPTIONS)
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
        console.debug(res)
        return res
      })
  }

  manageLoginRedirection = async (to, from, next) => {
    // RememberUrlService.redirectToLastWantedUrlWhenLoggin(from, to, next)
    RememberUrlService.keepLastUrl(to)

    const isPublic = to.matched.some(record => record.meta.public)
    const onlyWhenLoggedOut = to.matched.some(record => record.meta.onlyWhenLoggedOut)

    if (isPublic) {
      console.debug('url is public, continue')
      return next()
    }
    if (config.downloadError) {
      console.debug('downloadError is defined')
      return next('/login')
    }

    const loggedIn = await this.isAlreadyLoggedIn()
    const needToDownloadConfig = !(config.isComplete)
    console.debug(`loggedIn? ${loggedIn} / needToDownloadConfig? ${needToDownloadConfig}`)
    if (needToDownloadConfig) {
      try {
        config.authentication = await this.getOauthProviders()
        console.debug('Authent conf retrieved')
        config.downloadError = false
      } catch (err) {
        console.error(err)
        config.downloadError = true
        console.debug('error while retrieving providers')
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
      console.debug('access denied')
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
    this.clearDetails()
    // window.location.href = config.authentication.logoutProcessingUrl
    console.debug(`conf imported ? ${conf !== undefined}`)
    window.location.href = conf.authentication.logoutProcessingUrl
  }

  getDetails = () => {
    return {
      user: JSON.parse(localStorage.getItem(USER_DETAILS)),
      providerName: JSON.parse(localStorage.getItem(PROVIDER_NAME))
    }
  }

  saveUserDetails = (authenticationDetails) => {
    localStorage.setItem(USER_DETAILS, JSON.stringify(authenticationDetails))
  }

  saveProviderName = (providerName) => {
    localStorage.setItem(PROVIDER_NAME, JSON.stringify(providerName))
  }

  clearDetails = () => {
    localStorage.removeItem(USER_DETAILS)
  }
}

const AuthenticationService = new AuthenticationServiceClass()
export { AuthenticationService }
