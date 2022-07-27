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
export default ({
  namespaced: true,

  state: {
    savedSingleUserConnections: false
  },

  mutations: {
    saveSingleUserConnections: (state, adminRight) => {
      state.savedSingleUserConnections = adminRight
    },

    initialiseStore (state) {
      if (localStorage.getItem('adminRight')) {
        state.savedSingleUserConnections = true
      }
    }
  },

  actions: {
    enableAdmin ({ state, commit }, payload) {
      if (!this.savedSingleUserConnections && payload === 'active-admin') {
        commit('saveSingleUserConnections', true)
        localStorage.setItem('adminRight', true)
      } else if (payload === 'dashboard') {
        commit('saveSingleUserConnections', false)
        localStorage.setItem('adminRight', false)
      }
    }
  }
})
