<template>
  <div @click="goToAuthenticationURI()">
    <em :class="'fa fa-' + provider.icon + ' fa-fw'"></em><span>{{provider.display}}</span>
  </div>
</template>

<script>
import { AuthenticationService } from '../../service/authentication.service'

export default {
  name: 'authentication-button',

  props: {
    provider: Object
  },

  methods: {
    goToAuthenticationURI () {
      AuthenticationService.saveProviderName(this.provider.display)
      window.location.href = this.provider.uri
    }
  }
}
</script>
