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

<script lang="ts">
    import {Component, Prop, Vue} from "vue-property-decorator"
    import axios from "axios"

    class Category {
        name:           string;
        alias:          string[] | undefined;
        allParents:     string[];
        directChildren: string[];
        min:            number;
        desired:        number;
        max:            number;
        maxPerWeek:     number;
        maxPerYear:     number;
        directParent?:  string | undefined;

        constructor(name: string, alias: string[] | undefined, allParents: string[], directChildren: string[], min: number, desired: number, max: number, maxPerWeek: number, maxPerYear: number, directParent: string | undefined) {
            this.name = name;
            this.alias = alias;
            this.allParents = allParents;
            this.directChildren = directChildren;
            this.min = min;
            this.desired = desired;
            this.max = max;
            this.maxPerWeek = maxPerWeek;
            this.maxPerYear = maxPerYear;
            this.directParent = directParent;
        }
    }

    class Product {
        barcode?: string | undefined;
        expireDate: number;
        insertDate: number;
        name: string;
        pictureUrl?: string | undefined;
        qr: string;
        description?: string | undefined;
        cat: string[];
        catParents: string[];

        constructor(barcode: string | undefined, expireDate: number, insertDate: number, name: string, pictureUrl: string | undefined, qr: string, description: string | undefined, cat: string[], catParents: string[]) {
            this.barcode = barcode;
            this.expireDate = expireDate;
            this.insertDate = insertDate;
            this.name = name;
            this.pictureUrl = pictureUrl;
            this.qr = qr;
            this.description = description;
            this.cat = cat;
            this.catParents = catParents;
        }
    }

    class TagState {
        name: string;
        state: string;
        count: number;

        constructor(name: string, state: string, count: number) {
            this.name = name;
            this.state = state;
            this.count = count;
        }
    }

    class CategoryCount {
        category: Category;
        count: number;

        constructor(category: Category, count: number) {
            this.category = category;
            this.count = count;
        }
    }

    @Component
    export default class HomeProducts extends Vue {
        @Prop() private msg!: string;
        private products!: Product[];
        private categories!: Category[];
        private tagFilter!: string;

        data() {
            return {
                products: [],
                categories: [],
                tagFilter: ''
            }
        }

        mounted() {
            axios.get("https://noexp-for-home.firebaseio.com/home.json").then(response => {
                console.log(response);
                const list: Product[] = this.valuesFrom(response.data);
                this.products = list.sort((a, b) => a.expireDate - b.expireDate).map(p => new Product(
                    p.barcode, p.expireDate, p.insertDate, p.name, p.pictureUrl, p.qr, p.description, p.cat || ["Alimenti"], p.catParents || []
                ));
            })
            axios.get("https://noexp-for-home.firebaseio.com/category.json").then(response => {
                console.log(response);
                const list: Category[] = this.valuesFrom(response.data);
                this.categories = list.map(c => new Category(
                    c.name, c.alias, c.allParents || [],
                    c.directChildren || [],
                    c.min, c.desired, c.max, c.maxPerWeek, c.maxPerYear, c.directParent
                ));
            })
        }

        private findCategory(categoryName: string): Category | undefined {
            return this.categoriesVisible.find(c => c.category.name === categoryName)?.category;
        }

        private toTags(categories: string[], state: string): TagState[] {
            return categories
                .map(value => this.categoriesVisible.find(c => c.category.name === value)!)
                .filter(value => value != undefined)
                .map(value => new TagState(value.category.name, state, value.count));
        }

        private valuesFrom(map: any) {
            const list = []
            for (const k in map) {
                list.push(map[k])
            }
            return list;
        }

        get productsFiltered(): Product[] {
            if (this.tagFilter == '' || this.categories.length == 0) {
                return this.products
            } else {
                const categoriesMap = new Map(this.categories.map(c => [c.name, c]))
                return this.products.filter(product => {
                    const cat = categoriesMap.get(product.cat[0])!;
                    return cat.name === this.tagFilter || cat.allParents.find(p => p === this.tagFilter) != null
                });
            }
        }

        get categoriesVisible(): CategoryCount[] {
            const products = this.products
            const categoriesMap = new Map(this.categories.map(c => [c.name, c]))
            return this.categories
                .map(category => {
                    const p = products.filter(product => {
                        const cat = categoriesMap.get(product.cat[0])!;
                        return cat.name === category.name || cat.allParents.find(p => p === category.name) != null
                    })
                    return new CategoryCount(category, p.length)
                })
                .filter(value => value.count > 0)
        }

        get pageTagStates(): TagState[] {
            const categoryName = this.tagFilter;
            const category = this.findCategory(categoryName);
            if (categoryName == undefined || categoryName == "" || category == undefined) {
                return this.categoriesVisible.filter(c => (c.category.allParents).length == 0).map(value => new TagState(value.category.name, "", value.count));
            } else {
                return this.toTags(category.allParents, "info")
                    .concat(this.toTags([category.name], "success"))
                    .concat(this.toTags(category.directChildren, ""));
            }
        }


        onSelectTag(tag: string) {
            if (this.tagFilter != tag) {
                this.tagFilter = tag
            } else {
                this.tagFilter = this.findCategory(tag)?.directParent || ''
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

        filterTagsByRemove(tags: string[], tagName: string): TagState[] {
            return tags
                .map((value, index) => new TagState(value, index == 0 ? "" : "info", 0))
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

    .item {
        margin-top: 10px;
        margin-right: 40px;
    }
</style>
