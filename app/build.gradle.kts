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

    // For Material Components
    implementation(libs.material.v1130)
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

    // Supabase dependencies
    val kotlin_version = "3.1.4"
    val ktor_version = "3.1.2"

    implementation(platform("io.github.jan-tennert.supabase:bom:$kotlin_version"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-android:$ktor_version")
    implementation("io.github.jan-tennert.supabase:storage-kt:$ktor_version")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Networking (Retrofit & OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}