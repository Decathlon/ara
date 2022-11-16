import { defineStore } from "pinia"

interface State {
  conditionsPositions: Array<string>
}

export const useCardsPositionsStore = defineStore('cardsPositions', {
  state: (): State => ({
    conditionsPositions: []
  }),

  actions: {
    setCartItems(items) {
      this.conditionsPositions[items.position - 1] = items;
    },

    // easily reset state using `$reset`
    clearUser () {
      this.$reset()
    }
  }
})
