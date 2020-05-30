package com.github.omarmiatello.jackldev.feature.telegram

import com.github.omarmiatello.jackldev.toAppResponse
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import com.github.omarmiatello.telegram.*
import com.github.omarmiatello.telegram.TelegramRequest.*

fun Route.webhookTelegram() {
    post("/") {
        val request = call.receiveText().parseTelegramRequest()

        val message = request.message
        when {
            message != null -> {
                val inputText = message.text
                if (inputText != null) {
                    call.respondText(
                        SendMessageRequest(
                            chat_id = message.chat.id.toString(),
                            text = message.toAppResponse(),
                            parse_mode = ParseMode.HTML
                        ).toJsonForResponse(),
                        ContentType.Application.Json
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