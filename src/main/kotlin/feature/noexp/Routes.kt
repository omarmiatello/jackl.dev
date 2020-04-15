package feature.noexp

import com.github.omarmiatello.jackldev.feature.noexp.expireMessage
import config.MyConfig
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import service.telegram.TelegramApi
import java.util.concurrent.TimeUnit

fun Route.webhookNoExp() {
    get("telegram/expire") {
        val msg = expireMessage()
        TelegramApi.sendMessage(MyConfig.chat_case, msg, TelegramApi.ParseMode.HTML)
        call.respondText(msg)
    }
}
