<script>
import Mgr from "../services/SecurityService";
import Api from "../services/ApiService";

export default {
  data: () => {
    return {
      mgr: new Mgr(),
      api: new Api(),
      userData: false,
      errorMessage: "",
      user: {
        username: "",
        password: "",
      },
    };
  },

  methods: {
    async getAll(api) {
      let self = this;
      let result = await this.api.getAll(api);
      self.logApi(result);
    },
    getToken() {
      let self = this;
      this.mgr.getUser().then(
        (token) => {
          self.logToken(token);
        },
        (err) => {
          console.log(err);
        }
      );
    },
    getTokenId() {
      let self = this;
      this.mgr.getIdToken().then(
        (tokenId) => {
          self.logToken(tokenId);
        },
        (err) => {
          console.log(err);
        }
      );
    },
    getTokenSessionState() {
      let self = this;
      this.mgr.getSessionState().then(
        (sessionState) => {
          self.logToken(sessionState);
        },
        (err) => {
          console.log(err);
        }
      );
    },
    getAccessToken() {
      let self = this;
      this.mgr.getAcessToken().then(
        (acessToken) => {
          self.logToken(acessToken);
        },
        (err) => {
          console.log(err);
        }
      );
    },
    getTokenScopes() {
      let self = this;
      this.mgr.getScopes().then(
        (scopes) => {
          self.logToken(scopes);
        },
        (err) => {
          console.log(err);
        }
      );
    },
    getTokenProfile() {
      let self = this;
      this.mgr.getProfile().then(
        (tokenProfile) => {
          self.logToken(tokenProfile);
        },
        (err) => {
          console.log(err);
        }
      );
    },
    renewToken() {
      let self = this;
      this.mgr.renewToken().then(
        (newToken) => {
          self.logToken(newToken);
        },
        (err) => {
          console.log(err);
        }
      );
    },
    logApi() {
      document.getElementById("resultsApi").innerText = "";

      Array.prototype.forEach.call(arguments, function (msg) {
        if (msg instanceof Error) {
          msg = "Error: " + msg.message;
        } else if (typeof msg !== "string") {
          msg = JSON.stringify(msg, null, 2);
        }
        document.getElementById("resultsApi").innerHTML += msg + "\r\n";
      });
    },
    logToken() {
      document.getElementById("resultsToken").innerText = "";

      Array.prototype.forEach.call(arguments, function (msg) {
        if (msg instanceof Error) {
          msg = "Error: " + msg.message;
        } else if (typeof msg !== "string") {
          msg = JSON.stringify(msg, null, 2);
        }
        document.getElementById("resultsToken").innerHTML += msg + "\r\n";
      });
    },
  },
};
</script>

<template>
  <div class="vtmn-h-screen vtmn-flex vtmn-items-center vtmn-justify-center">
    <div>
      <h1 class="vtmn-text-center vtmn-typo_title-1">
        Agile Regression Analyzer
      </h1>
      <div class="vtmn-mx-auto vtmn-px-4 vtmn-py-4 vtmn-my-auto">
        <div
          class="card bg-gray-50 md:bg-white vtmn-border-2 vtmn-border-solid vtmn-w-max vtmn-m-auto vtmn-rounded-3xl mb-6"
        >
          <div class="vtmn-py-1">
            <div class="vtmn-p-4">
              <h2
                class="vtmn-typo_title-2 vtmn-text-center vtmn-mb-8 vtmn-md:mt-4 vtmn-text-gray-800 vtmn-tracking-normal"
              >
                Sign In
              </h2>

              <form @submit.prevent="login">
                <div class="vtmn-mb-8 vtmn-w-max vtmn-m-auto">
                  <label class="vtmn-text-input_label" for="vtmn-input">
                    Username
                  </label>
                  <div class="vtmn-text-input_container">
                    <input
                      type="text"
                      class="vtmn-text-input"
                      placeholder="Type your username..."
                      v-model="user.username"
                      required
                    />
                    <span class="vtmx-user-line"></span>
                  </div>
                </div>

                <div class="vtmn-mb-6 vtmn-w-max vtmn-m-auto">
                  <label class="vtmn-text-input_label" for="vtmn-input">
                    Password
                  </label>
                  <div class="vtmn-text-input_container">
                    <input
                      type="password"
                      class="vtmn-text-input"
                      placeholder="Type your password..."
                      v-model="user.password"
                      required
                    />
                    <span class="vtmx-eye-off-line"></span>
                  </div>
                </div>
              </form>

              <p v-if="errorMessage">{{ errorMessage }}</p>
            </div>

            <div class="vtmn-mb-4 vtmn-w-max vtmn-m-auto">
              <div style="width: 400px; display: flex; justify-content: center">
                <button
                  class="vtmn-btn vtmn-btn_variant--primary vtmn-btn_size--medium"
                  @click="getAll('values')"
                >
                  Sign in
                </button>
              </div>
              <p class="vtmn-text-center vtmn-typo_text-3 vtmn-mt-2">
                You don't have and accout? Sign up!
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="userData"></div>
  </div>
</template>

<style scoped>
.vtmn-typo_title-1 {
  color: var(--vtmn-semantic-color_content-inactive) !important;
}

.vtmn-typo_title-1::before {
  content: "";
  display: inline-block;
  vertical-align: middle;
  background-image: url("../assets/img/logo.png");
  background-size: cover;
  width: 80px;
  height: 80px;
}

.card {
  border-color: var(--vtmn-semantic-color_border-secondary);
}

p {
  color: var(--vtmn-semantic-color_content-secondary);
}
</style>
