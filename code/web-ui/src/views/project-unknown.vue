<template>
  <div class="container">
    <div class="content">
      The project you are trying to reach don't seems to exist...
      <router-link :to="'/admin/projects-list'">Go to projects list</router-link>
    </div>
  </div>
</template>
  
<script>
  export default {
    name: 'project-unknown'
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
  