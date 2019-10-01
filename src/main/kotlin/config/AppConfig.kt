package com.github.jacklt.gae.ktor.tg.config

@DslMarker
annotation class AppConfigMarker

@AppConfigMarker
abstract class AppConfig {
    val telegram = TELEGRAM()

    fun telegram(conf: TELEGRAM.() -> Unit) {
        telegram.conf()
    }

    companion object {
        fun getDefault(): AppConfig = MyConfig
    }
}

@AppConfigMarker
class TELEGRAM {
    var apiKey: String = ""

    // chatId: Unique identifier for the target chat or username of the target channel (in the format @channelusername)
    var chatId: String = ""
}

