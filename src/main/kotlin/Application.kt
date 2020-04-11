package com.github.omarmiatello.jackldev

import com.github.omarmiatello.jackldev.feature.newhome.webhookSpreadsheetNewHome
import com.github.omarmiatello.jackldev.feature.supermarket.webhookSupermarket
import com.github.omarmiatello.jackldev.feature.telegram.webhookTelegram
import feature.noexp.webhookNoExp
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
import kotlinx.html.body
import kotlinx.html.h1

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


    // Registers routes
    routing {
        // For the root / route, we respond with an Html.
        // The `respondHtml` extension method is available at the `ktor-html-builder` artifact.
        // It provides a DSL for building HTML to a Writer, potentially in a chunked way.
        // More information about this DSL: https://github.com/Kotlin/kotlinx.html

        get("/") { call.respondHtml { body { h1 { +"${emojiAnimals.random()} + ${emojiAnimals.random()} = ${emojiAnimals.random()}" } } } }
        route("webhook") {
            route("telegram") { webhookTelegram() }
            route("spreadsheet") { webhookSpreadsheetNewHome() }
            route("noexp") { webhookNoExp() }
            route("supermarket") { webhookSupermarket()}
        }
        static("/static") { resources("static") }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
