package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.appEngineCacheFast
import com.github.jacklt.gae.ktor.tg.appengine.jsoupGet
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.data.FireDB
import kotlinx.serialization.Serializable
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import org.jsoup.select.Elements
import java.text.NumberFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

fun main() = startApp()

private val immUrlRegex = "(https://www\\.immobiliare\\.it/annunci/\\d+)/?.*?".toRegex()
private val ideUrlRegex = "(https://www\\.idealista\\.it/immobile/\\d+)/?.*?".toRegex()
private val immAmountRegex = ".*?€ ([\\d.]+).*".toRegex()
private val ideAmountRegex = ".*?([\\d.]+) €.*".toRegex()

private val NAME_REVIEW_SERIALIZER = (String.serializer() to Review.serializer()).map

private fun show(prefix: String, field: String?) = if (field.isNullOrEmpty()) "" else "\n|$prefix$field"

private fun Elements.toIntOrDefault(default: Int) =
    firstOrNull()?.let { it.text().filter { it.isDigit() }.toIntOrNull() } ?: default

private val Message.userName
    get() = from?.run { username ?: listOfNotNull(first_name, last_name).joinToString(" ") } ?: "unknown user"

fun myApp(message: Message): String {
    val userInput = message.text.orEmpty()
    val houseId = appEngineCacheFast[message.userName]
    appEngineCacheFast.delete(message.userName)
    return when {
        userInput in listOf("casa", "case", "🏠", "🏡", "🏘", "🏚") -> showHouses()
        userInput.startsWith("/") -> showHouse(message, userInput.drop(1).takeWhile { it != '@' })
        houseId != null && userInput.firstOrNull()?.isDigit() ?: false -> updateComment(message, houseId)
        houseId != null && userInput == "delete" -> deleteHouse(houseId)
        else -> searchAndSaveHouses(message)
    }
}

private fun showHouse(message: Message, houseId: String): String {
    val house = FireDB["house/$houseId", House.serializer()]
    val reviewsMap = FireDB["review/$houseId", NAME_REVIEW_SERIALIZER].orEmpty()
    return if (house != null) {
        appEngineCacheFast[message.userName] = houseId
        val reviews = reviewsMap.toList().joinToString("\n") { "${it.first} ${it.second}" }
        """${house.descDetails(showUrl = true)}
            |-- REVIEWS --${show("", reviews)}
            |
            |Puoi lasciare un voto (1-10) e se vuoi a fianco un piccolo commento, es: "10 bellissima!
            |Per eliminare la casa (e i commenti) scrivi: delete"
    """.trimMargin()
    } else {
        "Non c'è la casa $houseId"
    }
}

private fun updateComment(message: Message, houseId: String): String {
    val house = FireDB["house/$houseId", House.serializer()]
    val userInput = message.text.orEmpty()
    val review = Review(userInput.takeWhile { it.isDigit() }.toInt(), userInput.dropWhile { it.isDigit() }.drop(1))
    FireDB["review/$houseId/${message.userName}", Review.serializer()] = review
    return if (house != null) {
        "${house.descShort()}\n${message.userName} $review"
    } else {
        "Non c'è la casa $houseId"
    }
}

private fun showHouses(): String {
    val houses = FireDB["house", (String.serializer() to House.serializer()).map].orEmpty().values
    val reviewsMap = FireDB["review", (String.serializer() to NAME_REVIEW_SERIALIZER).map].orEmpty()
    val houseReviews = houses.associateWith { reviewsMap["${it.site}_${it.id}"].orEmpty() }.toList()
        .sortedByDescending { it.second.map { it.value.vote }.ifEmpty { listOf(0) }.average() }
        .sortedBy { it.first.action }
    return houseReviews.joinToString("\n\n") {
        val reviews = it.second.toList().joinToString("\n") { "${it.first} ${it.second}" }
        "${it.first.descShort(showTags = false)}${show("", reviews)}".trimMargin()
    }
}

fun deleteHouse(houseId: String): String {
    FireDB.delete("house/$houseId")
    FireDB.delete("review/$houseId")
    return "🏠 eliminata!"
}


private fun searchAndSaveHouses(message: Message): String {
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

    return if (houses.size == 1) {
        houses.first().descDetails()
    } else {
        val housesDesc = show("\n", houses.joinToString("\n\n") { it.descShort() })
        """Trovate ${houses.size} case (${errors.size} errori).$housesDesc""".trimMargin()
    }
}

private fun String.parseHouse(): House {
    return when {
        immUrlRegex.matches(this) -> parseImmobiliare(immUrlRegex.find(this)!!.groupValues[1])
        ideUrlRegex.matches(this) -> parseIdealista(ideUrlRegex.find(this)!!.groupValues[1])
        else -> error("Unknown url $this")
    }
}


private fun parseImmobiliare(url: String): House {
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

private fun parseIdealista(url: String): House {
    val html = try {
        jsoupGet(url)
    } catch (e: Exception) {
        error("Idealista: $url non disponibile, ${e.javaClass.simpleName}")
    }

    val detailsList = html.select("#details li, .flex-feature .txt-medium").map { it.text() }
    val priceText = html.select(".features__price > span , .info-data-price").first().text()
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
        details = detailsList.mapIndexed { index, s -> "d$index" to s }.toMap(),
        tags = null,
        planimetry_photos = html.getElementsContainingText("Planimetria").size,
        video = 0,
        photos = html.select(".show").size + html.select(".more-photos").toIntOrDefault(0),
        address = html.select("#headerMap li").text()
    )
}

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
    val tags: String? = null,
    val planimetry_photos: Int,
    val video: Int,
    val photos: Int,
    val address: String? = null
) {
    val priceFormatted get() = NumberFormat.getCurrencyInstance(Locale.ITALY).format(price)

    fun descShort(showTags: Boolean = true) =
        """[${action.toUpperCase()}] $title ($priceFormatted) /${site}_$id${show("Tags: ", tags.takeIf { showTags })}""".trimMargin()

    fun descDetails(showUrl: Boolean = false) = """[${action.toUpperCase()}] $title, $subtitle
        |Price: $priceFormatted
        |Details: ${details.orEmpty().toList().joinToString("\n") {
        if (it.first.drop(1).all { it.isDigit() }) {
            it.second
        } else {
            it.first + ": " + it.second
        }
    }}
        |Images: $photos 📷, $video 📹, $planimetry_photos 🗺${show("Tags: ", tags)}${show("Url: ", url)}
        |/${site}_$id
    """.trimMargin()

    companion object {
        const val ACTION_AUCTION = "auction"
        const val ACTION_BUY = "buy"
        const val ACTION_RENT = "rent"
    }
}

@Serializable
data class Review(val vote: Int, val commment: String?) {
    override fun toString() = "Vote: $vote - $commment"
}