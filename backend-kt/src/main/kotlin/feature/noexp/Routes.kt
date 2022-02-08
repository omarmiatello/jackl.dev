package com.github.omarmiatello.jackldev.feature.noexp

import com.github.omarmiatello.telegram.ParseMode
import com.github.omarmiatello.jackldev.config.MyConfig
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import com.github.omarmiatello.jackldev.service.telegram.telegramApi
import com.github.omarmiatello.jackldev.utils.json
import com.github.omarmiatello.noexp.*
import com.github.omarmiatello.noexp.utils.extractCategories
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

fun Route.webhookNoExp() {
    get("telegram/expire") {
        val msg = expireMessage()
        telegramApi.sendMessage(MyConfig.chat_case, msg, ParseMode.HTML)
        call.respondText(msg)
    }
    post("new") {
        val productDao = NoExpDBModel.ProductDao.fromJson(call.receiveText())
        val name = productDao.name ?: error("Missing name in $productDao")
        val categories = name.extractCategories(allCategories, allCategories.first())
        call.respondText(
            productDao.copy(
                cat = categories.map { it.name },
                catParents = categories.map { it.allParents.joinToString() },
            ).toJson(),
            ContentType.Application.Json
        )
    }
}
