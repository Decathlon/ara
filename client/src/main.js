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

Vue.use(Vue2Filters)
Vue.use(VueResource)
Vue.use(VueRouter)
Vue.use(iView, { locale })
Vue.use(VueVirtualScroller)

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
  routes
})

router.beforeEach((to, from, next) => {
  iView.LoadingBar.start()
  util.title(to.meta.title)
  iView.Message.destroy()
  next()
})

router.afterEach((to, from, next) => {
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
