package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.telegram.TelegramApi
import com.github.jacklt.gae.ktor.tg.appengine.telegram.TelegramRequest
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Update
import com.github.jacklt.gae.ktor.tg.config.MyConfig
import com.github.jacklt.gae.ktor.tg.feature.home.NoExpDB
import com.github.jacklt.gae.ktor.tg.feature.home.expireMessage
import com.github.jacklt.gae.ktor.tg.utils.TelegramHelper
import com.google.gson.Gson
import feature.home.Product
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * For more information about this file: https://ktor.io/servers/configuration.html#hocon-file
 */
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(StatusPages)

    val gson = Gson()
    fun telegramMessage(chatId: Int, text: String) =
        gson.toJsonTree(TelegramRequest.SendMessageRequest(chatId.toString(), text, TelegramApi.ParseMode.HTML.str))
            .apply { asJsonObject.addProperty("method", "sendMessage") }
            .toString()

    // Registers routes
    routing {
        // For the root / route, we respond with an Html.
        // The `respondHtml` extension method is available at the `ktor-html-builder` artifact.
        // It provides a DSL for building HTML to a Writer, potentially in a chunked way.
        // More information about this DSL: https://github.com/Kotlin/kotlinx.html

        route("webhook") {
            post("telegram") {
                val request = Json.nonstrict.parse(Update.serializer(), call.receiveText())

                when {
                    request.message != null -> {
                        val inputText = request.message.text
                        if (inputText != null) {
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

        get("buy") {
            call.respondText(getFullJson { it.first.action == House.ACTION_BUY })
        }
        get("rent") {
            call.respondText(getFullJson { it.first.action == House.ACTION_RENT })
        }
        get("auction") {
            call.respondText(getFullJson { it.first.action == House.ACTION_AUCTION })
        }
        get("feature/home/expire") {
            val msg = expireMessage()
            TelegramApi.sendMessage(MyConfig.chat_case, msg, TelegramApi.ParseMode.HTML)
            call.respondText(msg)
        }
    }
}
