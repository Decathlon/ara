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
import { AuthenticationService } from '../service/authentication.service'

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
        const userDefaultProject = AuthenticationService.getDetails().user.default_project
        state.defaultProjectCode = userDefaultProject || projects[0].code
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
