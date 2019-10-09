package com.github.jacklt.gae.ktor.tg.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val iso8601: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

fun Date.toIso8601(): String = iso8601.format(this)