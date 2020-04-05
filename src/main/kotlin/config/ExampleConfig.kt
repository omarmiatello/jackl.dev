package config

// https://api.telegram.org/botTOKENsetWebhook?url=https://example.com/webhook/telegram
// https://api.telegram.org/botTOKEN/deleteWebhook

object ExampleConfig : AppConfig(
    Telegram(
        apiKey = ""
    ),
    Esselunga(
        username = "",
        password = ""
    )
)