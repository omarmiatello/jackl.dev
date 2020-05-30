package com.github.omarmiatello.jackldev.feature.noexp

import com.github.omarmiatello.noexp.NoExpDBModel
import com.github.omarmiatello.noexp.extractCategories
import com.github.omarmiatello.noexp.toCatNames
import com.github.omarmiatello.noexp.toCatParentsNames
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
                cat = categories.toCatNames(),
                catParents = categories.toCatParentsNames()
            ).toJson(),
            ContentType.Application.Json
        )
    }
}
