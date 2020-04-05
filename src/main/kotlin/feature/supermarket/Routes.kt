package com.github.omarmiatello.jackldev.feature.supermarket

import config.MyConfig
import feature.supermarket.EsselungaClient
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import service.telegram.TelegramApi

fun Route.webhookSupermarket() {
    get("esselunga") {
        val availableSlots = EsselungaClient.getAvailableSlots()
        val msg = buildString {
            if (availableSlots.isNotEmpty()) {
                appendln("${availableSlots.size} slot disponibili:")
                append(availableSlots.joinToString("\n"))
            } else {
                append("Nessuno slot disponibile")
            }
        }
        if (availableSlots.isNotEmpty()) {
            TelegramApi.sendMessage(
                MyConfig.chat_esselunga_venegono,
                "L'Esselunga di Venegono Inferiore in questo momento ha $msg",
                TelegramApi.ParseMode.HTML
            )
        }
        call.respondText(msg)
    }
}