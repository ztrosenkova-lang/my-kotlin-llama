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
        maven { url = java.net.URI("https://jitpack.io") }
        // Репозиторий для sherpa-onnx (Maven Central уже содержит нужный артефакт)
    }
}

rootProject.name = "KotlinLlamaCpp"
include(":app", ":llamaCpp")
