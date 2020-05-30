package com.github.omarmiatello.jackldev.feature.newhome

import com.github.omarmiatello.jackldev.getFullJson
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.webhookSpreadsheetNewHome() {
    get("buy") {
        call.respondText(getFullJson { it.first.action == House.ACTION_BUY })
    }
    get("rent") {
        call.respondText(getFullJson { it.first.action == House.ACTION_RENT })
    }
    get("auction") {
        call.respondText(getFullJson { it.first.action == House.ACTION_AUCTION })
    }
}