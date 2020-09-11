<template>
  <div class="container">
    <div class="content">
      Oh oh, it seems like this page does not exist...
      <router-link :to="'/'">Please take me back home</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'not-found'
}
</script>

<style scoped>
  .container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    height: 400px;
  }

  .content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    font-size: 35px;
    text-align: center;
    width: 75%;
    margin: 0 auto;
  }
</style>
