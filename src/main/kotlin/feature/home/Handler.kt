package feature.home

import java.util.concurrent.TimeUnit


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
