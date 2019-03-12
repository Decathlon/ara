<template>
  <div v-if="cycleStabilities" :class="problem.effectiveStatus === 'REAPPEARED' ? 'reapparead' : ''">
    <div v-for="(cycleStability, index) in cycleStabilities" :key="index" class="stability"
         :style="'width: ' + (cycleStability.executionStabilities.length * 7 + 2) + 'px;'">
      <div v-for="(executionStability, index2) in cycleStability.executionStabilities" :key="index2">
        <Tooltip :transfer="true" placement='bottom'>
          <router-link v-if="executionStability.executionId"
                       :to="{ name: 'execution', params: { id: executionStability.executionId }, query: { problem: problem.id }}"
                       :class="'stability-' + executionStability.status"></router-link>
          <div v-else :class="'stability-' + executionStability.status"></div>
          <div slot='content' style='text-align: center'>
            <p>{{formatDate(executionStability.testDate) + (executionStability.testDate ? " - " : "")
            + cycleStability.branchName + "/"
            + cycleStability.cycleName}}</p>
            <p v-if="executionStability.status == 'E'"><i>Problem appeared in this execution</i></p>
            <p v-else-if="executionStability.status == 'O'"><i>Problem did not appear in this execution</i></p>
          </div>
        </Tooltip>
      </div>
    </div>
  </div>
</template>

<script>
  import util from '../libs/util'

  export default {
    name: 'problemStability',
    props: [ 'cycleStabilities', 'problem' ],
    mixins: [
      {
        methods: util
      }
    ]
  }
</script>

<style lang="less" scoped>
  @size: 7px;
  @height: @size + 2px;

  .stability {
    border: 1px solid gray;
    height: @height;
    line-height: @size;
    border-radius: 2px;
    margin-top: 3px;
    margin-bottom: 3px;
  }

  .stability div,
  .stability a {
    display: inline-block;
    width: @size;
    height: @size;
    vertical-align: top;

  }

  .stability-- {
    background-color: #F5F7F9;
    &:hover {
      background-color: #FFFFFF;
      border-left: 1px solid gray;
      border-right: 1px solid gray;
    }
  }

  .stability-O {
    background-color: #F5F7F9;
    &:hover {
      background-color: #FFFFFF;
      border-left: 1px solid gray;
      border-right: 1px solid gray;
    }
  }

  .stability-E {
    background-color: #FF9900;
    &:hover {
      background-color: #FFB649;
      border-left: 1px solid gray;
      border-right: 1px solid gray;
    }
  }

  .reapparead .stability-E {
    background-color: #ED3F14;
    &:hover {
      background-color: #FF6C4B;
    }
  }
</style>
