package com.github.jacklt.gae.ktor.tg.data

import com.github.jacklt.gae.ktor.tg.appengine.FirebaseDatabaseApi
import com.github.jacklt.gae.ktor.tg.appengine.fireMap
import com.github.jacklt.gae.ktor.tg.appengine.fireProperty
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer

// :: Firebase DB - Simplified mode ::
// - Read example:
//      FireDB.testString
// - Write example:
//      FireDB.testMap = mapOf("name" to "Vale")
//      FireDB.testString = "test 👍"
//      FireDB.lastMessage = message
//
// :: Firebase DB - Advanced mode ::
// get: read a path
//      FireDB["testString", String.serializer()]
// set: write (and replace) the content of a path
//      FireDB["testMap", (String.serializer() to String.serializer()).map] = mapOf("name" to "Vale")
//      FireDB["testString", String.serializer()] = "test 👍"
//      FireDB["lastMessage", Message.serializer()] = message
// addItem: create a new ID and add an item to path!
//      FireDB.addItem("pathExample", "test1", String.serializer())
//      FireDB.addItem("pathExample", "test2", String.serializer())
//      FireDB.addItem("pathExample", "test3", String.serializer())
// update: update only some children of this path
//      FireDB.update("pathExample", mapOf("campo2" to "2"), String.serializer())
// delete: delete a path
//      FireDB.delete("pathExample")

object FireDB : FirebaseDatabaseApi() {
    override val basePath = "https://jutils-3f869.firebaseio.com/"

    var testString by fireProperty(String.serializer())
    var lastMessage by fireProperty(Message.serializer())
    var testMap by fireMap(MapSerializer(String.serializer(), String.serializer()))
}