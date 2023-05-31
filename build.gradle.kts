plugins {
    val libs = libs
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.gms.google.services) apply false
    alias(libs.plugins.ksp) apply false
}
repositories {
    google()
    mavenCentral()
}
