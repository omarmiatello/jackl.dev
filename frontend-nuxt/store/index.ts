import { MutationTree, ActionTree, ActionContext } from "vuex";
import { Context as AppContext } from "@nuxt/types";
import { RootState, Category, Product } from "~/types";

export const state = (): RootState => ({
  categories: [],
  products: []
})

export const mutations: MutationTree<RootState> = {
  setCategories(state: RootState, categories: Category[]): void {
    state.categories = categories
  },
  setProducts(state: RootState, products: Product[]): void {
    state.products = products
  }
}

interface Actions<S, R> extends ActionTree<S, R> {
  nuxtServerInit (actionContext: ActionContext<S, R>, appContext: AppContext): void
}

// axios.get("https://noexp-for-home.firebaseio.com/home.json").then(response => {
//   console.log(response);
//   const list: Product[] = this.valuesFrom(response.data);
//   this.products = list.sort((a, b) => a.expireDate - b.expireDate).map(p => new Product(
//     p.barcode, p.expireDate, p.insertDate, p.name, p.pictureUrl, p.qr, p.description, p.cat || ["Alimenti"], p.catParents || []
//   ));
// })
// axios.get("https://noexp-for-home.firebaseio.com/category.json").then(response => {
//   console.log(response);
//   const list: Category[] = this.valuesFrom(response.data);
//   this.categories = list.map(c => new Category(
//     c.name, c.alias, c.allParents || [],
//     c.directChildren || [],
//     c.min, c.desired, c.max, c.maxPerWeek, c.maxPerYear, c.directParent
//   ));
// })

export const actions: Actions<RootState, RootState> = {
  async nuxtServerInit({ commit }, context) {
    let categories: Category[] = await context.app.$axios.$get("https://noexp-for-home.firebaseio.com/category.json")
    commit("setCategories", categories)
    let products: Product[] = await context.app.$axios.$get("https://noexp-for-home.firebaseio.com/home.json")
    commit("setProducts", products)
  }
}
