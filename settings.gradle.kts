pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/StarlightC/HBVideoView_Core")
            credentials {
                username = "StarlightC"
                password = "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"
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/StarlightC/HBVideoView_IjkPlayer")
            credentials {
                username = "StarlightC"
                password = "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"
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/StarlightC/HBVideoView_ExoPlayer")
            credentials {
                username = "StarlightC"
                password = "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"
            }
        }
    }
}
rootProject.name = "HBVideoView"
include(":sample")
include(":hbvideoview")