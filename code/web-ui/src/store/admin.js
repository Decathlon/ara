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
    savedSingleUserConnections: false,
    userRole: '',
    membersType: '',
    showSubMenuMembers: false,
    typeSelected: ''
  },

  mutations: {
    saveSingleUserConnections: (state, adminRight) => {
      state.savedSingleUserConnections = adminRight
    },

    saveUserRole: (state, userRole) => {
      state.userRole = userRole
    },

    initialiseStore (state) {
      state.savedSingleUserConnections = false
    },

    activeSubMenuMembers (state, memberType) {
      state.showSubMenuMembers = memberType
    },

    saveSelectedType (state, type) {
      state.typeSelected = type
    }
  },

  actions: {
    enableAdmin ({ state, commit }, payload) {
      if (!(state.savedSingleUserConnections) && payload === 'projects-list') {
        commit('saveSingleUserConnections', true)
        localStorage.setItem('adminRight', true)
      } else if (payload === 'dashboard') {
        commit('saveSingleUserConnections', false)
        localStorage.setItem('adminRight', false)
      }
    },

    showChoice ({ commit }, payload) {
      commit('activeMembersChoice', payload)
    },

    showSubMenuMembers ({ commit }, payload) {
      commit('activeSubMenuMembers', payload)
    },

    setTypeSelected ({ commit }, type) {
      commit('saveSelectedType', type)
    },

    setRole ({ commit }, userInfo) {
      commit('saveUserRole', userInfo)
    }
  }
})
