import org.jetbrains.kotlin.konan.properties.loadProperties


plugins {
    kotlin("multiplatform") version ("1.4.30")
    id("com.android.library")
    id("maven-publish")
}

val kotlinVersion = "1.4.30"
val coroutinesVersion = "1.4.3-native-mt"


object libVersion{
    private val major = 0
    private val minor = 1
    private val patch = 1
    val num = 10_000 * major + 100 * minor + 1 * patch
    val text = "$major.$minor.$patch"
}


group = "org.dda.reduks"
version = libVersion.text


repositories {
    gradlePluginPortal()
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


    val libName = "${project.name}_lib"
    val ios = listOf(iosX64(), iosArm64())
    configure(ios) {
        val main by compilations.getting
        binaries {
            framework {
                baseName = "libName"
            }
        }
    }

    // Create a task to build a fat framework.
    tasks.create("debugFatFramework", org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask::class) {
        // The fat framework must have the same base name as the initial frameworks.
        baseName = libName
        // The default destination directory is '<build directory>/fat-framework'.
        destinationDir = buildDir.resolve("fat-framework/debug")
        // Specify the frameworks to be merged.
        from(
            ios.map { it.binaries.getFramework("DEBUG") }
        )
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.dda.ankoLogger:AnkoLogger:0.2.4")
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
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.3.0")
            }
        }
        val androidTest by getting

        val iosX64Main by getting
        val iosArm64Main by getting {
            dependsOn(iosX64Main)
        }
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


//      Publishing
val (bintrayUser, bintrayPass, bintrayKey) = project.rootProject.file("publish.properties").let {
    it.absolutePath
}.let { path ->
    loadProperties(path)
}.let { prop ->
    val user = prop.getProperty("bintrayUser")
    val pass = prop.getProperty("bintrayPass")
    val key = prop.getProperty("bintrayKey")
    System.err.println("bintray credentials: $user/$pass key: $key")
    listOf(user, pass, key)
}

publishing {
    repositories.maven("https://api.bintray.com/maven/september669/reduks/Reduks/;publish=1;override=1") {
        name = "bintray"

        credentials {
            username = bintrayUser
            password = bintrayKey
        }
    }
}