<template>
    <div class="hello">
        <h3>Prodotti</h3>

        <el-select v-model="categoryFilter" placeholder="Select" filterable clearable>
            <el-option v-for="item in categories" :key="item.name" :label="item.name" :value="item.name"/>
        </el-select>

        <el-container>
            <el-main>
                <el-tag v-for="tag in pageTags" :key="tag.name" :type="tag.state" size="small"
                        v-on:click="onSelectTag(tag.name)">{{tag.name}}
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
                    <el-tag v-for="tag in filterTagsByRemove(scope.row.cat, categoryFilter)" :key="tag.name"
                            :type="tag.state" size="mini" v-on:click="onSelectTag(tag.name)">
                        {{tag.name}}
                    </el-tag>
                </template>
            </el-table-column>
        </el-table>

    </div>
</template>

<script lang="ts">
    import {Component, Prop, Vue} from "vue-property-decorator"
    import axios from "axios"
    import "./models"

    class Tag {
        name: string;
        state: string;

        constructor(name: string, state: string) {
            this.name = name;
            this.state = state;
        }
    }

    @Component
    export default class HomeProducts extends Vue {
        @Prop() private msg!: string;
        private products!: Product[];
        private categories!: Category[];
        private categoryFilter!: string;

        data() {
            return {
                products: [],
                categories: [],
                categoryFilter: ''
            }
        }

        mounted() {
            axios.get("https://noexp-for-home.firebaseio.com/home.json").then(response => {
                console.log(response);
                const list: Product[] = this.valuesFrom(response.data);
                this.products = list.sort((a, b) => a.expireDate - b.expireDate);
            })
            axios.get("https://noexp-for-home.firebaseio.com/category.json").then(response => {
                console.log(response);
                const list: Category[] = this.valuesFrom(response.data);
                this.categories = list;
            })
            // axios.get("/categories.json").then(response => {
            //     console.log(response);
            //     this.categories = response.data;
            // })
        }

        private findCategory(categoryName: string): Category | undefined {
            return this.categories.find(c => c.name === categoryName);
        }

        private toTags(categories: string[], state: string): Tag[] {
            return categories
                .map(value => this.categories.find(c => c.name === value)!)
                .filter(value => value != undefined).map(value => new Tag(value.name, state));
        }

        private valuesFrom(map: any) {
            const list = []
            for (const k in map) {
                list.push(map[k])
            }
            return list;
        }

        get productsFiltered(): Product[] {
            if (this.categoryFilter == undefined || this.categoryFilter == "") {
                return this.products
            } else {
                return this.products.filter(product => {
                    const cat = product.cat || ["Alimenti"]
                    const catParents = product.catParents || []
                    return cat.find(p => p === this.categoryFilter) != null || catParents.find(p => p === this.categoryFilter) != null
                });
            }
        }

        get pageTags(): Tag[] {
            const categoryName = this.categoryFilter || '';
            const category = this.findCategory(categoryName);
            if (categoryName == undefined || categoryName == "" || category == undefined) {
                return this.categories.filter(category => (category.allParents || []).length == 0).map(value => new Tag(value.name, ""));
            } else {
                return this.toTags(category.allParents || [], "info")
                    .concat(this.toTags([category.name], "success"))
                    .concat(this.toTags(category.directChildren || [], ""));
            }
        }


        onSelectTag(tag: string) {
            if (this.categoryFilter != tag) {
                this.categoryFilter = tag
            } else {
                this.categoryFilter = this.findCategory(tag)?.directParent || ''
            }
        }

        formatProductDate(expireDate: number): string {
            const now = new Date().getTime()
            const h = Math.round((expireDate - now) / (1000 * 60 * 60))
            if (Math.abs(h) < 36) return h + "h"
            const d = Math.round(h / 24)
            if (Math.abs(d) < 45) return d + "d"
            const m = Math.round(d / 30)
            if (Math.abs(m) < 13) return m + "M"
            return Math.round(m / 12) + "y"
        }

        filterTagsByRemove(tags: string[], tagName: string): Tag[] {
            return tags
                .map((value, index) => new Tag(value, index == 0 ? "" : "info"))
                .filter(value => value.name != tagName)
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .el-tag + .el-tag {
        margin-left: 8px;
        margin-bottom: 8px;
    }
</style>
