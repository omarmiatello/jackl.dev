package com.github.omarmiatello.jackldev.feature.noexp

import com.github.omarmiatello.jackldev.service.FirebaseDatabaseApi
import com.github.omarmiatello.jackldev.service.fireMap
import com.github.omarmiatello.noexp.NoExpDBModel
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer


object NoExpFireDB : FirebaseDatabaseApi(
    basePath = "https://noexp-for-home.firebaseio.com/",
    credentialsFile = "noexp-credentials.json"
) {
    var home by fireMap(MapSerializer(String.serializer(), NoExpDBModel.ProductDao.serializer()))
}