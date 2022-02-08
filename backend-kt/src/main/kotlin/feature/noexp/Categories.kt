package com.github.omarmiatello.jackldev.feature.noexp

import com.github.omarmiatello.jackldev.utils.json
import com.github.omarmiatello.jackldev.utils.readResourceFile
import com.github.omarmiatello.jackldev.utils.readSecret
import com.github.omarmiatello.noexp.Category
import kotlinx.serialization.builtins.ListSerializer

val allCategories by lazy {
    json.decodeFromString(
        ListSerializer(Category.serializer()),
        readSecret("parsed-categories.json")
    )
}

