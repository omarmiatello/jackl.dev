package feature.home

import com.github.omarmiatello.jackldev.service.FirebaseDatabaseApi
import com.github.omarmiatello.jackldev.service.fireMap
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer


object NoExpDB : FirebaseDatabaseApi(
    basePath = "https://noexp-for-home.firebaseio.com/",
    credentialsFile = "noexp-credentials.json"
) {
    var home by fireMap(MapSerializer(String.serializer(), Product.serializer()))
}