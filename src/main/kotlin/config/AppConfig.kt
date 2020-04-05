package config

@DslMarker
annotation class AppConfigMarker

@AppConfigMarker
abstract class AppConfig(
    val telegram: Telegram,
    val esselunga: Esselunga
) {

    @AppConfigMarker
    class Telegram(
        var apiKey: String
    )

    @AppConfigMarker
    class Esselunga(
        var username: String,
        var password: String
    )

    companion object {
        val default: AppConfig get() = MyConfig
    }
}
