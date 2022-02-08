package com.github.omarmiatello.jackldev.utils

fun readResourceFile(filename: String): String =
    filename.javaClass.classLoader.getResourceAsStream(filename)!!.use { it.reader().readText() }

fun readSecret(name: String): String = try {
    System.getenv(name.uppercase().replace('.', '_')) ?: readResourceFile(filename = "config/$name")
} catch (e: Exception) {
    error("Missing '$name' secret. Please check ENV variable or resources/config/$name")
}
