plugins {
    val libs = libs
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.gms.google.services)
}

android {
    namespace = "cz.jaro.rozvrh"
    compileSdk = 33
    buildToolsVersion = "31.0.0"

    defaultConfig {
        applicationId = "cz.jaro.rozvrh"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.5.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
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
        freeCompilerArgs += "-Xcontext-receivers"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0-alpha02"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.androidx.work)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.bundles.androidx.jetpack.compose)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.permissions)
    implementation(libs.bundles.androidx.jetpack.glance)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.jsoup)
    implementation(libs.bundles.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
