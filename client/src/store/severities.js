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
import api from '../libs/api'

const getProjectCode = function (view) {
  return view.$route.params.projectCode
}

const getProject = function (state, projectCode) {
  if (state.projects[projectCode] === undefined) {
    Vue.set(state.projects, projectCode, {
      loading: false,
      loaded: false,
      severities: []
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
    severities: state => view => {
      return getProject(state, getProjectCode(view)).severities
    }
  },

  mutations: {
    setLoading (state, { projectCode, loading }) {
      getProject(state, projectCode).loading = loading
    },

    setSeverities (state, { projectCode, severities }) {
      let project = getProject(state, projectCode)
      project.severities = severities
      project.loaded = true
    }
  },

  actions: {
    ensureSeveritiesLoaded ({ state, commit }, view) {
      let projectCode = getProjectCode(view)
      if (projectCode) {
        let project = getProject(state, projectCode)
        if (!project.loading && !project.loaded) {
          commit('setLoading', { projectCode, loading: true })
          Vue.http
            .get(api.paths.severities(projectCode), api.REQUEST_OPTIONS)
            .then((response) => {
              commit('setSeverities', { projectCode, severities: response.body })
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
