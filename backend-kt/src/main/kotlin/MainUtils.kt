package com.github.omarmiatello.jackldev

import com.github.omarmiatello.telegram.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

val Exception.stackTraceString get() = StringWriter().also { printStackTrace(PrintWriter(it)) }.toString()

suspend fun startApp() {
    while (true) {
        val input = readLine().orEmpty()
        val message = Message(
            message_id = 0,
            from = User(0, false, "me"),
            date = Date().time,
            chat = Chat(0, "local_chat"),
            text = input
        ).toAppResponse()
        println(message)
    }
}

suspend fun Message.toAppResponse(): String {
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
