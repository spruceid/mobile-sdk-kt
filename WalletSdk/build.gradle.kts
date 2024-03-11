plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    id("signing")
    id("com.gradleup.nmcp")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/spruceid/wallet-sdk-kt")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        // Creates a Maven publication called "release".
        create<MavenPublication>("release") {
            groupId = "com.spruceid.wallet.sdk"
            artifactId = "walletsdk"
            version = System.getenv("VERSION")

            afterEvaluate {
                from(components["release"])
            }

            pom {
                packaging = "aar"
                name.set("walletsdk")
                description.set("Android SpruceID Wallet SDK")
                url.set("https://github.com/spruceid/wallet-sdk-kt")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Spruce Systems, Inc.")
                        email.set("hello@spruceid.com")
                    }
                }
                scm {
                    url.set(pom.url.get())
                    connection.set("scm:git:${url.get()}.git")
                    developerConnection.set("scm:git:${url.get()}.git")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}

nmcp {
    afterEvaluate {
        publish("release") {
            username = System.getenv("MAVEN_USERNAME")
            password = System.getenv("MAVEN_PASSWORD")
            publicationType = "AUTOMATIC"
        }
    }
}


android {
    namespace = "com.spruceid.walletsdk"
    compileSdk = 33

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api("com.spruceid.wallet.sdk.rs:walletsdkrs:0.0.25")
    implementation("com.android.support:appcompat-v7:28.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}
