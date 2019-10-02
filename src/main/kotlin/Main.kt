package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.jsoupGet
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.utils.toJson
import kotlinx.serialization.Serializable
import java.util.logging.Level
import java.util.logging.Logger

fun main() = startApp()

val immobiliareRegex = "(https://www\\.immobiliare\\.it/annunci/\\d+)/?.*?".toRegex()
val idealistaRegex = "(https://www\\.idealista\\.it/immobile/\\d+)/?.*?".toRegex()

fun myApp(message: Message): String {
    fun isChatGroup() = message.chat.type == "group"
    return when {
        isChatGroup() -> {
            val userInput = message.text.orEmpty()
            val urls = message.entities
                ?.filter { it.type == "url" }
                ?.map { userInput.substring(it.offset, it.offset + it.length) }
                .orEmpty()

            "Trovati ${urls.size} indirizzi\n" + urls.joinToString("\n\n") { url ->
                when {
                    immobiliareRegex.matches(url) -> parseImmobiliare(immobiliareRegex.find(url)!!.groupValues[1])
                    idealistaRegex.matches(url) -> parseIdealista(idealistaRegex.find(url)!!.groupValues[1])
                    else -> "Ignorato: $url"
                }
            }

        }
        else -> "Puoi aggiungermi ad un canale e rendemi admin per leggere i messaggi"
    }
}

@Serializable
data class Immobile(
    val type: String,
    val url: String,
    val price: String,
    val title: String,
    val subtitle: String
    //val description: String
) {
    companion object {
        const val TYPE_ASTA = "Asta"
    }
}

fun parseImmobiliare(url: String): String {
    val html = try {
        jsoupGet(url)
    } catch (e: Exception) {
        return "Immobiliare: $url non disponibile: ${e.javaClass.simpleName}"
    }
    val title = html.getElementsByClass("title-detail").text()
    val immobile = Immobile(
        type = if (title.contains("asta", ignoreCase = true)) Immobile.TYPE_ASTA else "",
        url = url,
        price = html.getElementsByClass("features__price").select("span").text(),
        title = title,
        subtitle = html.getElementsByClass("description__title").text()
        // description = html.getElementsByClass("description-text").text()
    )
    Logger.getLogger("ok").log(Level.INFO, immobile.toJson(Immobile.serializer()))
    return "Immobiliare: $immobile"
}

fun parseIdealista(url: String): String {
    return "Idealista: $url"
}

private val Message.userName get() = from?.run { username ?: listOfNotNull(first_name, last_name).joinToString(" ") } ?: "unknown user"