package com.github.jacklt.gae.ktor.tg.utils

import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*

// Date utils

private val dateFormat by lazy { SimpleDateFormat("dd MMMM YYYY", Locale.ITALIAN) }
private val monthFormat by lazy { SimpleDateFormat("MMMM", Locale.ITALIAN) }
private val months by lazy {
    (0..11).map {
        monthFormat.format(Calendar.getInstance().apply { set(Calendar.MONTH, it) }.time)
            .toLowerCase(Locale.ITALIAN)
    }
}

val Long.formatDateWithDays: String?
    get() {
        val today = Calendar.getInstance()
        val expire = Calendar.getInstance().also { it.timeInMillis = this }
        val days = ChronoUnit.DAYS.between(today.toInstant(), expire.toInstant())
        return "${dateFormat.format(expire.time)}\n$days giorni"
    }

val Long.formatDateWithDaysShort: String?
    get() {
        val today = Calendar.getInstance()
        val expire = Calendar.getInstance().also { it.timeInMillis = this }
        val days = ChronoUnit.DAYS.between(today.toInstant(), expire.toInstant())
        return "${dateFormat.format(expire.time)}, ${days}g"
    }

val Long.formatDays: String?
    get() {
        val today = Calendar.getInstance()
        val expire = Calendar.getInstance().also { it.timeInMillis = this }
        val days = ChronoUnit.DAYS.between(today.toInstant(), expire.toInstant())
        return "${days}g"
    }
