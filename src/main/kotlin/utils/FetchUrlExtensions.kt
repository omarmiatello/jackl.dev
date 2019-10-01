package com.github.jacklt.gae.ktor.tg.utils

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


val json = Json(JsonConfiguration.Default.copy(strictMode = false, prettyPrint = true))

inline fun <T> T.toJson(serializer: SerializationStrategy<T>) = json.stringify(serializer, this)

inline fun <T> T.toJsonContent(serializer: SerializationStrategy<T>) =
    ByteArrayContent("application/json", json.stringify(serializer, this).toByteArray())

inline fun <T> T.toJsonPretty(serializer: SerializationStrategy<T>): String = json.stringify(serializer, this)

inline fun <reified T> HttpResponse.parse(serializer: DeserializationStrategy<T>): T? {
    if (isSuccessStatusCode) {
        return parseAsString()
            .takeIf { it != "null" }
            ?.let { json.parse(serializer, it) }
    } else {
        throw HttpResponseException(this)
    }
}

inline fun <reified T> HttpResponse.parseNotNull(serializer: DeserializationStrategy<T>): T {
    if (isSuccessStatusCode) {
        return json.parse(serializer, parseAsString())
    } else {
        throw HttpResponseException(this)
    }
}
