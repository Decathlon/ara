import router from '../main'

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
  }

}

export { AuthenticationService }
