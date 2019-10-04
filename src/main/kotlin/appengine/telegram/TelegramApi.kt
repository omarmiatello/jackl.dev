package com.github.jacklt.gae.ktor.tg.appengine.telegram

object TelegramApi {
    fun sendMessage(
        chatId: String,
        text: String,
        parseMode: ParseMode = ParseMode.NONE,
        disableWebPagePreview: Boolean = false,
        button: List<List<InlineKeyboardButton>>? = null
    ) = TelegramMethod.sendMessage(
        chat_id = chatId,
        text = text,
        parse_mode = parseMode.str,
        disable_web_page_preview = disableWebPagePreview,
        reply_markup = button?.let { InlineKeyboardMarkup(it) }
    )

    fun editMessageText(
        chatId: String,
        messageId: Int,
        text: String,
        parseMode: ParseMode = ParseMode.NONE,
        disableWebPagePreview: Boolean = false,
        button: List<List<InlineKeyboardButton>>? = null
    ) = TelegramMethod.editMessageText(
        text = text,
        chat_id = chatId,
        message_id = messageId,
        parse_mode = parseMode.str,
        disable_web_page_preview = disableWebPagePreview,
        reply_markup = button?.let { InlineKeyboardMarkup(it) }
    )

    fun forwardMessage(
        chatId: String,
        fromChatId: String,
        messageId: Int,
        disableNotification: Boolean? = null
    ) = TelegramMethod.forwardMessage(
        chat_id = chatId,
        from_chat_id = fromChatId,
        message_id = messageId,
        disable_notification = disableNotification
    )

    fun deleteMessage(chatId: String, messageId: Int) = TelegramMethod.deleteMessage(chatId, messageId)

    enum class ParseMode(val str: String?) {
        NONE(null),
        MARKDOWN("Markdown"),
        HTML("HTML")
    }
}