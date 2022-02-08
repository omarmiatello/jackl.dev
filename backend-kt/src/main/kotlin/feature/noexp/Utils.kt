package com.github.omarmiatello.jackldev.feature.noexp

import com.github.omarmiatello.noexp.Category
import com.github.omarmiatello.noexp.ProductHome
import com.github.omarmiatello.noexp.ProductQr
import com.github.omarmiatello.noexp.utils.toProductQr
import java.util.concurrent.TimeUnit


// QR Generator

private fun ProductHome.getNameFormatted() =
    listOfNotNull(name).joinToString().ifEmpty { barcode }

private fun ProductQr.nameFormattedHtml() =
    "<a href='https://jackl.dev/home/$qr'>${expireFormatted()}</a> ${getNameFormatted()}"


private fun show(description: String, products: List<ProductQr>) = buildString {
    appendLine("${products.size} prodotti $description:")
    products.forEach {
        appendLine(it.nameFormattedHtml())
    }
    appendLine()
}

fun expireMessage() = buildString {
    val now = System.currentTimeMillis()
    val categoriesMap = mapOf<String, Category>().withDefault { Category(it) } // TODO use real categoriesMap
    val fullList = NoExpFireDB.home.values.map { it.toProductQr(categoriesMap) }.sortedBy { it.expireDate.valueIfRealOrEstimate ?: 0 }

    appendLine("Ci sono ${fullList.size} prodotti")

    val expiredList = fullList.takeWhile { (it.expireDate.valueIfRealOrEstimate ?: 0) < now }
    if (expiredList.isNotEmpty()) appendLine(show("scaduti", expiredList.take(5)))

    val expiredListNot = fullList.drop(expiredList.size)
    val weekEnd = now + TimeUnit.DAYS.toMillis(7)
    val weekList = expiredListNot.takeWhile { (it.expireDate.valueIfRealOrEstimate ?: 0) < weekEnd }
    if (weekList.isNotEmpty()) appendLine(show("entro 7 giorni", weekList.take(10)))

    val weekListNot = expiredListNot.drop(weekList.size)
    val monthEnd = now + TimeUnit.DAYS.toMillis(30)
    val monthList = weekListNot.takeWhile { (it.expireDate.valueIfRealOrEstimate ?: 0) < monthEnd }
    if (monthList.isNotEmpty()) appendLine(show("entro 30 giorni", monthList.take(10)))

    val monthListNot = weekListNot.drop(monthList.size)
    if (monthListNot.isNotEmpty()) appendLine(show("oltre 1 mese", monthListNot.take(10)))
}