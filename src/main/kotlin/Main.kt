package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.appEngineCacheFast
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.data.FireDB
import com.github.jacklt.gae.ktor.tg.utils.TelegramHelper
import com.github.jacklt.gae.ktor.tg.utils.json
import kotlinx.serialization.json.content
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import java.util.logging.Level
import java.util.logging.Logger

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
            if (msgs.isEmpty()) "Non ci sono case" else msgs.last()
        }
        userInput.toLowerCase() in listOf("affitto", "affitti", "affitta", "rent", "🛏") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_RENT }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Non ci sono affitti" else msgs.last()
        }
        userInput.toLowerCase() in listOf("asta", "aste", "auction", "bid", "📢") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_AUCTION }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Non ci sono aste" else msgs.last()
        }
        userInput.toLowerCase() in listOf("voto", "vota", "votare") -> {
            val msgs = getHousesWithReviewsStrings { message.getUserName() !in it.second.keys }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Hai votato tutto! 🎉" else msgs.last()
        }
        userInput.startsWith("/") -> {
            val cmd = userInput.drop(1)
            when {
                cmd.take(3) == "imb" -> showHouse(message, "immobiliare_" + cmd.drop(3).takeWhile { it != '@' })
                cmd.take(3) == "idl" -> showHouse(message, "idealista_" + cmd.drop(3).takeWhile { it != '@' })
                houseId != null -> when (cmd) {
                    "visto" -> visitedHouse(houseId, true)
                    "davedere" -> visitedHouse(houseId, false)
                    "delete" -> deleteHouse(houseId)
                    else -> "Comando sconosciuto"
                }
                else -> "Comando sconosciuto"
            }
        }
        houseId != null && userInput.firstOrNull()?.isDigit() ?: false -> updateComment(message, houseId)
        else -> searchAndSaveHouses(message)
    }
}


private fun showHouse(message: Message, houseId: String): String {
    val house = getHouse(houseId)
    return if (house != null) {
        appEngineCacheFast[message.getUserName()] = houseId
        val reviewsMap = getReviewsByHouse(houseId)
        house.descDetails(reviewsMap)
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
            saveReview(
                houseId, userName, Review(
                    vote = userInput.takeWhile { it.isDigit() }.toInt(),
                    commment = userInput.dropWhile { it.isDigit() }.drop(1)
                )
            )
        }

        val reviewsMap = getReviewsByHouse(houseId)
        val houseNeedReview = getHousesWithReviews()
            .sorted()
            .firstOrNull { message.getUserName() !in it.second.keys }
            ?.first
        val nextReview = if (houseNeedReview == null) {
            "Hai votato tutto! 🎉"
        } else {
            "Prossima review: /${houseNeedReview.idShort}"
        }
        "${house.descShort(reviewsMap)}\n$nextReview"
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
        house.descDetails(reviewsMap)
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

fun getFullJson(predicate: (Pair<House, Map<String, Review>>) -> Boolean): String {
    val sortedList = getHousesWithReviews().filter(predicate).sorted()
    val nickEmptyMap = sortedList.flatMap { it.second.keys }.distinct().map { it to "" }.toMap()
    val detailsEmptyMap = sortedList.flatMap {
        val keys = it.first.details.keys
        if ("d0" in keys) emptySet() else keys
    }.distinct().map { it to "" }.toMap()

    val results = sortedList.map { (house, votes) ->
        val houseJsonObj = json.toJson(House.serializer(), house).jsonObject
        val details = houseJsonObj["details"]?.jsonObject.orEmpty()
        val votesMap = votes.map { it.key to it.value.toString() }.toMap()
        val detailsMap = if (details.containsKey("d0")) {
            mapOf("details" to details.values.joinToString { it.content })
        } else {
            details.mapValues { it.value.content }
        }
        val objectMap = (houseJsonObj - "details").mapValues { it.value.content }

        nickEmptyMap + detailsEmptyMap + votesMap + detailsMap + objectMap
    }.toList()
    return json.stringify((String.serializer() to String.serializer()).map.list, results)
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

fun visitedHouse(houseId: String, visited: Boolean): String {
    FireDB["house/$houseId/visited", Boolean.serializer()] = visited
    return "🏠 ${if (visited) "vista" else "da vedere"}!"
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
            it.first.descShort(reviewsMap, showTags = false)
        }
    }
}

private fun getHousesWithReviewsStrings(predicate: (Pair<House, Map<String, Review>>) -> Boolean) =
    getHousesWithReviews().filter(predicate).sorted().toStringList()