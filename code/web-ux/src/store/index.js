import { createStore, createLogger } from "vuex";
import cardsPosition from "./modules/cardsPositions";

const debug = process.env.NODE_ENV !== "production";

export default createStore({
  modules: {
    cardsPosition,
  },
  strict: debug,
  plugins: debug ? [createLogger()] : [],
});
