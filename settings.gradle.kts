pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Репозиторий для библиотек Next-gen Kaldi (sherpa-onnx)
        maven { url = java.net.URI("https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models") }
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "KotlinLlamaCpp"
include(":app", ":llamaCpp")
