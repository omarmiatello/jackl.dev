package com.github.jacklt.gae.ktor.tg.feature.home

import com.github.jacklt.gae.ktor.tg.appengine.FirebaseDatabaseApi
import com.github.jacklt.gae.ktor.tg.appengine.fireMap
import feature.home.Product
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer


object NoExpDB : FirebaseDatabaseApi() {
    override val basePath = "https://noexp-for-home.firebaseio.com/"
    override val devRules = true

    var home by fireMap(MapSerializer(String.serializer(), Product.serializer()), useCache = false)
}