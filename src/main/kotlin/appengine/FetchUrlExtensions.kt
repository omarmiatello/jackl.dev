package com.github.jacklt.gae.ktor.tg.appengine

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


@UnstableDefault
val json = Json(JsonConfiguration.Default.copy(prettyPrint = true))

@UnstableDefault
val jsonNonStrict = Json(JsonConfiguration.Default.copy(ignoreUnknownKeys = true))


@UnstableDefault
fun <T> T.toJsonContent(serializer: SerializationStrategy<T>) =
    ByteArrayContent("application/json", json.stringify(serializer, this).toByteArray())

@UnstableDefault
fun <T> T.toJsonPretty(serializer: SerializationStrategy<T>): String = json.stringify(serializer, this)

@UnstableDefault
inline fun <reified T> HttpResponse.parse(serializer: DeserializationStrategy<T>): T? {
    if (isSuccessStatusCode) {
        return parseAsString()
            .takeIf { it != "null" }
            ?.let { jsonNonStrict.parse(serializer, it) }
    } else {
        throw HttpResponseException(this)
    }
}

@UnstableDefault
inline fun <reified T> HttpResponse.parseNotNull(serializer: DeserializationStrategy<T>): T {
    if (isSuccessStatusCode) {
        return jsonNonStrict.parse(serializer, parseAsString())
    } else {
        throw HttpResponseException(this)
    }
}


