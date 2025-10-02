import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
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
}