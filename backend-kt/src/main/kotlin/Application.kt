package com.github.omarmiatello.jackldev

import com.github.omarmiatello.jackldev.config.AppConfig
import com.github.omarmiatello.jackldev.config.MyConfig
import com.github.omarmiatello.jackldev.feature.newhome.webhookSpreadsheetNewHome
import com.github.omarmiatello.jackldev.feature.supermarket.webhookSupermarket
import com.github.omarmiatello.jackldev.feature.telegram.webhookTelegram
import com.github.omarmiatello.jackldev.feature.noexp.webhookNoExp
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.serialization.json.Json

fun Application.module() {
    install(DefaultHeaders) { header("X-Engine", "Ktor") }
    install(ContentNegotiation) { json() }
    install(CallLogging)
    install(StatusPages)

    routing {
        get("/") { call.respondHtml { body { h1 { +"${emojiAnimals.random()} + ${emojiAnimals.random()} = ${emojiAnimals.random()}" } } } }
        route("webhook") {
            route("telegram") { webhookTelegram() }
            route("spreadsheet") { webhookSpreadsheetNewHome() }
            route("noexp") { webhookNoExp() }
            route("supermarket") { webhookSupermarket() }
        }
        static("/static") { resources("static") }
    }
}

fun main() {
    println(Json {  }.encodeToString(AppConfig.serializer(), MyConfig))
    embeddedServer(
        factory = CIO,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        watchPaths = listOf("build"),
        module = Application::module,
    ).start(wait = true)
}
