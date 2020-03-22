package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.telegram.Chat
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.appengine.telegram.User
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

val Exception.stackTraceString get() = StringWriter().also { printStackTrace(PrintWriter(it)) }.toString()

fun startApp() {
    while (true) {
        val input = readLine().orEmpty()
        val message = Message(
            message_id = 0,
            from = User(0, false, "me"),
            date = Date().time.toInt(),
            chat = Chat(0, "local_chat"),
            text = input
        ).toAppResponse()
        println(message)
    }
}

fun Message.toAppResponse(): String {
    return try {
        myApp(this)
    } catch (e: Exception) {
        Logger.getLogger("ok").log(Level.WARNING, "errore", e)
        "C'è stato un errore: ```\n${e.stackTraceString}\n```"
    }.ifBlank {
        "Non so cosa rispondere... 🙄"
    }
}

fun show(prefix: String, field: String?) = if (field.isNullOrEmpty()) "" else "\n|$prefix$field"

val emojiAnimals = listOf(
    "🙈", "🙉", "🙊", "🐵", "🐒", "🦍", "🐶", "🐕", "🐩", "🐺", "🦊", "🦝", "🐱",
    "🐈", "🦁", "🐯", "🐅", "🐆", "🐴", "🐎", "🦄", "🦓", "🦌", "🐮", "🐂", "🐃", "🐄", "🐷", "🐖", "🐗", "🐽", "🐏",
    "🐑", "🐐", "🐪", "🐫", "🦙", "🦒", "🐘", "🦏", "🦛", "🐭", "🐁", "🐀", "🐹", "🐰", "🐇", "🐿", "🦔", "🦇", "🐻",
    "🐨", "🐼", "🦘", "🦡", "🐾", "🦃", "🐔", "🐓", "🐣", "🐤", "🐥", "🐦", "🐧", "🕊", "🦅", "🦆", "🦢", "🦉", "🦚",
    "🦜", "🐸", "🐊", "🐢", "🦎", "🐍", "🐲", "🐉", "🦕", "🦖", "🐳", "🐋", "🐬", "🐟", "🐠", "🐡", "🦈", "🐙", "🐚",
    "🐌", "🦋", "🐛", "🐜", "🐝", "🐞", "🦗", "🕷", "🕸", "🦂", "🦟"
)
