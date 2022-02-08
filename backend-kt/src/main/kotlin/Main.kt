package com.github.omarmiatello.jackldev

import com.github.omarmiatello.jackldev.feature.newhome.House
import com.github.omarmiatello.jackldev.feature.newhome.NewHomeDB
import com.github.omarmiatello.jackldev.feature.newhome.Review
import com.github.omarmiatello.jackldev.feature.newhome.parseHouse
import com.github.omarmiatello.jackldev.feature.noexp.expireMessage
import com.github.omarmiatello.jackldev.utils.InMemoryCache
import com.github.omarmiatello.telegram.Message
import com.github.omarmiatello.jackldev.feature.supermarket.EsselungaClient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import com.github.omarmiatello.jackldev.utils.TelegramHelper
import com.github.omarmiatello.jackldev.utils.json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

suspend fun main() = startApp()

private val NAME_REVIEW_SERIALIZER = MapSerializer(String.serializer(), Review.serializer())

private val Message.userName
    get() = from
        ?.run { username ?: listOfNotNull(first_name, last_name).joinToString(" ") }
        ?: "unknown user"

suspend fun myApp(message: Message): String {
    val userInput = message.text.orEmpty()
    val houseId = InMemoryCache[message.userName]
    InMemoryCache.delete(message.userName)
    val input = userInput.lowercase()
    return when {
        "cas[ae] \\d+".toRegex().matches(input) -> {
            val max: Int = "cas[ae] (\\d+)".toRegex().matchEntire(input)!!.groupValues[1].toInt()
            val msgs =
                getHousesWithReviewsStrings { it.first.action == House.ACTION_BUY && it.first.price <= max * 1000 }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Non ci sono case" else msgs.last()
        }
        input in listOf("casa", "case", "buy", "compra", "🏠", "🏡", "🏘", "🏚") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_BUY }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Non ci sono case" else msgs.last()
        }
        input in listOf("affitto", "affitti", "affitta", "rent", "🛏") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_RENT }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Non ci sono affitti" else msgs.last()
        }
        input in listOf("asta", "aste", "auction", "bid", "📢") -> {
            val msgs = getHousesWithReviewsStrings { it.first.action == House.ACTION_AUCTION }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Non ci sono aste" else msgs.last()
        }
        input in listOf("voto", "vota", "votare") -> {
            val msgs = getHousesWithReviewsStrings { message.userName !in it.second.keys }
            sendTelegram(message.chat.id.toString(), msgs.dropLast(1))
            if (msgs.isEmpty()) "Hai votato tutto! 🎉" else msgs.last()
        }
        input in listOf("ss", "spreadsheet", "docs", "doc") -> {
            "https://docs.google.com/spreadsheets/d/10VolNdRjvS376p0AsYu3VIFdpnN3bNbeYe02ulX6du4/edit?usp=sharing"
        }
        input in listOf("expire", "exp") -> {
            expireMessage()
        }
        input in listOf("esselunga", "s") -> {
            val availableSlots = EsselungaClient.getAvailableSlots()
            if (availableSlots.isNotEmpty()) "${availableSlots.size} slot disponibili: $availableSlots" else "Nessuno slot disponibile"
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
        InMemoryCache[message.userName] = houseId
        val reviewsMap = getReviewsByHouse(houseId)
        house.descDetails(reviewsMap)
    } else {
        "Non c'è la casa $houseId"
    }
}

private fun updateComment(message: Message, houseId: String): String {
    val house = getHouse(houseId)
    return if (house != null) {
        val userName = message.userName
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
            .firstOrNull { message.userName !in it.second.keys }
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
        ?.map { userInput.substring(it.offset.toInt(), it.offset.toInt() + it.length.toInt()) }
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
        InMemoryCache[message.userName] = house.let { it.idDatabase }
        val reviewsMap = getReviewsByHouse(house.idDatabase)
        house.descDetails(reviewsMap)
    } else {
        val housesDesc = show("\n", houses.joinToString("\n\n") { it.descShort() })
        val errorMsg = if (errors.size == 0) "" else " (${errors.size} errori)"
        if (houses.isEmpty() && errorMsg.isEmpty()) {
            "Puoi passarmi uno o più link di Immobiliare/Idelista o scrivere: case, affitto, asta, vota, doc"
        } else {
            """Trovate ${houses.size} case$errorMsg.$housesDesc""".trimMargin()
        }
    }
}

private suspend fun sendTelegram(chatId: String, msgs: List<String>) {
    if (msgs.isNotEmpty()) {
        TelegramHelper(chatId).send(*msgs.toTypedArray())
    }
}

fun getFullJson(predicate: (Pair<House, Map<String, Review>>) -> Boolean): String {
    val housesWithReviews = getHousesWithReviews()
    val sortedList = housesWithReviews.filter(predicate).sorted()
    val nickEmptyMap = housesWithReviews.flatMap { it.second.keys }.distinct().map { it to "" }.toMap()
    val detailsEmptyMap = sortedList.flatMap {
        val keys = it.first.details.keys
        if ("d0" in keys) emptySet() else keys
    }.distinct().map { it to "" }.toMap()

    val results = sortedList.map { (house, votes) ->
        val houseJsonObj = json.encodeToJsonElement(House.serializer(), house).jsonObject
        val details = houseJsonObj["details"]?.jsonObject.orEmpty()
        val icons = mapOf("🔥" to house.icons(votes))
        val votesMap = votes.map { it.key to it.value.toString() }.toMap()
        val detailsMap = if (details.containsKey("d0")) {
            mapOf("details" to details.values.joinToString { it.jsonPrimitive.content })
        } else {
            details.mapValues { it.value.jsonPrimitive.content }
        }
        val objectMap = (houseJsonObj - "details").mapValues { it.value.jsonPrimitive.content }

        icons + nickEmptyMap + detailsEmptyMap + votesMap + detailsMap + objectMap
    }.toList()
    return json.encodeToString(ListSerializer(MapSerializer(String.serializer(), String.serializer())), results)
}

// Houses utils

private fun getHouses(): Collection<House> {
    val t = (String.serializer() to House.serializer())
    return NewHomeDB["house", MapSerializer(t.first, t.second)].orEmpty().values
}

private fun getHouse(houseId: String) = NewHomeDB["house/$houseId", House.serializer()]

private fun saveHouses(houses: List<House>) {
    NewHomeDB.update("house", houses.associateBy { it.idDatabase }, House.serializer())
}

private fun deleteHouse(houseId: String): String {
    NewHomeDB.delete("house/$houseId")
    NewHomeDB.delete("review/$houseId")
    return "🏠 eliminata!"
}

fun visitedHouse(houseId: String, visited: Boolean): String {
    NewHomeDB["house/$houseId/visited", Boolean.serializer()] = visited
    return "🏠 ${if (visited) "vista" else "da vedere"}!"
}

// Reviews utils

private fun saveReview(houseId: String, userName: String, review: Review) {
    NewHomeDB["review/$houseId/$userName", Review.serializer()] = review
}

private fun saveReviewVote(houseId: String, userName: String, vote: Int) {
    NewHomeDB["review/$houseId/$userName/vote", Int.serializer()] = vote
}

private fun getReviewsByHouse(houseId: String) = NewHomeDB["review/$houseId", NAME_REVIEW_SERIALIZER].orEmpty()

private fun getReviewsMap(): Map<String, Map<String, Review>> {
    return NewHomeDB["review", MapSerializer(String.serializer(), NAME_REVIEW_SERIALIZER)].orEmpty()
}


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
    return chunked(10).map {
        it.joinToString("\n\n") { (house, reviewsMap) ->
            house.descShort(reviewsMap, showTags = false)
        }
    }
}

private fun getHousesWithReviewsStrings(predicate: (Pair<House, Map<String, Review>>) -> Boolean) =
    getHousesWithReviews().filter(predicate).sorted().toStringList()