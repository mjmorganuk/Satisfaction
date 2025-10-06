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
    namespace = "com.example.satisfaction" // Use assignment '='
    compileSdk = 36                        // Use assignment '='

    defaultConfig {
        applicationId = "com.example.satisfaction" // Use assignment '='
        minSdk = 24                           // Use assignment '='
        targetSdk = 34                        // Use assignment '='
        versionCode = 1                       // Use assignment '='
        versionName = "1.0"                   // Use assignment '='
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.15"
//    }

    kotlinOptions {
        jvmTarget = "1.8" // Or your desired Java version, ensure it matches compileOptions
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    val room_version = "2.8.1"
    implementation("com.google.android.material:material:1.12.0") // Or the latest version
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
    implementation("androidx.room:room-runtime:${room_version}")
    kapt("androidx.room:room-compiler:${room_version}")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Optional (for preview/debug)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.2")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
}
