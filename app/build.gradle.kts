import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
}

android {
    namespace = "com.ssba.pantrychef"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ssba.pantrychef"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

/*
 * Code Attribution
 *
 * Purpose:
 *   - This Gradle build configuration defines the Android application setup for the PantryChef app.
 *   - It configures SDK versions, dependencies, and Kotlin/Java compilation targets.
 *   - The dependencies include key Android Jetpack libraries, Firebase (Auth & Firestore), Retrofit for API calls,
 *     Glide for image loading, and Kotlin Coroutines for asynchronous programming.
 *   - Also integrates Google Sign-In, biometrics, and secure credential storage for user authentication.
 *
 * Authors/Technologies Used:
 *   - Android Jetpack Libraries: Android Open Source Project
 *   - Kotlin DSL & Coroutines: JetBrains
 *   - Firebase SDKs: Google Firebase
 *   - Retrofit & OkHttp Logging Interceptor: Square, Inc.
 *   - Glide Image Library: Bumptech
 *   - CircleImageView: Henning Dodenhof
 *
 * References:
 *   - Android Gradle Plugin Documentation: https://developer.android.com/studio/build
 *   - Firebase for Android Setup Guide: https://firebase.google.com/docs/android/setup
 *   - Retrofit Official Docs: https://square.github.io/retrofit/
 *   - Kotlin Coroutines Guide: https://kotlinlang.org/docs/coroutines-overview.html
 *   - Glide GitHub Repository: https://github.com/bumptech/glide
 *   - CircleImageView Repository: https://github.com/hdodenhof/CircleImageView
 */


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // For ViewPager2 to swipe between fragments
    implementation(libs.androidx.viewpager2)
    // For Fragments
    implementation(libs.androidx.fragment.ktx)
    // For ViewModel to share data between fragments and the activity
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // For SplashScreen
    implementation(libs.androidx.core.splashscreen)
    // For Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // For Supabase
    implementation(platform(libs.bom))
    implementation(libs.postgrest.kt)
    implementation(libs.ktor.client.android)
    implementation(libs.storage.kt)
    // Glide for image loading
    implementation(libs.glide)
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    // Google Sign-In
    implementation(libs.play.services.auth)
    // Biometrics
    implementation(libs.androidx.biometric.ktx)
    // For securely storing credentials
    implementation(libs.androidx.security.crypto)
    // Coroutines for asynchronous operations
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    // CircleImageView for profile picture
    implementation(libs.circleimageview)
}