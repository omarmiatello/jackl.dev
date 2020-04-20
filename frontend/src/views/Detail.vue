<template>
  <div class="home">
    <img alt="Vue logo" src="../assets/logo.png" />
    <HomeProducts msg="Welcome to Your Vue.js App" />
  </div>
</template>

<script>
// @ is an alias to /src
import HomeProducts from "@/components/HomeProducts";

export default {
  name: "Home",
  components: {
    HomeProducts
  }
};
</script>
