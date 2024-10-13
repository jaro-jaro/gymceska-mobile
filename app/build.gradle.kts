plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "cz.jaro.rozvrh"
    compileSdk = 34

    defaultConfig {
        applicationId = "cz.jaro.rozvrh"
        minSdk = 26
        targetSdk = 34
        versionCode = 39
        versionName = "2.5.0-alpha.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xcontext-receivers"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.bundles.androidx.jetpack.compose)
    implementation(libs.bundles.androidx.jetpack.glance)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.config)
    implementation(libs.bundles.ksoup)
    implementation(libs.bundles.koin)
    implementation(libs.androidx.adaptive.android)
    implementation(libs.semver)
    implementation(libs.kotlinx.datetime)
    implementation(libs.filekit.core)
    implementation(libs.multiplatform.settings)
    implementation(libs.multiplatform.settings.coroutines)
}
