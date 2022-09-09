// initial state
const state = () => ({
  conditionsPositions: [],
});

//getters

// actions
const actions = {
  checkout({ commit }, items) {
    commit("setCartItems", { items: items });
  },

  // ADD REMOVE METHOD
};

// mutations
const mutations = {
  setCartItems(state, { items }) {
    state.conditionsPositions[items.position - 1] = items;
  },
};

export default {
  namespaced: true,
  state,
  actions,
  mutations,
};
