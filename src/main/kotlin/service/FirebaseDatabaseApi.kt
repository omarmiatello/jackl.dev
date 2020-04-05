package com.github.omarmiatello.jackldev.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import utils.json
import utils.parse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class FirebaseDatabaseApi(basePath: String, credentialsFile: String) {
    private val app = FirebaseApp.initializeApp(
        FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(javaClass.classLoader.getResourceAsStream(credentialsFile)))
            .setConnectTimeout(5000)
            .setReadTimeout(5000)
            .setDatabaseUrl(basePath)
            .build(),
        basePath
    )

    val db = FirebaseDatabase.getInstance(app)

    inline operator fun <reified T> get(path: String, deserializer: KSerializer<T>): T? {
        return runBlocking {
            db.getReference(path).toDataSnapshot().toDataObject<T>(deserializer)
        }
    }

    operator fun <T> set(path: String, serializer: KSerializer<T>, obj: T) {
        db.getReference(path).setValueAsync(obj).get()
    }

    fun <T> addItem(path: String, obj: T, serializer: KSerializer<T>) {
        db.getReference(path).push().setValueAsync(obj).get()
    }

    fun <T> update(path: String, map: Map<String, T>, serializer: KSerializer<T>) {
        db.getReference(path).push().updateChildrenAsync(map).get()
    }

    fun delete(path: String) {
        db.getReference(path).removeValueAsync().get()
    }
}

inline fun <reified T> fireProperty(serializer: KSerializer<T>, key: String? = null) = localFireDB(key, serializer)

inline fun <reified T> fireList(serializer: KSerializer<Collection<T>>, key: String? = null) = localFireDB(key, serializer)

inline fun <reified T> fireMap(serializer: KSerializer<Map<String, T>>, key: String? = null) = localFireDB(key, serializer)

inline fun <reified T> localFireDB(key: String?, serializer: KSerializer<T>) =
    object : ReadWriteProperty<FirebaseDatabaseApi, T> {
        override fun getValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>): T {
            return localGet(
                thisRef,
                key ?: property.name,
                serializer
            )
        }

        override fun setValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>, value: T) {
            localSet(thisRef, key ?: property.name, value)
        }
    }

fun <T> localGet(firebaseDatabaseApi: FirebaseDatabaseApi, key: String, deserializer: DeserializationStrategy<T>): T {
    return runBlocking {
        // TODO runBlocking
        firebaseDatabaseApi.db.getReference(key).toDataSnapshot().toDataObject(deserializer)
    }
}

fun <T> DataSnapshot.toDataObject(deserializer: DeserializationStrategy<T>) = Gson().toJson(value).parse(deserializer)

inline fun <reified T> localSet(firebaseDatabaseApi: FirebaseDatabaseApi, key: String, value: T) {
    firebaseDatabaseApi.db.getReference(key).setValueAsync(value).get()
}

suspend fun DatabaseReference.toDataSnapshot(): DataSnapshot = suspendCancellableCoroutine { cont ->
    addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {
            cont.resumeWithException(error!!.toException())
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            cont.resume(snapshot!!)
        }
    })
}