package com.github.jacklt.gae.ktor.tg.config

object ExampleConfig : AppConfig() {

    // https://api.telegram.org/botTOKENsetWebhook?url=https://example.com/webhook/telegram
    // https://api.telegram.org/botTOKEN/deleteWebhook

    init {
        telegram {
            apiKey = ""
            chatId = ""
        }
    }
}