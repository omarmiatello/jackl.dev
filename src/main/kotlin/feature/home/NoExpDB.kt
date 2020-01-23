package com.github.jacklt.gae.ktor.tg.feature.home

import com.github.jacklt.gae.ktor.tg.appengine.FirebaseDatabaseApi
import com.github.jacklt.gae.ktor.tg.appengine.fireMap
import feature.home.Product
import kotlinx.serialization.map
import kotlinx.serialization.serializer


object NoExpDB : FirebaseDatabaseApi() {
    override val basePath = "https://noexp-for-home.firebaseio.com/"

    var home by fireMap((String.serializer() to Product.serializer()).map, useCache = false)
}