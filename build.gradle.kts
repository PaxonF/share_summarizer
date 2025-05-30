plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // Add any necessary build dependencies here
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}