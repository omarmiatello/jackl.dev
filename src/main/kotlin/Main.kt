package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.jsoupGet
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.data.FireDB
import kotlinx.serialization.Serializable
import org.jsoup.select.Elements
import java.util.logging.Level
import java.util.logging.Logger

fun main() = startApp()

val immUrlRegex = "(https://www\\.immobiliare\\.it/annunci/\\d+)/?.*?".toRegex()
val ideUrlRegex = "(https://www\\.idealista\\.it/immobile/\\d+)/?.*?".toRegex()
val immAmountRegex = ".*?€ ([\\d.]+).*".toRegex()
val ideAmountRegex = ".*?([\\d.]+) €.*".toRegex()

fun myApp(message: Message): String {
    fun isChatGroup() = true || message.chat.type == "group"
    return when {
        isChatGroup() -> {
            val userInput = message.text.orEmpty()
            val urls = message.entities
                ?.filter { it.type == "url" }
                ?.map { userInput.substring(it.offset, it.offset + it.length) }
                .orEmpty()

            val errors = mutableListOf<String>()
            val houses = urls.mapNotNull {
                try {
                    it.parseHouse()
                } catch (e: Exception) {
                    Logger.getLogger("ok").log(Level.WARNING, e.message, e)
                    errors += e.message.orEmpty()
                    null
                }
            }

            FireDB.update("house", houses.associateBy { "${it.site}_${it.id}" }, House.serializer())

            "Trovate ${houses.size} case (${errors.size} errori)."
        }
        else -> "Puoi aggiungermi ad un canale e rendemi admin per leggere i messaggi"
    }
}

private fun String.parseHouse(): House {
    return when {
        immUrlRegex.matches(this) -> parseImmobiliare(immUrlRegex.find(this)!!.groupValues[1])
        ideUrlRegex.matches(this) -> parseIdealista(ideUrlRegex.find(this)!!.groupValues[1])
        else -> error("Unknown url $this")
    }
}


fun parseImmobiliare(url: String): House {
    val html = try {
        jsoupGet(url)
    } catch (e: Exception) {
        error("Immobiliare: $url non disponibile, ${e.javaClass.simpleName}")
    }

    val detailsMap = html.select(".section-data .col-xs-12 .col-xs-12").map { it.text() }
        .chunked(2).map { it[0] to it[1] }.toMap()
    val priceText = html.select(".features__price > span").first().text()
    val price = immAmountRegex.find(priceText)!!.groupValues[1].filter { it.isDigit() }.toInt()

    return House(
        site = "immobiliare",
        id = url.takeLastWhile { it.isDigit() },
        action = when (detailsMap["Contratto"]) {
            "Vendita" -> House.ACTION_BUY
            "Affitto" -> House.ACTION_RENT
            else -> House.ACTION_AUCTION
        },
        url = url,
        price = price,
        title = html.getElementsByClass("title-detail").text(),
        subtitle = html.getElementsByClass("description__title").text(),
        description = html.getElementsByClass("description-text").text(),
        details = detailsMap,
        tags = html.select(".label-gray").map { it.text() }.joinToString(),
        planimetry_photos = html.select("#plan-tab").toIntOrDefault(0),
        video = html.select("#video-tab").toIntOrDefault(0),
        photos = html.select("#foto-tab").toIntOrDefault(0),
        address = html.select(".leaflet-control").firstOrNull()?.text()
    )
}

fun parseIdealista(url: String): House {
    val html = try {
        jsoupGet(url)
    } catch (e: Exception) {
        error("Idealista: $url non disponibile, ${e.javaClass.simpleName}")
    }

    val detailsList = html.select("#details li, .flex-feature .txt-medium").map { it.text() }
    val priceText = html.select(".features__price > span").first().text()
    val price = ideAmountRegex.find(priceText)!!.groupValues[1].filter { it.isDigit() }.toInt()

    return House(
        site = "idealista",
        id = url.takeLastWhile { it.isDigit() },
        action = if (price > 5000) House.ACTION_BUY else House.ACTION_RENT,
        url = url,
        price = price,
        title = html.getElementsByClass("main-info__title-main").text(),
        subtitle = html.getElementsByClass("main-info__title-minor").text(),
        description = html.getElementsByClass("comment").text(),
        details = detailsList.mapIndexed { index, s -> index.toString() to s }.toMap(),
        tags = null,
        planimetry_photos = html.getElementsContainingText("Planimetria").size,
        video = 0,
        photos = html.select(".show").size + html.select(".more-photos").toIntOrDefault(0),
        address = html.select("#headerMap li").text()
    )
}

private fun Elements.toIntOrDefault(default: Int) =
    firstOrNull()?.let { it.text().filter { it.isDigit() }.toIntOrNull() } ?: default

@Serializable
data class House(
    val site: String,
    val id: String,
    val action: String,
    val url: String,
    val price: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val details: Map<String, String>,
    val tags: String?,
    val planimetry_photos: Int,
    val video: Int,
    val photos: Int,
    val address: String?
) {
    companion object {
        const val ACTION_AUCTION = "auction"
        const val ACTION_BUY = "buy"
        const val ACTION_RENT = "rent"
    }
}