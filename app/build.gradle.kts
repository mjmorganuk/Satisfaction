plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)   // Changed from id 'org.jetbrains.kotlin.android'
    alias(libs.plugins.compose.compiler)
    // For kapt, if it's not in your libs.versions.toml, you'd keep it as is or add it there.
    // Assuming it's not, or you prefer to declare it directly:
    id("kotlin-kapt")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.satisfactionsurvey" // Use assignment '='
    compileSdk = 36                        // Use assignment '='

    defaultConfig {
        applicationId = "com.example.satisfactionsurvey" // Use assignment '='
        minSdk = 24                           // Use assignment '='
        targetSdk = 34                        // Use assignment '='
        versionCode = 1                       // Use assignment '='
        versionName = "1.0"                   // Use assignment '='
    }

    packagingOptions {
        // Excludes files with the specified path from the APK
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE.md,LICENSE-notice.md}"
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.15"
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.androidx.navigation.compose)
    val roomversion = "2.8.1"
    implementation("com.google.android.material:material:1.13.0") // Or the latest version
    // ... other dependencies
    // Jetpack Compose
    // Jetpack Compose - Use the BOM
    implementation(platform(libs.androidx.compose.bom)) // Add this line
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics) // Good to include for graphics
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)


    // Room
    implementation("androidx.room:room-runtime:${roomversion}")
    kapt("androidx.room:room-compiler:${roomversion}")
    annotationProcessor("androidx.room:room-compiler:$roomversion")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomversion")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Optional (for preview/debug)
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.3")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:1.9.3")
    // Add MockK for testing and previews
    // Use debugImplementation to make it available for @Preview but not in release builds
    debugImplementation("io.mockk:mockk-android:1.14.6")
    // If the above doesn't work for previews, you can use `implementation`
    // implementation "io.mockk:mockk-android:1.13.8"
}
