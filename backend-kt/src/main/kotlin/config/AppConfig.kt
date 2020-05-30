package com.github.omarmiatello.jackldev.config

abstract class AppConfig(
    val telegram: Telegram,
    val esselunga: Esselunga
) {
    class Telegram(
        var apiKey: String
    )

    class Esselunga(
        var username: String,
        var password: String
    )

    companion object {
        val default: AppConfig get() = MyConfig
    }
}
