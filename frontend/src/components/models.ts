class Category {
    name:           string;
    alias:          string[] | undefined;
    allParents:     string[] | undefined;
    directChildren: string[] | undefined;
    min:            number;
    desired:        number;
    max:            number;
    maxPerWeek:     number;
    maxPerYear:     number;
    directParent?:  string | undefined;

    constructor(name: string, alias: string[] | undefined, allParents: string[] | undefined, directChildren: string[] | undefined, min: number, desired: number, max: number, maxPerWeek: number, maxPerYear: number, directParent: string | undefined) {
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
    cat: string[] | undefined;
    catParents: string[] | undefined;

    constructor(barcode: string, expireDate: number, insertDate: number, name: string, pictureUrl: string, qr: string, description: string, cat: string[] | undefined, catParents: string[] | undefined) {
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