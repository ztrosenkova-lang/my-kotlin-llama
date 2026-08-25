import java.net.URL
import java.net.HttpURLConnection

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
            // useLegacyPackaging = true
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

    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(files("libs/sherpa-onnx-1.13.6.aar"))

    implementation(project(":llamaCpp"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

val libsDir = file("$projectDir/libs")
val sherpaAarFile = file("$libsDir/sherpa-onnx-1.13.6.aar")
val sherpaAarUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.13.6/sherpa-onnx-1.13.6.aar"

val ttsModelDir = file("$projectDir/src/main/assets/tts-model")
val ttsModelArchive = file("${layout.buildDirectory.get().asFile}/tts-model/vits-piper-ru_RU-ruslan-medium.tar.bz2")
val ttsModelUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-ru_RU-ruslan-medium.tar.bz2"

tasks.register("downloadSherpaAar") {
    doLast {
        if (sherpaAarFile.exists() && sherpaAarFile.length() > 1000000) {
            println("Sherpa AAR already exists. Skipping download.")
            return@doLast
        }

        println("Downloading Sherpa AAR from $sherpaAarUrl")
        libsDir.mkdirs()

        val connection = URL(sherpaAarUrl).openConnection() as HttpURLConnection
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

        if (sherpaAarFile.length() < 1000000) {
            throw GradleException("Downloaded AAR is too small (${sherpaAarFile.length()} bytes). Download failed.")
        }
    }
}

tasks.register("downloadTtsModel") {
    doLast {
        val modelFile = File(ttsModelDir, "ru_RU-ruslan-medium.onnx")
        val tokensFile = File(ttsModelDir, "tokens.txt")
        val espeakDataDir = File(ttsModelDir, "espeak-ng-data")

        if (modelFile.exists() && tokensFile.exists() && espeakDataDir.exists()) {
            println("TTS model already exists. Skipping download.")
            return@doLast
        }

        println("Downloading TTS model from $ttsModelUrl")
        ttsModelArchive.parentFile.mkdirs()

        val connection = URL(ttsModelUrl).openConnection() as HttpURLConnection
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

        val subDirs = ttsModelDir.listFiles()?.filter { it.isDirectory }
        subDirs?.forEach { subDir ->
            subDir.listFiles()?.forEach { file ->
                val target = File(ttsModelDir, file.name)
                if (file.isDirectory) {
                    file.copyRecursively(target, overwrite = true)
                } else {
                    file.copyTo(target, overwrite = true)
                    file.delete()
                }
            }
            subDir.deleteRecursively()
        }
        println("Files moved to root of ttsModelDir")

        ttsModelArchive.delete()
        println("Archive deleted.")
    }
}

tasks.register("checkTtsModel") {
    dependsOn("downloadTtsModel")
    doLast {
        println("=== Files in ${ttsModelDir.absolutePath} ===")
        ttsModelDir.walkTopDown().forEach { file ->
            println("${file.relativeTo(ttsModelDir)} ${if (file.isDirectory) "(dir)" else "(file)"}")
        }
        println("=== End of files ===")
        
        val modelFile = File(ttsModelDir, "ru_RU-ruslan-medium.onnx")
        val tokensFile = File(ttsModelDir, "tokens.txt")
        val espeakDataDir = File(ttsModelDir, "espeak-ng-data")
        
        println("modelFile exists: ${modelFile.exists()}")
        println("tokensFile exists: ${tokensFile.exists()}")
        println("espeakDataDir exists: ${espeakDataDir.exists()}")
        
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
