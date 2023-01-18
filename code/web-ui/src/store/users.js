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
    users: [],
    userRole: '',
    user: []
  },

  mutations: {
    storeAllUsers: (state, users) => {
      for (let user of users) {
        state.users.push(user.memberName)
      }
    },

    setUserRole: (state, userRole) => {
      state.userRole = userRole
    },

    setUserInfo: (state, user) => {
      state.user = user
    }
  },

  actions: {
    getAllUsers ({ commit }, payload) {
      commit('storeAllUsers', payload)
    },

    getUserRole ({ commit }, payload) {
      commit('setUserRole', payload)
    },

    getUserInfo ({ commit }, payload) {
      commit('setUserInfo', payload)
    }
  }
})
