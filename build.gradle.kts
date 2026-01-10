plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    kotlin("kapt") version "2.0.20" apply false
}