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
        // JitPack для sherpa-onnx (работает как для GitHub-репозиториев)
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "KotlinLlamaCpp"
include(":app", ":llamaCpp")
