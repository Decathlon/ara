<template>
  <div class="main-container">
    <div class="login-box">
      <div class="login-title">
        <img src="../assets/favicon.png" width="32" height="32"/>
        <span>Login to ARA</span>
      </div>
      <div class="signin-selection">
        <div class="signin-title"><span>Sign in with</span></div>
        <div class="authentication-buttons-container">
          <google-authentication class="authentication-button"></google-authentication>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import GoogleAuthentication from '../components/authentication/google-authentication-button'

export default {
  name: 'login',

  components: {
    GoogleAuthentication
  }
}
</script>

<style>

.main-container {
  position: relative;
  border: darkgrey solid 1px;
  border-radius: 5px;
  background-color: #145c93;
  margin: 0 auto;
  width: 75%;
  height: 500px;
}

.login-box {
  background-color: white;
  position: absolute;
  width: 50%;
  margin: 0;
  top: 50%;
  left: 50%;
  -ms-transform: translate(-50%, -50%);
  transform: translate(-50%, -50%);
}

.login-title {
  width: 50%;
  text-align: center;
  font-weight: 300;
  color: #444;
  margin: 10px auto 0;
  padding: 20px 0 45px 0;
  font-size: 35px;
  line-height: 38px;
  text-transform: none;
  letter-spacing: 0;
}

.signin-selection {
  margin-bottom: 15px;
}

.signin-title {
  margin: 0 auto;
  width: 50%;
  text-align: center;
}

.signin-title span {
  color: #5b6987;
  display: -ms-grid;
  display: grid;
  font-size: 16px;
  width: 100%;
  line-height: 40px;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  text-align: center;
  -ms-grid-columns: minmax(20px,1fr) auto minmax(20px,1fr);
  grid-template-columns: minmax(20px,1fr) auto minmax(20px,1fr);
  grid-gap: 19px;
}

.signin-title span:before, .signin-title span:after {
  content: "";
  border-top: 1px solid #5b6987;
}

.authentication-buttons-container {
  margin: 0 auto;
  padding: 10px 0 10px;
  width: 50%;
}

.authentication-button {
  margin: 0 auto;
  width: 35%;
}

</style>
