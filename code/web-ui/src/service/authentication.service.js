import router from '../main'
import Vue from 'vue'
import iView from 'iview'
import api from '../libs/api'

const AUTHENTICATION_DETAILS = 'authentication_details'

const AuthenticationService = {

  login (authenticationDetails) {
    this.saveDetails(authenticationDetails)
    router.push('/')
  },

  isAlreadyLoggedIn () {
    return !!this.getDetails()
  },

  logout () {
    this.clearDetails()
    router.push('/login')
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
