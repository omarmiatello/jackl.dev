import {Category, CategoryCount, TagState} from "~/utils/types";

export function valuesFrom(map: any) {
  const list = []
  for (const k in map) {
    list.push(map[k])
  }
  return list;
}

export function findCategory(categoriesVisible: CategoryCount[], categoryName: string): Category | undefined {
  return categoriesVisible.find(c => c.category.name === categoryName)?.category;
}

export function toTags(categoriesVisible: CategoryCount[], categories: string[], state: string): TagState[] {
  return categories
    .map(value => categoriesVisible.find(c => c.category.name === value)!)
    .filter(value => value != undefined)
    .map(value => new TagState(value.category.name, state, value.count));
}

export function formatProductDate(expireDate: number): string {
  const now = new Date().getTime()
  const h = Math.round((expireDate - now) / (1000 * 60 * 60))
  if (Math.abs(h) < 36) return h + "h"
  const d = Math.round(h / 24)
  if (Math.abs(d) < 45) return d + "d"
  const m = Math.round(d / 30)
  if (Math.abs(m) < 13) return m + "M"
  return Math.round(m / 12) + "y"
}

export function filterTagsByRemove(tags: string[], tagName: string): TagState[] {
  return tags
    .map((value, index) => new TagState(value, index == 0 ? "" : "info", 0))
    .filter(value => value.name != tagName)
}
