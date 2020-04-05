package com.github.omarmiatello.jackldev

import com.github.omarmiatello.jackldev.feature.newhome.House
import com.google.gson.Gson
import config.MyConfig
import feature.home.expireMessage
import feature.supermarket.EsselungaClient
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import kotlinx.html.body
import kotlinx.html.h1
import service.telegram.TelegramApi
import service.telegram.TelegramRequest
import service.telegram.Update
import utils.json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ContentNegotiation) {
        json()
    }

    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(StatusPages)

    fun telegramMessage(chatId: Int, text: String) =
        Gson().toJsonTree(TelegramRequest.SendMessageRequest(chatId.toString(), text, TelegramApi.ParseMode.HTML.str))
            .apply { asJsonObject.addProperty("method", "sendMessage") }
            .toString()

    // Registers routes
    routing {
        // For the root / route, we respond with an Html.
        // The `respondHtml` extension method is available at the `ktor-html-builder` artifact.
        // It provides a DSL for building HTML to a Writer, potentially in a chunked way.
        // More information about this DSL: https://github.com/Kotlin/kotlinx.html

        get("/") {
            call.respondHtml { body { h1 { +"${emojiAnimals.random()} + ${emojiAnimals.random()} = ${emojiAnimals.random()}" } } }
        }
        route("webhook") {
            post("telegram") {
                val request = json.parse(Update.serializer(), call.receiveText())

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
        get("feature/supermarket/esselunga") {
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

        static("/static") {
            resources("static")
        }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
