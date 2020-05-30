package com.github.omarmiatello.jackldev.feature.supermarket

import com.github.omarmiatello.jackldev.config.AppConfig
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.builtins.list
import com.github.omarmiatello.jackldev.utils.json
import com.github.omarmiatello.jackldev.utils.toJsonPretty
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger


object EsselungaClient {
    private val config = AppConfig.default.esselunga
    private const val TIMEOUT_MS = 10_000

    fun getAvailableSlots(): List<Slot> {
        val keys = retryNotNull { esselungaKeys() }
            ?: return emptyList()
        val response =
            retryNotNull {
                val call =
                    get("https://www.esselungaacasa.it/ecommerce/resources/auth/slot/status") {
                        setRequestProperty("Cookie", "JSESSIONID=${keys.jSessionId}")
                        setRequestProperty("X-XSRF-TOKEN", keys.xsrfEcomToken)
                    }
                val body = String(call.inputStream.readBytes())
                try {
                    json.parse(
                        SlotResponse.serializer(),
                        body
                    )
                } catch (e: Exception) {
                    null
                }
            }
        response ?: return emptyList()
        log(
            response.slots.toJsonPretty(
                Slot.serializer().list
            )
        )
        return response.slots.filter { it.status != "DISABLED" && it.viewStatus != "ESAURITA" }
    }

    private fun esselungaKeys(): Keys? {
        val loginStep1 =
            postFormUrlEncoded(
                url = "https://www.esselunga.it/area-utenti/loginExt",
                param = mapOf(
                    "username" to config.username,
                    "password" to config.password,
                    "appName" to "esselungaEcommerce",
                    "daru" to "https://www.esselungaacasa.it:443/ecommerce/nav/auth/supermercato/home.html?",
                    "dare" to "https://www.esselunga.it/area-utenti/applicationCheck?appName=esselungaEcommerce&daru=https://www.esselungaacasa.it:443/ecommerce/nav/auth/supermercato/home.html?utm_campaign=ESS_aon&utm_source=sito_esselunga&utm_term=top_dx&utm_content=quicklink_esselunga_a_casa&utm_medium=internal&loginType=light"
                )
            )

        val loginStep2 =
            get(loginStep1.headerFields["location"]!!.first()) {
                val cookie = loginStep1.headerFields["set-cookie"]!!.joinToString(", ")
                setRequestProperty("Cookie", cookie)
            }

        val loginLocation3 = loginStep2.headerFields["location"]?.first()
        return if (loginLocation3 != null) {
            val loginStep3 =
                get(
                    loginLocation3
                ) {
                    val cookie = loginStep2.headerFields["set-cookie"]!!.joinToString(", ")
                    log("cookie: $cookie")
                    setRequestProperty("Cookie", cookie)
                }

            val cookies = loginStep3.headerFields["set-cookie"]!!.toCookieMap()

            Keys(
                jSessionId = cookies.getValue("JSESSIONID")!!,
                xsrfEcomToken = cookies.getValue("XSRF-ECOM-TOKEN")!!
            )
        } else null
    }

    private fun List<String>.toCookieMap() = flatMap {
        it.split(";").map { it.split("=").let { it[0] to it.getOrNull(1) } }
    }.toMap()


    private fun postFormUrlEncoded(url: String, param: Map<String, String>) =
        post(
            url,
            "application/x-www-form-urlencoded",
            param.entries.joinToString("&") { "${it.key}=${it.value.encodeURLParameter()}" })

    private fun get(
        url: String,
        _contentType: String = "application/x-www-form-urlencoded",
        block: HttpURLConnection.() -> Unit = {}
    ): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout =
                TIMEOUT_MS
            readTimeout =
                TIMEOUT_MS
            doOutput = true
            requestMethod = "GET"
            setRequestProperty("Content-Type", "$_contentType; charset=UTF-8")
            setRequestProperty("Accept", "*/*")
            block(this)
            instanceFollowRedirects = false
        }
    }

    private fun post(
        url: String,
        _contentType: String = "application/x-www-form-urlencoded",
        param: String,
        block: HttpURLConnection.() -> Unit = {}
    ): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout =
                TIMEOUT_MS
            readTimeout =
                TIMEOUT_MS
            doOutput = true
            requestMethod = "POST"
            setRequestProperty("Content-Type", "$_contentType; charset=UTF-8")
            block(this)
            instanceFollowRedirects = false
            val writer = OutputStreamWriter(outputStream)
            writer.write(param)
            writer.close()
        }
    }

    fun <T : Any?> retryNotNull(max: Int = 3, block: () -> T?): T? {
        (1..max).forEach {
            val res = block()
            if (res != null) return res
        }
        return null
    }

    fun log(msg: String) = Logger.getLogger("ok").log(Level.WARNING, msg)

    class Keys(val jSessionId: String, val xsrfEcomToken: String)
}