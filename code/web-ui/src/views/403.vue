<template>
  <div class="container">
    <div class="content">
      You don't have the rights to access this page!
      <router-link :to="'/admin/projects-list'">Go back to projects list</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: '403'
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
