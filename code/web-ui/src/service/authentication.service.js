import Vue from 'vue'
import api from '../libs/api'

const USER_DETAILS = 'user_details'
const PROVIDER_NAME = 'provider_name'

const LAST_URL_BEFORE_LOGOUT = 'last_url'

let isLogged = false

const AuthenticationService = {

  async isAlreadyLoggedIn () {
    return new Promise((resolve) => {
      if (isLogged) {
        resolve(true)
      } else {
        this.updateAuthenticationStatus().then(() => {
          resolve(isLogged)
        })
      }
    })
  },

  updateAuthenticationStatus () {
    return Vue.http.get(api.paths.loggedStatus)
      // .then(answer => answer.status)
      // .then(status => status !== 401)
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
  },

  logout (manual) {
    this.saveUrl()
    this.clearDetails()
    window.location.href = '/logout'
  },

  saveUrl () {
    const currentUrl = window.location.pathname
    localStorage.setItem(LAST_URL_BEFORE_LOGOUT, currentUrl)
  },

  getLastUrlBeforeLogout () {
    const url = localStorage.getItem(LAST_URL_BEFORE_LOGOUT)
    return url || '/'
  },

  getDetails () {
    return {
      user: JSON.parse(localStorage.getItem(USER_DETAILS)),
      providerName: JSON.parse(localStorage.getItem(PROVIDER_NAME))
    }
  },

  saveUserDetails (authenticationDetails) {
    localStorage.setItem(USER_DETAILS, JSON.stringify(authenticationDetails))
  },

  saveProviderName (providerName) {
    localStorage.setItem(PROVIDER_NAME, JSON.stringify(providerName))
  },

  clearDetails () {
    localStorage.removeItem(USER_DETAILS)
  }

}

export { AuthenticationService }
