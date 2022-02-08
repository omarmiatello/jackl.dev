package com.github.omarmiatello.jackldev.config

import com.akuleshov7.ktoml.Toml
import com.github.omarmiatello.jackldev.utils.readSecret
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
class AppConfig(
    val telegram: Telegram,
    val esselunga: Esselunga,
    val extra: Map<String, String>,
) {
    @Serializable
    class Telegram(
        var apiKey: String
    )
    @Serializable
    class Esselunga(
        var username: String,
        var password: String
    )

    companion object {
        val default: AppConfig get() = Toml.decodeFromString(serializer(), readSecret("config.toml"))
    }
}
