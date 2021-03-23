/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/
import Vue from 'vue'
import Vue2Filters from 'vue2-filters'
import VueResource from 'vue-resource'
import VueRouter from 'vue-router'
import iView from 'iview'

import routes from './routes'
import store from './store'
import util from './libs/util'
import app from './app.vue'

import locale from 'iview/dist/locale/en-US'
import 'iview/dist/styles/iview.css'

import VueVirtualScroller from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import { AuthenticationService } from './service/authentication.service'
import configurationPlugin from '@/config'
import { config } from './config'
import api from './libs/api'
import VueCookies from 'vue-cookies'

Vue.use(Vue2Filters)
Vue.use(VueResource)
Vue.use(VueRouter)
Vue.use(iView, { locale })
Vue.use(VueVirtualScroller)
Vue.use(configurationPlugin)
Vue.use(VueCookies)

Vue.http.interceptors.push(function (request, next) {
  next(function (response) {
    const status = response.status
    const accessDenied = status === 401 || status === 403
    const noLongerConnected = accessDenied && AuthenticationService.isAlreadyLoggedIn()
    if (noLongerConnected) {
      iView.Notice.open({
        title: 'You were logged out from ARA...',
        desc: 'It seems that your session has expired. Please login again to ARA',
        duration: 0
      })
      AuthenticationService.logout(false)
    }
  })
})

iView.LoadingBar.config({
  color: '#FFEA28', // Yellowish
  height: 3
})

iView.Message.config({
  // We won't make message disappear: always put { closable: true } in messages (this is not a default configuration option)
  duration: 60 * 60 * 24 * 365,
  top: 60 - 8 // header's height - margin
})

const router = new VueRouter({
  mode: 'history',
  routes: routes
})

const downloadConfig = async function () {
  return Vue.http.get(api.paths.authenticationConfiguration(), api.REQUEST_OPTIONS)
}

const manageLoginRedirection = async function (to, from, next) {
  const isPublic = to.matched.some(record => record.meta.public)
  const onlyWhenLoggedOut = to.matched.some(record => record.meta.onlyWhenLoggedOut)
  const goingToLoginPage = to.name === 'login'

  if (config.downloadError) {
    if (goingToLoginPage) {
      return next()
    }
    return next('login')
  }

  const loggedIn = AuthenticationService.isAlreadyLoggedIn()
  const needToDownloadConfig = !(loggedIn || config.isComplete)
  if (needToDownloadConfig) {
    try {
      const response = await downloadConfig()
      config.authentication.providers = response.body.providers
      config.downloadError = false
    } catch (err) {
      config.downloadError = true
      return next('login')
    }
  }
  const requireLogin = !loggedIn && config.authentication.enabled

  const canAccess = isPublic || loggedIn || !requireLogin
  if (!canAccess) {
    iView.Notice.open({
      title: 'Access denied',
      desc: 'You need to login first if you want to access this page.'
    })
    return goToLogin(next)
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

const goToLogin = function (next) {
  const providersUrls = config.getProviderUrls()
  const onlyOneProvider = providersUrls.length === 1
  if (onlyOneProvider) {
    window.location.href = providersUrls[0]
  }
  return next({
    path: '/login'
  })
}

router.beforeEach(async (to, from, next) => {
  await manageLoginRedirection(to, from, next)

  iView.LoadingBar.start()
  util.title(to.meta.title)
  iView.Message.destroy()

  next()
})

router.afterEach(() => {
  iView.LoadingBar.finish()
  window.scrollTo(0, 0)
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render: h => h(app)
})

export default router
