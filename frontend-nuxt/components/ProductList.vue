<template>
  <div class="hello">
    <h3>Prodotti</h3>

    <el-select v-model="tagFilter" placeholder="Select" filterable clearable>
      <el-option v-for="item in categoriesVisible" :key="item.category.name" :label="item.category.name"
                 :value="item.category.name"/>
    </el-select>

    <el-container>
      <el-main>
        <el-tag v-for="tag in pageTagStates" :key="tag.name" :type="tag.state" size="small"
                v-on:click="onSelectTag(tag.name)">
          <el-tag size="mini" :type="tag.state" effect="dark">{{tag.count}}</el-tag>
          {{tag.name}}
        </el-tag>
      </el-main>
    </el-container>

    <el-table :data="productsFiltered" style="width: 100%">
      <el-table-column prop="qr" label="QR" width="50"/>
      <el-table-column prop="expireDate" label="Scade" width="70">
        <template slot-scope="scope">
          {{formatProductDate(scope.row.expireDate)}}
        </template>
      </el-table-column>
      <!--            <el-table-column prop="pictureUrl" width="100" label="Img">-->
      <!--                <template slot-scope="scope">-->
      <!--                    <el-avatar :size="80" shape="square" :src="scope.row.pictureUrl"></el-avatar>-->
      <!--                </template>-->
      <!--            </el-table-column>-->
      <el-table-column prop="name" label="Prodotto">
        <template slot-scope="scope">
          {{scope.row.name}}
          <el-tag v-for="tag in filterTagsByRemove(scope.row.cat, tagFilter)" :key="tag.name"
                  :type="tag.state" size="mini" v-on:click="onSelectTag(tag.name)">
            {{tag.name}}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
  .el-tag + .el-tag {
    margin-left: 8px;
    margin-bottom: 8px;
  }

  .item {
    margin-top: 10px;
    margin-right: 40px;
  }
</style>
<script lang="ts">

  import {CategoryCount, Product, TagState} from "~/utils/types";

  import Vue, { PropOptions } from 'vue'

  export default Vue.extend({

  })


</script>
