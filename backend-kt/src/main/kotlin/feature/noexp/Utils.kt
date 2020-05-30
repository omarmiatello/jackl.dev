package com.github.omarmiatello.jackldev.feature.noexp

import com.github.omarmiatello.noexp.Category
import com.github.omarmiatello.noexp.Product
import com.github.omarmiatello.noexp.utils.toProduct
import java.util.concurrent.TimeUnit


// QR Generator

private fun Product.getNameFormatted() =
    listOfNotNull(name, description).joinToString().ifEmpty { barcode }

private fun Product.nameFormattedHtml() =
    "<a href='https://jackl.dev/home/$qr'>${expireFormatted()}</a> ${getNameFormatted()}"


private fun show(description: String, products: List<Product>) = buildString {
    appendln("${products.size} prodotti $description:")
    products.forEach {
        appendln(it.nameFormattedHtml())
    }
    appendln()
}

fun expireMessage() = buildString {
    val now = System.currentTimeMillis()
    val categoriesMap = mapOf<String, Category>().withDefault { Category(it) } // TODO use real categoriesMap
    val fullList = NoExpFireDB.home.values.map { it.toProduct(categoriesMap) }.sortedBy { it.expireDate }

    appendln("Ci sono ${fullList.size} prodotti")

    val expiredList = fullList.takeWhile { it.expireDate < now }
    if (expiredList.isNotEmpty()) appendln(show("scaduti", expiredList.take(5)))

    val expiredListNot = fullList.drop(expiredList.size)
    val weekEnd = now + TimeUnit.DAYS.toMillis(7)
    val weekList = expiredListNot.takeWhile { it.expireDate < weekEnd }
    if (weekList.isNotEmpty()) appendln(show("entro 7 giorni", weekList.take(10)))

    val weekListNot = expiredListNot.drop(weekList.size)
    val monthEnd = now + TimeUnit.DAYS.toMillis(30)
    val monthList = weekListNot.takeWhile { it.expireDate < monthEnd }
    if (monthList.isNotEmpty()) appendln(show("entro 30 giorni", monthList.take(10)))

    val monthListNot = weekListNot.drop(monthList.size)
    if (monthListNot.isNotEmpty()) appendln(show("oltre 1 mese", monthListNot.take(10)))
}