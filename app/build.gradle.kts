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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
            noCompress.addAll(listOf("bin", "gguf", "onnx", "txt"))
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

    // Vosk
    implementation("com.alphacephei:vosk-android:0.3.47")

    // ONNX Runtime
    implementation(libs.onnx.runtime)

    // Llama.cpp - Local module reference
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
// ЗАДАЧА ДЛЯ СКАЧИВАНИЯ МОДЕЛИ TTS ПРИ СБОРКЕ
// ============================================================

val ttsModelDir = file("src/main/assets/tts-model")
val ttsModelFile = file("src/main/assets/tts-model/ru_RU-robot-medium.onnx")
val ttsModelUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ru/ru_RU/robot-medium/ru_RU-robot-medium.onnx"

tasks.register("downloadTtsModel") {
    group = "download"
    description = "Скачивает модель TTS из Hugging Face, если она отсутствует в assets"
    onlyIf { !ttsModelFile.exists() }
    doLast {
        println("⏳ Скачивание модели TTS...")
        ttsModelDir.mkdirs()
        try {
            val url = URI(ttsModelUrl).toURL()
            url.openStream().use { inputStream ->
                ttsModelFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            println("✅ Модель TTS успешно скачана: ${ttsModelFile.absolutePath}")
        } catch (e: Exception) {
            println("❌ Ошибка скачивания модели TTS: ${e.message}")
            throw e
        }
    }
}

// Автоматический запуск скачивания перед сборкой
tasks.named("preBuild") {
    dependsOn("downloadTtsModel")
}

// Проверка размера модели после скачивания (для отладки)
tasks.register("checkTtsModel") {
    group = "verification"
    description = "Проверяет, что модель TTS существует и имеет корректный размер"
    doLast {
        if (ttsModelFile.exists()) {
            val sizeMB = ttsModelFile.length() / (1024 * 1024)
            println("✅ Модель TTS найдена. Размер: $sizeMB МБ")
            if (sizeMB < 10) {
                println("⚠️ Внимание: модель слишком маленькая ($sizeMB МБ). Возможно, файл повреждён.")
            }
        } else {
            println("❌ Модель TTS не найдена")
        }
    }
}
