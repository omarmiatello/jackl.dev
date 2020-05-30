package com.github.omarmiatello.jackldev.service.telegram

import com.github.omarmiatello.jackldev.service.httpClient
import com.github.omarmiatello.telegram.TelegramClient
import com.github.omarmiatello.jackldev.config.AppConfig

private val config = AppConfig.default.telegram
val telegramApi = TelegramClient(config.apiKey, httpClient)