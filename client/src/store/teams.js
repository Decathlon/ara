import Vue from 'vue'
import api from '../libs/api'

const getProjectCode = function (viewOrProjectCode) {
  if (typeof viewOrProjectCode === 'string') {
    return viewOrProjectCode
  } else {
    return viewOrProjectCode.$route.params.projectCode
  }
}

const getProject = function (state, projectCode) {
  if (state.projects[projectCode] === undefined) {
    Vue.set(state.projects, projectCode, {
      loading: false,
      loaded: false,
      teams: []
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
    teamsAssignableToProblems: state => viewOrProjectCode => {
      return getProject(state, getProjectCode(viewOrProjectCode)).teams.filter(team => team.assignableToProblems)
    },

    teamsAssignableToFunctionalities: state => viewOrProjectCode => {
      return getProject(state, getProjectCode(viewOrProjectCode)).teams.filter(team => team.assignableToFunctionalities)
    }
  },

  mutations: {
    setLoading (state, { projectCode, loading }) {
      getProject(state, projectCode).loading = loading
    },

    setTeams (state, { projectCode, teams }) {
      let project = getProject(state, projectCode)
      project.teams = teams
      project.loaded = true
    },

    unloadTeams (state, { projectCode }) {
      let project = getProject(state, projectCode)
      project.teams = []
      project.loaded = false
    }
  },

  actions: {
    ensureTeamsLoaded ({ state, commit }, viewOrProjectCode) {
      let projectCode = getProjectCode(viewOrProjectCode)
      if (projectCode) {
        let project = getProject(state, projectCode)
        if (!project.loading && !project.loaded) {
          commit('setLoading', { projectCode, loading: true })
          Vue.http
            .get(api.paths.teams(projectCode), api.REQUEST_OPTIONS)
            .then((response) => {
              commit('setTeams', { projectCode, teams: response.body })
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
