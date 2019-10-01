package com.github.jacklt.gae.ktor.tg.appengine

import com.google.appengine.api.utils.SystemProperty

val isAppEngine
    get() = isAppEngineProduction || isAppEngineDevelopment

val isAppEngineProduction
    get() = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production

val isAppEngineDevelopment
    get() = SystemProperty.environment.value() == SystemProperty.Environment.Value.Development