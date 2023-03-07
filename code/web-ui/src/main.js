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
import VueCookies from 'vue-cookies'

import { sanitizeUrl } from '@braintree/sanitize-url'
import VueI18n from 'vue-i18n'

Vue.config.devtools = true
Vue.use(Vue2Filters)
Vue.use(VueResource)
Vue.use(VueRouter)
Vue.use(iView, { locale })
Vue.use(VueVirtualScroller)
Vue.use(configurationPlugin)
Vue.use(VueCookies)
Vue.use(VueI18n)
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
  routes: routes
})

const i18n = new VueI18n({
  locale: 'en',
  fallbackLocale: 'en',
  messages: {
    en: require('./locales/en.json')
  }
})

router.beforeEach(async (to, from, next) => {
  await AuthenticationService.manageLoginRedirection(to, from, next)

  iView.LoadingBar.start()
  util.title(to.meta.title)
  iView.Message.destroy()

  next()
})

router.afterEach(() => {
  iView.LoadingBar.finish()
  window.scrollTo(0, 0)
})

AuthenticationService.setLoggedOutInterceptor(router)

util.cleanFromPreviousVersion()

Vue.prototype.$sanitizeUrl = sanitizeUrl

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  i18n,
  beforeCreate () { this.$store.commit('initialiseStore') },
  render: h => h(app)
})

export default router
