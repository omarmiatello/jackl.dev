package utils

import service.telegram.TelegramApi


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
                val messageId: Int = TelegramApi.sendMessage(
                    chatId = chatId,
                    text = msg,
                    parseMode = TelegramApi.ParseMode.NONE,
                    disableWebPagePreview = true
                    //button = listOf(listOf(InlineKeyboardButton("Meetup", event.meetupLink!!)))
                ).result.message_id
                idsMsgSends += messageId
                response.appendln("NEW $chatId - messageId: $messageId > ${msg.replace('\n', ' ').take(140)}...")
            }
        }
    }
}