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
            url = uri("https://maven.pkg.github.com/spruceid/mobile-sdk-kt")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        mavenLocal()
    }
    publications {
        // This command must be commented on when releasing a new version.
        // create<MavenPublication>("debug") {
        //    groupId = "com.spruceid.mobile.sdk"
        //    artifactId = "mobilesdk"
        //    version = System.getenv("VERSION")
        //  afterEvaluate { from(components["release"]) }
        //}

        // Creates a Maven publication called "release".
        create<MavenPublication>("release") {
            groupId = "com.spruceid.mobile.sdk"
            artifactId = "mobilesdk"
            version = System.getenv("VERSION")

            afterEvaluate { from(components["release"]) }

            pom {
                packaging = "aar"
                name.set("mobilesdk")
                description.set("Android SpruceID Mobile SDK")
                url.set("https://github.com/spruceid/mobile-sdk-kt")
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
    namespace = "com.spruceid.mobile.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = "1.8" }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api("com.spruceid.mobile.sdk.rs:mobilesdkrs:0.8.5")
    //noinspection GradleCompatible
    implementation("com.android.support:appcompat-v7:28.0.0")
    /* Begin UI dependencies */
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("androidx.camera:camera-mlkit-vision:1.3.0-alpha06")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    /* End UI dependencies */
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20230618")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}
