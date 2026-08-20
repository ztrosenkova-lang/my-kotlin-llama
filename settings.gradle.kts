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
        maven { url = java.net.URI("https://k2-fsa.github.io/sherpa/onnx/maven_repo/") }
    }
}

rootProject.name = "KotlinLlamaCpp"
include(":app", ":llamaCpp")
