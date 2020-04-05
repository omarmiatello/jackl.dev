package com.github.omarmiatello.jackldev.feature.telegram

import com.github.omarmiatello.jackldev.toAppResponse
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import service.telegram.TelegramApi
import service.telegram.TelegramRequest
import service.telegram.Update
import utils.json

fun Route.webhookTelegram() {
    post("/") {
        val request = json.parse(Update.serializer(), call.receiveText())

        when {
            request.message != null -> {
                val inputText = request.message.text
                if (inputText != null) {

                    fun telegramMessage(chatId: Int, text: String) =
                        Gson().toJsonTree(TelegramRequest.SendMessageRequest(chatId.toString(), text, TelegramApi.ParseMode.HTML.str))
                            .apply { asJsonObject.addProperty("method", "sendMessage") }
                            .toString()

                    call.respondText(
                        telegramMessage(request.message.chat.id, request.message.toAppResponse()),
                        ContentType.parse("application/json")
                    )
                }
            }
            request.inline_query != null -> {
                // TODO handle
            }
        }

        if (call.response.status() == null) call.respond(HttpStatusCode.NoContent)
    }
}