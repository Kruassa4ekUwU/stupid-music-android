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
        // Нужен для NewPipe Extractor
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "StupidMusic"
include(":app")
