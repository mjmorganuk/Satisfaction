//buildscript {
//    ext {
//        compose_ui_version = "1.7.2"
//    }
//}
// Top-level build file
// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false

}
