export * from './state';

export class Category {
  name: string;
  alias: string[] | undefined;
  allParents: string[];
  directChildren: string[];
  min: number;
  desired: number;
  max: number;
  maxPerWeek: number;
  maxPerYear: number;
  directParent?: string | undefined;

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

export class Product {
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

export class TagState {
  name: string;
  state: string;
  count: number;

  constructor(name: string, state: string, count: number) {
    this.name = name;
    this.state = state;
    this.count = count;
  }
}

export class CategoryCount {
  category: Category;
  count: number;

  constructor(category: Category, count: number) {
    this.category = category;
    this.count = count;
  }
}
