package com.github.omarmiatello.jackldev.feature.noexp

import com.github.omarmiatello.jackldev.utils.json
import com.github.omarmiatello.noexp.Category
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

fun readText(filename: String) =
    filename.javaClass.classLoader.getResourceAsStream(filename)!!.use { it.reader().readText() }

val allCategories by lazy {
    json.parse(
        ListSerializer(Category.serializer()),
        readText("config/parsed-categories.json")
    )
}

