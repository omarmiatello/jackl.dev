package utils

import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


val json = Json(JsonConfiguration.Default.copy(ignoreUnknownKeys = true, prettyPrint = true, encodeDefaults = false))

inline fun <T> T.toJson(serializer: SerializationStrategy<T>) = json.stringify(serializer, this)

inline fun <T> String.parse(serializer: DeserializationStrategy<T>) = json.parse(serializer, this)

inline fun <T> T.toJsonContent(serializer: SerializationStrategy<T>) =
    TextContent(json.stringify(serializer, this), ContentType.Application.Json)

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
