import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

publishing {
    repositories {
        maven {
            url = uri("")
        }
    }

    publications {
        // Creates a Maven publication called "release".
        create<MavenPublication>("release") {
            afterEvaluate {
                groupId = "com.spruceid"
                artifactId = "walletsdk"
                version = "0.0.1"
                artifact(tasks.getByName("bundleReleaseAar"))
            }
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
}

dependencies {
    api("com.spruceid.wallet.sdk.rs:walletsdkrs:0.0.6")
    implementation("com.android.support:appcompat-v7:28.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}