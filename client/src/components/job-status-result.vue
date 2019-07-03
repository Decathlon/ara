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
  <span>
    <span v-if="job.status" :class="job.status">({{job.status.toLowerCase()}})</span>
    <span v-if="job.result" :class="job.result">({{job.result.toLowerCase()}})</span>
    <em v-if="showDuration && job.status === 'DONE' && job.duration" style="color: lightgray;">
      - took {{util.prettyHoursMinutesFromMillisecondsDuration(job.duration)}}
    </em>
  </span>
</template>

<script>
  import util from '../libs/util'

  export default {
    name: 'job-status-result',

    props: [ 'job', 'showDuration' ],

    mixins: [{
      created () {
        this.util = util
      }
    }]
  }
</script>

<style scoped>
  /* JobStatus */
  .PENDING     { color: #FF9900; }
  .RUNNING     { color: #0082C3; }
  .DONE        { display: none; }
  .UNAVAILABLE { color: #ED3F14; }

  /* Result */
  .ABORTED   { color: #ED3F14; }
  .FAILURE   { color: #ED3F14; }
  .NOT_BUILT { color: #ED3F14; }
  .SUCCESS   { display: none; }
  .UNSTABLE  { display: none; }
</style>
