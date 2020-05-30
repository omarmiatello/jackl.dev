<template>
  <div class="home">
    <HomeProducts />
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
