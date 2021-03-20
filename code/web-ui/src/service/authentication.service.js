import router from '../main'
import Vue from 'vue'
import iView from 'iview'
import api from '../libs/api'
import { config } from '../config'

const AUTHENTICATION_DETAILS = 'authentication_details'

const LAST_URL_BEFORE_LOGOUT = 'last_url'

const AuthenticationService = {

  login (authenticationDetails) {
    this.saveDetails(authenticationDetails)
    const url = this.getLastUrlBeforeLogout()
    router.push(url)
  },

  isAlreadyLoggedIn () {
    return !!this.getDetails()
  },

  logout (manual) {
    this.saveUrl()
    this.clearDetails()
    const providersUrls = config.getProviderUrls()
    if (!manual && providersUrls.length === 1) {
      window.location.href = providersUrls[0]
      return
    }
    router.push('/login')
  },

  saveUrl () {
    const currentUrl = window.location.pathname
    localStorage.setItem(LAST_URL_BEFORE_LOGOUT, currentUrl)
  },

  getLastUrlBeforeLogout () {
    const url = localStorage.getItem(LAST_URL_BEFORE_LOGOUT)
    return url || '/'
  },

  deleteAuthenticationCookie () {
    const url = api.paths.logout()
    Vue.http
      .post(url)
      .then(
        () => {},
        () => {
          iView.Notice.open({
            title: 'You were not logged out properly',
            desc: 'An error occurred while logging out',
            duration: 0
          })
        })
  },

  getDetails () {
    const stringifiedDetails = localStorage.getItem(AUTHENTICATION_DETAILS)
    return JSON.parse(stringifiedDetails)
  },

  saveDetails (authenticationDetails) {
    const stringifiedDetails = JSON.stringify(authenticationDetails)
    localStorage.setItem(AUTHENTICATION_DETAILS, stringifiedDetails)
  },

  clearDetails () {
    localStorage.removeItem(AUTHENTICATION_DETAILS)
    this.deleteAuthenticationCookie()
  }

}

export { AuthenticationService }
