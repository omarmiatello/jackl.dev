package com.github.omarmiatello.jackldev.utils

import com.github.omarmiatello.jackldev.service.telegram.telegramApi


class TelegramHelper(
    val chatId: String,
    val maxNew: Int = 10
) {
    val response: StringBuilder = StringBuilder()
    var idsMsgSends = emptyList<Int>()
        private set

    suspend fun send(vararg msgs: String) {
        var countNew = 0
        msgs.forEach { msg ->
            if (countNew++ < maxNew) {
                val messageId: Int = telegramApi.sendMessage(
                    chat_id = chatId,
                    text = msg,
                    disable_web_page_preview = true
                ).result.message_id
                idsMsgSends += messageId
                response.appendln("NEW $chatId - messageId: $messageId > ${msg.replace('\n', ' ').take(140)}...")
            }
        }
    }
}