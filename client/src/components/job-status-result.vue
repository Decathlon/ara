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
