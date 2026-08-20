import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.nehuatl.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.nehuatl.sample"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
        androidResources {
            noCompress.addAll(listOf("bin", "gguf", "txt", "onnx", "json"))
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Extended Material Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Sherpa-ONNX для офлайн TTS (AAR скачивается автоматически)
    implementation(files("libs/sherpa-onnx-android-1.12.12.aar"))

    // Llama.cpp - Local module reference (оставляем для локального ИИ)
    implementation(project(":llamaCpp"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// ============================================================
// ЗАДАЧА ДЛЯ СКАЧИВАНИЯ МОДЕЛИ TTS И БИБЛИОТЕКИ ПРИ СБОРКЕ
// ============================================================
val ttsModelDir = file("$projectDir/src/main/assets/tts-model")
val ttsModelArchive = file("$buildDir/tts-model/vits-piper-ru_RU-denis-medium.tar.bz2")
val ttsModelUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-ru_RU-denis-medium.tar.bz2"

val libsDir = file("$projectDir/libs")
val sherpaAarFile = file("$libsDir/sherpa-onnx-android-1.12.12.aar")
val sherpaAarUrl = "https://k2-fsa.github.io/sherpa/onnx/maven_repo/com/k2fsa/sherpa/onnx/sherpa-onnx-android/1.12.12/sherpa-onnx-android-1.12.12.aar"

tasks.register("downloadSherpaAar") {
    doLast {
        if (sherpaAarFile.exists() && sherpaAarFile.length() > 100000) {
            println("Sherpa AAR already exists. Skipping download.")
            return@doLast
        }

        println("Downloading Sherpa AAR from $sherpaAarUrl")
        libsDir.mkdirs()

        val connection = URL(sherpaAarUrl).openConnection() as java.net.HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 60000
        connection.readTimeout = 300000

        connection.inputStream.use { input ->
            sherpaAarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        connection.disconnect()

        println("Sherpa AAR downloaded to $sherpaAarFile (${sherpaAarFile.length()} bytes)")

        if (sherpaAarFile.length() < 100000) {
            throw GradleException("Downloaded AAR is too small (${sherpaAarFile.length()} bytes). Download failed.")
        }
    }
}

tasks.register("downloadTtsModel") {
    doLast {
        val modelFile = File(ttsModelDir, "ru_RU-denis-medium.onnx")
        val tokensFile = File(ttsModelDir, "tokens.txt")
        val espeakDataDir = File(ttsModelDir, "espeak-ng-data")

        if (modelFile.exists() && tokensFile.exists() && espeakDataDir.exists()) {
            println("TTS model already exists. Skipping download.")
            return@doLast
        }

        println("Downloading TTS model from $ttsModelUrl")
        ttsModelArchive.parentFile.mkdirs()

        val connection = URL(ttsModelUrl).openConnection() as java.net.HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 60000
        connection.readTimeout = 300000

        connection.inputStream.use { input ->
            ttsModelArchive.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        connection.disconnect()

        println("TTS model downloaded to $ttsModelArchive (${ttsModelArchive.length()} bytes)")

        if (ttsModelArchive.length() < 1000000) {
            throw GradleException("Downloaded file is too small (${ttsModelArchive.length()} bytes). Download failed.")
        }

        println("Extracting TTS model...")
        ttsModelDir.mkdirs()
        val process = ProcessBuilder(
            "tar", "xjf", ttsModelArchive.absolutePath,
            "-C", ttsModelDir.absolutePath,
            "--strip-components=1"
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            ttsModelArchive.delete()
            throw GradleException("Failed to extract TTS model: $output")
        }
        println("TTS model extracted to $ttsModelDir")

        val extractedDir = File(ttsModelDir, "vits-piper-ru_RU-denis-medium")
        if (extractedDir.exists() && extractedDir.isDirectory) {
            extractedDir.listFiles()?.forEach { file ->
                val target = File(ttsModelDir, file.name)
                file.copyTo(target, overwrite = true)
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
            extractedDir.deleteRecursively()
            println("Files moved to root of ttsModelDir")
        }

        ttsModelArchive.delete()
        println("Archive deleted.")
    }
}

tasks.register("checkTtsModel") {
    dependsOn("downloadTtsModel")
    doLast {
        val modelFile = File(ttsModelDir, "ru_RU-denis-medium.onnx")
        val tokensFile = File(ttsModelDir, "tokens.txt")
        val espeakDataDir = File(ttsModelDir, "espeak-ng-data")
        if (!modelFile.exists() || !tokensFile.exists() || !espeakDataDir.exists()) {
            throw GradleException("TTS model files are missing. Download failed.")
        }
        println("TTS model files verified.")
    }
}

tasks.named("preBuild") {
    dependsOn("downloadSherpaAar")
    dependsOn("checkTtsModel")
}
