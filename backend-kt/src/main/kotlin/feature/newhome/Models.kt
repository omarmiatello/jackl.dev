package com.github.omarmiatello.jackldev.feature.newhome

import com.github.omarmiatello.jackldev.emojiAnimals
import com.github.omarmiatello.jackldev.show
import com.github.omarmiatello.jackldev.utils.jsoupGet
import kotlinx.serialization.Serializable
import org.jsoup.select.Elements
import java.text.NumberFormat
import java.util.*
import kotlin.math.absoluteValue


private val immUrlRegex = "(https://www\\.immobiliare\\.it/annunci/\\d+)/?.*?".toRegex()
private val ideUrlRegex = "(https://www\\.idealista\\.it/immobile/\\d+)/?.*?".toRegex()
private val immAmountRegex = ".*?€ ([\\d.]+).*".toRegex()
private val ideAmountRegex = ".*?([\\d.]+) €.*".toRegex()

private val Map<String, Review>.icon
    get() = when (map { it.value.vote }.average().toInt()) {
        0 -> ""
        in 1..5 -> "😡"
        6 -> "🧐"
        7 -> "🙂"
        8 -> "😄"
        9 -> "😍"
        else -> "🤯"
    }

private fun Elements.toIntOrDefault(default: Int) =
    firstOrNull()?.let { it.text().filter { it.isDigit() }.toIntOrNull() } ?: default

private fun Map<String, Review>.showReviews(): String {
    return toList().joinToString("\n") {
        val emoji = emojiAnimals[it.first.hashCode().absoluteValue % emojiAnimals.size]
        "${emoji}${it.second}"
    }
}

private fun Map<String, Review>.showReviewsShort(): String {
    return toList().joinToString("\n") {
        val emoji = emojiAnimals[it.first.hashCode().absoluteValue % emojiAnimals.size]
        "${emoji}${it.second.toString().lines().first()}"
    }
}

@Serializable
data class Review(val vote: Int, val commment: String? = null) {
    override fun toString() = if (commment == null) "$vote" else "$vote: $commment"
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
    val address: String? = null,
    val visited: Boolean = false
) {
    val idDatabase get() = "${site}_${id}"
    val idShort get() = "${if (site == "immobiliare") "imb" else "idl"}${id}"

    val priceFormatted
        get() = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply { minimumFractionDigits = 0 }.format(price)

    val surfaceSize: Int
        get() = (details.getOrElse("Superficie") { null } ?: details.values.firstOrNull { " m²" in it })!!
            .takeWhile { it.isDigit() }.toInt()

    val surfaceFormatted get() = "${surfaceSize}m²"

    val priceMeter2Formatted
        get() = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply { minimumFractionDigits = 0 }
            .format(price / surfaceSize) + "/m²"

    val actionIcon
        get() = when (action) {
            ACTION_AUCTION -> "📢"
            ACTION_BUY -> "🏠"
            ACTION_RENT -> "🛏"
            else -> "❓"
        }

    val visitedIcon
        get() = if (visited) "👀" else ""

    fun icons(reviewsMap: Map<String, Review>?) =
        """$actionIcon${reviewsMap?.icon ?: ""}$visitedIcon"""

    fun descShort(reviewsMap: Map<String, Review>? = null, showTags: Boolean = true) =
        """${icons(reviewsMap)} $title $url
        |$priceFormatted / $surfaceFormatted ($priceMeter2Formatted) /$idShort${show(
            "",
            tags.takeIf { showTags })}${show(
            "",
            reviewsMap?.showReviewsShort()
        )}
    """.trimMargin()

    fun descDetails(reviewsMap: Map<String, Review>) =
        """${icons(reviewsMap)} $title, $subtitle
        |Price: $priceFormatted / $surfaceFormatted ($priceMeter2Formatted)
        |Details: ${details.toList().joinToString("\n") {
            if (it.first.drop(1).all { it.isDigit() }) {
                it.second
            } else {
                it.first + ": " + it.second
            }
        }}
        |/$idShort
        |Images: $photos 📷, $video 📹, $planimetry_photos 🗺${show(
            "Tags: ",
            tags
        )}${show("Url: ", url)}
        |REVIEWS${show("", reviewsMap.showReviews())}
        |
        |- per votare: scrivi un voto (1-10) e un commento, es: "10 bellissima!"
        |- Per eliminare la casa: /delete
        |- Aggiorna ${if (visited) "/davedere" else "/visto"}
    """.trimMargin()

    companion object {
        const val ACTION_AUCTION = "auction"
        const val ACTION_BUY = "buy"
        const val ACTION_RENT = "rent"
    }
}

fun String.parseHouse(): House {
    return when {
        immUrlRegex.matches(this) -> parseImmobiliare(
            immUrlRegex.find(this)!!.groupValues[1]
        )
        ideUrlRegex.matches(this) -> parseIdealista(
            ideUrlRegex.find(this)!!.groupValues[1]
        )
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

    val contract = detailsMap["Contratto"].orEmpty()

    return House(
        site = "immobiliare",
        id = url.takeLastWhile { it.isDigit() },
        action = when {
            contract.contains(
                "Vendita",
                ignoreCase = true
            ) -> House.ACTION_BUY
            contract.contains(
                "Affitto",
                ignoreCase = true
            ) -> House.ACTION_RENT
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
