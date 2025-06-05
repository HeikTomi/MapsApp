plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
    // Apply the secrets-gradle-plugin at the top level
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("kotlin-kapt")
}

// buildscript block should be at the top level
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // You might still need this if the plugin isn't found through the plugins block
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

android {
    namespace = "com.example.mapsapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mapsapp"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Google Maps SDK for Android
    // Location Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.sqlite:sqlite:2.1.0")
}