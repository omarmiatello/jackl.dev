package com.github.omarmiatello.jackldev.utils

import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private val factory = UrlFetchTransport.getDefaultInstance().createRequestFactory()
fun jsoupGet(path: String): Document = factory
    .buildGetRequest(GenericUrl(path))
    .execute()
    .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }
    .let { Jsoup.parse(it.parseAsString()) }