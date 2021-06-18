plugins {
    kotlin("multiplatform") version "1.4.30"
    id("convention.publication")
    id("com.android.library")
}

val kotlinVersion = "1.4.30"
val coroutinesVersion = "1.4.3-native-mt"


object libVersion{
    private val major = 0
    private val minor = 1
    private val patch = 5
    val num = 10_000 * major + 100 * minor + 1 * patch
    val text = "$major.$minor.$patch"
}


group = "io.github.september669"
version = libVersion.text


repositories {
    gradlePluginPortal() // To use 'maven-publish' and 'signing' plugins in our own plugin
    google()
    jcenter()
    mavenCentral()
}

kotlin {


    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }

    android {
        publishLibraryVariants("release", "debug")
    }

    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.github.september669:AnkoLogger:0.2.5")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:atomicfu:0.15.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.3.1")
            }
        }
        val androidTest by getting

        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(20)
        targetSdkVersion(30)
        versionCode = libVersion.num
        versionName = libVersion.text
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}