import Vue from 'vue'
import api from '../libs/api'

const getProjectCode = function (view) {
  return view.$route.params.projectCode
}

const getProject = function (state, projectCode) {
  if (state.projects[projectCode] === undefined) {
    Vue.set(state.projects, projectCode, {
      loading: false,
      loaded: false,
      rootCauses: null
    })
  }
  return state.projects[projectCode]
}

export default {
  namespaced: true,

  state: {
    projects: {}
  },

  getters: {
    rootCauses: state => view => {
      return getProject(state, getProjectCode(view)).rootCauses
    }
  },

  mutations: {
    setLoading (state, { projectCode, loading }) {
      getProject(state, projectCode).loading = loading
    },

    setRootCauses (state, { projectCode, rootCauses }) {
      let project = getProject(state, projectCode)
      project.rootCauses = rootCauses
      project.loaded = true
    }
  },

  actions: {
    ensureRootCausesLoaded ({ state, commit }, view) {
      let projectCode = getProjectCode(view)
      if (projectCode) {
        let project = getProject(state, projectCode)
        if (!project.loading && !project.loaded) {
          commit('setLoading', { projectCode, loading: true })
          Vue.http
            .get(api.paths.rootCauses(projectCode), api.REQUEST_OPTIONS)
            .then((response) => {
              commit('setRootCauses', { projectCode, rootCauses: response.body })
              commit('setLoading', { projectCode, loading: false })
            }, (error) => {
              api.handleError(error)
              commit('setLoading', { projectCode, loading: false })
            })
        }
      }
    }
  }
}
