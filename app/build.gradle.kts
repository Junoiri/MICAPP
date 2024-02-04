plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

}

android {
    compileSdk = 34


    defaultConfig {
        applicationId = "com.example.micapp"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.micapp"
    }

    namespace = "com.example.micapp"

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
    }
}

dependencies {
    implementation("com.facebook.android:facebook-android-sdk:16.3.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // GIF drawable
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.28")

    // OAuth2 for GitHub
    implementation("net.openid:appauth:0.11.1")

    // FirebaseUI Auth
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Microsoft Identity
    implementation("com.microsoft.identity.client:msal:4.10.0")
    implementation("com.microsoft.identity:common:16.2.0")
    implementation("com.microsoft.device.display:display-mask:0.3.0")

    // Material for bottom panel of Calendar Activity
    implementation("com.google.android.material:material:1.12.0-alpha03")

    implementation("com.sothree.slidinguppanel:library:3.4.0")

    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation("com.google.firebase:firebase-storage:20.3.0")


}

