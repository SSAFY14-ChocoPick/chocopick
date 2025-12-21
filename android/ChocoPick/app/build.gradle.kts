plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.ssafy.chocopick"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ssafy.chocopick"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }

    viewBinding{
        enable = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("com.google.firebase:firebase-database")

    // ViewModelScope 제공 (viewModelScope)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // repeatOnLifecycle / lifecycleScope 등 (UI에서 Flow collect 안정적으로)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ViewModel scope에 코루틴 쓰려고 (이미 있으면 생략)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.github.bumptech.glide:glide:5.0.5")

    implementation("com.google.code.gson:gson:2.11.0")

    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}