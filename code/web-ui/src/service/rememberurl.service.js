
const LAST_ROUTEPATH_KEY = 'LAST_ASKED_ROUTE'

const RememberUrlService = {

  keepLastUrl (to) {
    // We dont register the url in the following cases:
    // login/logout is asked
    // we're already going to the last saved url
    // the / is asked whereas we're coming fron a server redirection
    if (to.name === 'login') {
      return
    }
    if (to.name === 'logout') {
      return
    }
    const lastRoute = localStorage.getItem(LAST_ROUTEPATH_KEY)
    if (to.path === lastRoute) {
      return
    }
    if (this.redirectAsked()) {
      return
    }

    // in all other cases, we just register the current asked route
    localStorage.setItem(LAST_ROUTEPATH_KEY, to.path)
  },

  resetRedirectAsked () {
    window.history.replaceState({}, document.title, window.location.pathname)
  },

  redirectAsked (resetRedirection = false) {
    const res = new URLSearchParams(window.location.search).get('spring_redirect') === 'true'
    if (resetRedirection) {
      this.resetRedirectAsked()
    }
    return res
  },

  // We dont need to redirect when the target
  // is already the asked url
  redirectNeeded (redirectPath) {
    return redirectPath !== localStorage.getItem(LAST_ROUTEPATH_KEY)
  },

  getRedirectPath () {
    return localStorage.getItem(LAST_ROUTEPATH_KEY)
  }

}

export default RememberUrlService
