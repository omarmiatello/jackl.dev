package com.github.omarmiatello.jackldev.feature.supermarket

import com.github.omarmiatello.telegram.ParseMode
import com.github.omarmiatello.jackldev.config.MyConfig
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import com.github.omarmiatello.jackldev.service.telegram.telegramApi

fun Route.webhookSupermarket() {
    get("esselunga") {
        val availableSlots = EsselungaClient.getAvailableSlots()
        val msg = buildString {
            if (availableSlots.isNotEmpty()) {
                appendLine("${availableSlots.size} slot disponibili:")
                append(availableSlots.joinToString("\n"))
            } else {
                append("Nessuno slot disponibile")
            }
        }
        if (availableSlots.isNotEmpty()) {
            telegramApi.sendMessage(
                MyConfig.chat_esselunga_venegono,
                "L'Esselunga di Venegono Inferiore in questo momento ha $msg",
                ParseMode.HTML
            )
        }
        call.respondText(msg)
    }
}