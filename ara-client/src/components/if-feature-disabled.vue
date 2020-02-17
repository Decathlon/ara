<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (C) 2019 by the ARA Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ 	 http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
<template>
  <div v-if="inactive">
    <slot></slot>
  </div>
</template>

<script>
  import Vue from 'vue'
  import api from '../libs/api'

  export default {
    name: 'if-feature-disabled',
    props: ['code'],
    data () {
      return {
        inactive: null
      }
    },
    created () {
      Vue.http
        .get(api.paths.features() + '/' + this.$props.code + '/state', api.REQUEST_OPTIONS)
        .then((response) => {
          this.inactive = !response.body.enabled
        }, (error) => {
          console.err(error)
          // Use the disabled state by default.
          this.inactive = true
        })
    }
  }
</script>
