<template>
  <div>
    <div v-if="run.comment" class="comment">
      {{run.comment}}
    </div>

    <Row type="flex" :gutter="4" justify="space-around">
      <i-col span="4" style="padding-top: 6px;">
        <a :style="'display: block; color: #657180;' + (haveQualities ? '' : ' cursor: text;')" v-on:click="open = !open">
          <Icon type="md-arrow-dropright" size="16" v-if="haveQualities" :class="open ? 'arrow open' : 'arrow'" style="width: 4px; position: relative; left: -5px;" />
          <strong>{{run.country.code.toUpperCase()}}:</strong>
          {{run.type.name}}
          <div :style="'line-height: 10px; margin-top: -4px; margin-left: ' + (haveQualities ? '9px' : '1px') + ';'">
            <em style="font-size: 10px; color: #9ea7b4;">{{run.platform}}</em>
          </div>
        </a>
      </i-col>
      <i-col :span="(24 - 4)" v-if="!haveQualities" style="padding: 6px 0 13px 18px;">
        <div v-for="countryDeployment in execution.countryDeployments" :key="countryDeployment.id" v-if="countryDeployment.country.code === run.country.code">
          <div v-if="countryDeployment.status !== 'DONE' || countryDeployment.result === 'ABORTED' || countryDeployment.result === 'FAILURE' || countryDeployment.result === 'NOT_BUILT'">

            <!-- Deployment pending... -->
            <span v-if="countryDeployment.status === 'PENDING'">
              Deployment on <strong>{{countryDeployment.platform}}</strong>:
              <job-status-result :job="countryDeployment"/>
            </span>

            <!-- Deployment running... -->
            <span v-else-if="countryDeployment.status === 'RUNNING'">
              <nrt-progress-bar :small="true" :estimatedDuration="countryDeployment.estimatedDuration" :startDateTime="countryDeployment.startDateTime" style="padding-right: 17px;">
                Deploying on <strong>{{countryDeployment.platform}}</strong>...
                <a v-if="countryDeployment.jobUrl" :href="countryDeployment.jobUrl + 'console'" target="_blank">
                  <Button icon="md-open" size="small" style="margin-left: 8px;">SHOW LOGS</Button>
                </a>
              </nrt-progress-bar>
            </span>

            <!-- Deployment failed... -->
            <span v-else style="color: #ED3F14;">
              The country <strong>{{countryDeployment.country.displayName}}</strong> did not deploy on <strong>{{countryDeployment.platform}}</strong>
              <job-status-result :job="countryDeployment"/>
              <a v-if="countryDeployment.jobUrl" :href="countryDeployment.jobUrl + 'console'" target="_blank">
                <Button icon="md-open" size="small" style="margin-left: 8px;">SHOW LOGS</Button>
              </a>
            </span>

          </div>
          <div v-else>

            <!-- Run pending... -->
            <span v-if="run.status === 'PENDING'">
              <job-status-result :job="run"/>
            </span>

            <!-- Run running... -->
            <span v-else-if="run.status === 'RUNNING'">
              <nrt-progress-bar :small="true" :estimatedDuration="run.estimatedDuration" :startDateTime="run.startDateTime" style="padding-right: 17px;">
                Testing...
                <a v-if="run.jobUrl" :href="run.jobUrl + 'console'" target="_blank">
                  <Button icon="md-open" size="small" style="margin-left: 8px;">SHOW LOGS</Button>
                </a>
              </nrt-progress-bar>
            </span>

            <!-- Run failed... -->
            <span v-else style="color: #ED3F14;">
              No result (test failed to launch, initialize or report)
              <a v-if="run.jobUrl" :href="run.jobUrl + 'console'" target="_blank">
                <Button icon="md-open" size="small" style="margin-left: 8px;">SHOW LOGS</Button>
              </a>
            </span>

          </div>
        </div>
      </i-col>
      <i-col :span="(24 - 4) / execution.qualitySeverities.length" v-else v-for="qualitySeverity in execution.qualitySeverities" :key="qualitySeverity.severity.code">
        <nrt-progress
          :execution="execution"
          :run="run"
          :counts="run.qualitiesPerSeverity[qualitySeverity.severity.code]"
          :severityCode="qualitySeverity.severity.code"
          :routerReplace="routerReplace" />
      </i-col>
    </Row>
    <transition name="maxHeight">
      <div v-if="haveQualities && open">
        <div style="margin: 8px 0;"> <!-- In sub-div to avoid jump during transition animation  -->
          <nrt-team
            :execution="execution"
            :run="run"
            :team="{ id: -404, name: '(No team)' }"
            :routerReplace="routerReplace" />
          <nrt-team
            v-for="team in teamsAssignableToProblems"
            :key="team.id"
            :execution="execution"
            :run="run"
            :team="team"
            :routerReplace="routerReplace" />
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
  import jobStatusResultComponent from './job-status-result'
  import nrtProgress from './nrt-progress'
  import nrtTeam from './nrt-team'
  import nrtProgressBar from './nrt-progress-bar'

  export default {
    props: [ 'execution', 'run', 'routerReplace' ],

    components: {
      'job-status-result': jobStatusResultComponent,
      'nrt-progress': nrtProgress,
      'nrt-team': nrtTeam,
      'nrt-progress-bar': nrtProgressBar
    },

    data () {
      return {
        open: false
      }
    },

    computed: {
      teamsAssignableToProblems () {
        return this.$store.getters['teams/teamsAssignableToProblems'](this)
      },

      haveQualities () {
        return this.run.qualitiesPerSeverity &&
          this.run.qualitiesPerSeverity['*'] &&
          this.run.qualitiesPerSeverity['*'].total
      }
    }
  }
</script>

<style lang="less" scoped>
  .comment {
    margin-top: 4px;
    padding: 4px 0 2px 0;
    border-top: 1px solid #E3E8EE;
    color: gray;
    font-style: italic;
    font-size: 90%;
  }

  .arrow {
    transition: transform .333s ease;
    transform-origin: left center;
  }
  .arrow.open {
    transform: rotate(90deg);
  }

  .maxHeight-enter-active,
  .maxHeight-leave-active {
    transition: max-height .333s ease;
  }
  .maxHeight-enter,
  .maxHeight-leave-to {
    max-height: 0;
    overflow: hidden;
  }
  .maxHeight-enter-to,
  .maxHeight-leave {
    max-height: 600px; /* height: auto; cannot work with overflow: hidden; */
    overflow: hidden;
  }
</style>
