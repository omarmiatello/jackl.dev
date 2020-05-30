import {Category, Product} from "~/types";

export interface RootState {
  categories: Category[];
  products: Product[];
}
