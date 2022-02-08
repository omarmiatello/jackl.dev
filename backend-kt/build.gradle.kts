import org.gradle.tooling.GradleConnector
import org.jetbrains.kotlin.konan.file.use
import java.util.concurrent.*

plugins {
    application
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"

    // Shadow 5.0.0 requires Gradle 5+. Check the shadow plugin manual if you're using an older version of Gradle.
    // id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    // mavenLocal()
    mavenCentral()
    //jcenter()
    // maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.akuleshov7:ktoml-core:0.2.7")

    implementation("io.ktor:ktor-server-cio:1.6.4")
    // implementation("io.ktor:ktor-server-netty:1.6.4")
    implementation("io.ktor:ktor-server-core:1.6.4")
    implementation("io.ktor:ktor-server-host-common:1.6.4")
    implementation("io.ktor:ktor-server-servlet:1.6.4")
    implementation("io.ktor:ktor-serialization:1.6.4")
    implementation("io.ktor:ktor-html-builder:1.6.4")
    implementation("io.ktor:ktor-client-core:1.6.4")
    implementation("io.ktor:ktor-client-apache:1.6.4")
    implementation("io.ktor:ktor-client-okhttp:1.6.4")
    implementation("io.ktor:ktor-client-json:1.6.4")
    implementation("io.ktor:ktor-client-serialization-jvm:1.6.4")
    implementation("io.ktor:ktor-client-logging-jvm:1.6.4")

    implementation("com.github.omarmiatello.telegram:client:5.3")
    implementation("com.github.omarmiatello.telegram:dataclass:5.3")
    implementation("com.github.omarmiatello.noexp:dataclass:0.5.1")
    implementation("com.github.omarmiatello.noexp:categories-parser:0.5.1")
    implementation("com.github.omarmiatello.noexp:app:0.5.1")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("org.jsoup:jsoup:1.12.1")

    testImplementation("io.ktor:ktor-server-tests:1.6.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

application {
    mainClassName = "com.github.omarmiatello.jackldev.ApplicationKt"
}

// one task that does both the continuous compile and the run
tasks.create("dev") {
    doLast {
        fun fork(task: String, vararg args: String): Future<*> {
            return Executors.newSingleThreadExecutor().submit {
                GradleConnector.newConnector()
                        .forProjectDirectory(project.projectDir)
                        .connect()
                        .use {
                            it.newBuild()
                                    .addArguments(*args)
                                    .setStandardError(System.err)
                                    .setStandardInput(System.`in`)
                                    .setStandardOutput(System.out)
                                    .forTasks(task)
                                    .run()
                        }
            }
        }

        val classesFuture = fork("classes", "-t")
        val runFuture = fork("run")

        classesFuture.get()
        runFuture.get()
    }
}

defaultTasks("dev")
tasks.replace("assemble").dependsOn("installDist")
tasks.create("stage").dependsOn("installDist")
