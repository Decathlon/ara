import Vue from 'vue'
import api from '../libs/api'

export default {
  namespaced: true,

  state: {
    loading: false,
    loaded: false,
    projects: [],
    defaultProjectCode: undefined
  },

  mutations: {
    setLoading (state, loading) {
      state.loading = loading
    },

    setProjects (state, projects) {
      state.projects = projects

      // Compute the default project code: either the one set as default,
      // or the first one (or undefined if no projects exist)
      if (projects && projects.length) {
        state.defaultProjectCode = projects[0].code
        for (let project of projects) {
          if (project.defaultAtStartup) {
            state.defaultProjectCode = project.code
            break
          }
        }
      }

      // At the end, when everything is loaded/computed
      state.loaded = true
    }
  },

  actions: {
    ensureProjectsLoaded ({ state, commit }) {
      if (!this.loading && !this.loaded) {
        commit('setLoading', true)
        Vue.http
          .get(api.paths.projects(), api.REQUEST_OPTIONS)
          .then((response) => {
            commit('setProjects', response.body)
            commit('setLoading', false)
          }, (error) => {
            api.handleError(error)
            commit('setLoading', false)
          })
      }
    }
  }
}
