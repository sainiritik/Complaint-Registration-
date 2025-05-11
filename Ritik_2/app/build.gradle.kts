plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.ritik_2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ritik_2"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.ui) // Core UI components
    implementation(libs.material3) // Material Design 3
    implementation(libs.androidx.activity.compose.v1xx) // For Compose Activity
    implementation(libs.androidx.lifecycle.runtime.compose) // Lifecycle-aware components
    implementation(libs.ui.tooling.preview) // Preview support
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.accompanist.coil)
    implementation(libs.coil.compose)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx.v2514)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.material3.v120)
    implementation(libs.androidx.lifecycle.runtime.compose.v261)
    implementation(libs.jcifs.ng)
    debugImplementation(libs.ui.tooling) // For debugging previews
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.activity.compose.v181)

    // Jetpack Compose
    implementation(libs.androidx.compose.bom.v20231001)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel with Composer
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Document File Provider
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // jCIFS for SMB functionality
    implementation("eu.agno3.jcifs:jcifs-ng:2.1.9")  // For SMB/CIFS support

    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")  // Use the version that matches your Compose version

    implementation("androidx.navigation:navigation-compose:2.7.4")
}