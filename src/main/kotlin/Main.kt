package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.appEngineCacheFast
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.data.FireDB
import com.github.jacklt.gae.ktor.tg.utils.TelegramHelper
import com.google.appengine.api.ThreadManager
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.absoluteValue

fun main() = startApp()

private val NAME_REVIEW_SERIALIZER = (String.serializer() to Review.serializer()).map

private fun Message.getUserName() = from
    ?.run { username ?: listOfNotNull(first_name, last_name).joinToString(" ") }
    ?: "unknown user"

fun myApp(message: Message): String {
    val userInput = message.text.orEmpty()
    val houseId = appEngineCacheFast[message.getUserName()]
    appEngineCacheFast.delete(message.getUserName())
    return when {
        userInput.toLowerCase() in listOf("casa", "case", "buy", "compra", "🏠", "🏡", "🏘", "🏚") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_BUY }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            msgs.last().ifEmpty { "Non ci sono case" }
        }
        userInput.toLowerCase() in listOf("affitto", "affitti", "affitta", "rent", "🛏") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_RENT }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            msgs.last().ifEmpty { "Non ci sono affitti" }
        }
        userInput.toLowerCase() in listOf("asta", "aste", "auction", "bid", "📢") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_AUCTION }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            msgs.last().ifEmpty { "Non ci sono aste" }
        }
        userInput.toLowerCase() in listOf("voto", "vota", "votare") -> {
            val msgs = getHousesWithReviewsStrings { message.getUserName() !in it.second.keys }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            msgs.last().ifEmpty { "Hai votato tutto! 🎉" }
        }
        userInput.startsWith("/") -> {
            val cmd = userInput.drop(1)
            when {
                cmd.take(3) == "imb" -> showHouse(message, "immobiliare_" + cmd.drop(3).takeWhile { it != '@' })
                cmd.take(3) == "idl" -> showHouse(message, "idealista_" + cmd.drop(3).takeWhile { it != '@' })
                else -> "Comando sconosciuto"
            }
        }
        houseId != null && userInput.firstOrNull()?.isDigit() ?: false -> updateComment(message, houseId)
        houseId != null && userInput.toLowerCase() == "delete" -> deleteHouse(houseId)
        else -> searchAndSaveHouses(message)
    }
}

private fun showHouse(message: Message, houseId: String): String {
    val house = getHouse(houseId)
    return if (house != null) {
        appEngineCacheFast[message.getUserName()] = houseId
        val reviewsMap = getReviewsByHouse(houseId)
        """${house.descDetails(showUrl = true)}
            |REVIEWS${show("", reviewsMap.showReviews())}
            |
            |- per votare: scrivi un voto (1-10) e un commento, es: "10 bellissima!"
            |- Per eliminare la casa: ´delete´
    """.trimMargin()
    } else {
        "Non c'è la casa $houseId"
    }
}

private fun updateComment(message: Message, houseId: String): String {
    val house = getHouse(houseId)
    return if (house != null) {
        val userName = message.getUserName()
        val userInput = message.text.orEmpty()
        if (userInput.all { it.isDigit() }) {
            saveReviewVote(houseId, userName, userInput.toInt())
        } else {
            saveReview(houseId, userName, Review(
                vote = userInput.takeWhile { it.isDigit() }.toInt(),
                commment = userInput.dropWhile { it.isDigit() }.drop(1)
            ))
        }

        val reviewsMap = getReviewsByHouse(houseId)
        """${house.descShort()}
        |REVIEWS${show("", reviewsMap.showReviews())}
        """.trimMargin()
    } else {
        "Non c'è la casa $houseId"
    }
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

    saveHouses(houses)

    return if (houses.size == 1) {
        val house = houses.first()
        appEngineCacheFast[message.getUserName()] = house.let { it.idDatabase }
        val reviewsMap = getReviewsByHouse(house.idDatabase)
        """${house.descDetails()}
            |REVIEWS${show("", reviewsMap.showReviews())}
            |
            |- per votare: scrivi un voto (1-10) e un commento, es: "10 bellissima!"
            |- Per eliminare la casa: ´delete´
        """.trimMargin()
    } else {
        val housesDesc = show("\n", houses.joinToString("\n\n") { it.descShort() })
        val errorMsg = if (errors.size == 0) "" else " (${errors.size} errori)"
        if (houses.isEmpty() && errorMsg.isEmpty()) {
            "Puoi passarmi uno o più link di Immobiliare/Idelista o scrivere: case, affitto, asta, vota"
        } else {
            """Trovate ${houses.size} case$errorMsg.$housesDesc""".trimMargin()
        }
    }
}

private fun sendTelegram(chatId: String, msgs: List<String>) {
    if (msgs.isNotEmpty()) {
        TelegramHelper(chatId).send(*msgs.toTypedArray())
    }
}


// Houses utils

private fun getHouses() = FireDB["house", (String.serializer() to House.serializer()).map].orEmpty().values

private fun getHouse(houseId: String) = FireDB["house/$houseId", House.serializer()]

private fun saveHouses(houses: List<House>) {
    FireDB.update("house", houses.associateBy { it.idDatabase }, House.serializer())
}

private fun deleteHouse(houseId: String): String {
    FireDB.delete("house/$houseId")
    FireDB.delete("review/$houseId")
    return "🏠 eliminata!"
}

// Reviews utils

private fun saveReview(houseId: String, userName: String, review: Review) {
    FireDB["review/$houseId/$userName", Review.serializer()] = review
}

private fun saveReviewVote(houseId: String, userName: String, vote: Int) {
    FireDB["review/$houseId/$userName/vote", Int.serializer()] = vote
}

private fun getReviewsByHouse(houseId: String) = FireDB["review/$houseId", NAME_REVIEW_SERIALIZER].orEmpty()

private fun getReviewsMap() = FireDB["review", (String.serializer() to NAME_REVIEW_SERIALIZER).map].orEmpty()

private fun Map<String, Review>.showReviews() =
    toList().joinToString("\n") { "${it.first} ${it.second}" }

private fun Map<String, Review>.showReviewsEmoji() =
    toList().joinToString("") { it.second.voteToEmoji }

// Houses with Reviews utils

private fun getHousesWithReviews(): List<Pair<House, Map<String, Review>>> {
    val reviewsMap = getReviewsMap()
    return getHouses().associateWith { reviewsMap[it.idDatabase].orEmpty() }.toList()
}

private fun List<Pair<House, Map<String, Review>>>.sorted(): List<Pair<House, Map<String, Review>>> {
    return sortedBy { it.first.price }
        .sortedByDescending { it.second.map { it.value.vote }.ifEmpty { listOf(0) }.average() }
        .sortedBy { it.first.action }
}

private fun List<Pair<House, Map<String, Review>>>.toStringList(): List<String> {
    return chunked(15).map {
        it.joinToString("\n\n") {
            val reviewsMap = it.second
            val reviews = reviewsMap.toList().joinToString("\n") {
                val emoji = emojiAnimals[it.first.hashCode().absoluteValue % emojiAnimals.size]
                "$emoji${it.second}"
            }
            "${it.first.descShort(showTags = false)}${show("", reviews)}".trimMargin()
        }
    }
}

private fun getHousesWithReviewsStrings(predicate: (Pair<House, Map<String, Review>>) -> Boolean) =
    getHousesWithReviews().filter(predicate).sorted().toStringList()