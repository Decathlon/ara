import Vue from 'vue'
import Vuex from 'vuex'

import projects from './projects'
import rootCauses from './root-causes'
import severities from './severities'
import teams from './teams'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    projects,
    rootCauses,
    severities,
    teams
  }
})
