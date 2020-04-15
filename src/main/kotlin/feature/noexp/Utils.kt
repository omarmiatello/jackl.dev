package com.github.omarmiatello.jackldev.feature.noexp

import feature.noexp.NoExpDB
import feature.noexp.Product
import java.util.concurrent.TimeUnit


// QR Generator

private const val URL_PREFIX = "https://jackl.dev/home/"

fun String.toQrLastPartOrNull() = takeIf { startsWith(URL_PREFIX) }?.removePrefix(URL_PREFIX)

private val baseN = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

fun Int.toBase36(): String {
    val max = baseN.length
    return if (this < max) {
        "${baseN[this % max]}"
    } else {
        "${((this / max) - 1).toBase36()}${baseN[this % max]}"
    }
}

fun String.base36ToInt(): Int {
    val max = baseN.length
    var num = 0
    toUpperCase().reversed().forEachIndexed { index, c ->
        num += (baseN.indexOf(c).takeIf { it != -1 }!! + 1) * (Math.pow(
            max.toDouble(),
            index.toDouble()
        ).toInt())
    }
    return num - 1
}



fun expireMessage(): String {
    fun List<Product>.show(maxProd: Int, desc: String) = if (isEmpty()) "" else """$size prodotti $desc:
${take(maxProd).joinToString("\n")}

"""

    val now = System.currentTimeMillis()
    val fullList = NoExpDB.home.values.sortedBy { it.expireDate }
    val expiredList = fullList.takeWhile { it.expireDate < now }
    val expiredListNot = fullList.drop(expiredList.size)
    val weekEnd = now + TimeUnit.DAYS.toMillis(7)
    val weekList = expiredListNot.takeWhile { it.expireDate < weekEnd }
    val weekListNot = expiredListNot.drop(weekList.size)
    val monthEnd = now + TimeUnit.DAYS.toMillis(30)
    val monthList = weekListNot.takeWhile { it.expireDate < monthEnd }
    val monthListNot = weekListNot.drop(monthList.size)


    val msg = """Ci sono ${fullList.size} prodotti
${expiredList.show(5, "scaduti")}${weekList.show(10, "entro 7 giorni")}${monthList.show(
        10,
        "entro 30 giorni"
    )}${monthListNot.show(10, "oltre 1 mese")}
"""
    return msg
}