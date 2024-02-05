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
        maven {
            url = uri("https://maven.pkg.github.com/spruceid/wallet-sdk-rs")
            val properties = java.util.Properties().apply {
                load(file("local.properties").reader())
            }

            credentials {
                username = properties.getProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = properties.getProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

rootProject.name = "WalletSdk"
include(":example")
include(":WalletSdk")
