<template>
  <div>
    <!-- Progress-bar during deployment + testing, indicating estimated remaining time -->
    <span v-if="estimatedDurationTooltip" :title="estimatedDurationTooltip" style="float: right;">
      {{Math.trunc(currentPercentage)}} % ({{formattedRemainingDuration}})
    </span>
    <slot/>
    <!-- If percent=100, the "active" animated style is lost to a green fix style, which does not convey the "still running" state -->
    <Progress
      :stroke-width="small ? 5 : 10"
      :percent="currentPercentage === 100 ? 99.999 : currentPercentage"
      status="active"
      hide-info
      :style="'display: block; clear: both;' + (small ? ' line-height: 5px; padding-top: 4px;' : '')"/>
  </div>
</template>

<script>
  import moment from 'moment'
  import util from '../libs/util'

  export default {
    name: 'nrt-progress-bar',

    props: [ 'small', 'startDateTime', 'estimatedDuration' ],

    data () {
      return {
        interval: undefined,
        elapsedDuration: 0,
        currentPercentage: 0,
        formattedRemainingDuration: ''
      }
    },

    computed: {
      estimatedDurationTooltip () {
        if (this.estimatedDuration && this.estimatedDuration > 0) {
          return '' +
            util.prettyHoursMinutesFromMillisecondsDuration(this.elapsedDuration) +
            ' of estimated ' +
            util.prettyHoursMinutesFromMillisecondsDuration(this.estimatedDuration)
        }
        return ''
      }
    },

    methods: {
      updateProgressBar () {
        this.elapsedDuration = Math.max(0, moment().diff(moment(this.startDateTime)).valueOf())
        this.currentPercentage = Math.min(100, Math.trunc(this.elapsedDuration * 100 / this.estimatedDuration))

        let remainingDuration = this.estimatedDuration - this.elapsedDuration
        if (this.currentPercentage < 100) {
          this.formattedRemainingDuration = util.prettyHoursMinutesFromMillisecondsDuration(remainingDuration) + ' remaining'
        } else {
          this.formattedRemainingDuration = util.prettyHoursMinutesFromMillisecondsDuration(-remainingDuration) + ' ago'
        }
      }
    },

    mounted () {
      this.interval = setInterval(() => {
        this.updateProgressBar()
      }, 60000)
      this.updateProgressBar()
    },

    beforeDestroy () {
      clearInterval(this.interval)
      this.interval = null
    }
  }
</script>
