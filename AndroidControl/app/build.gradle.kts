import com.google.protobuf.gradle.*

plugins {
    id("com.android.application")
    kotlin("android")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        applicationId = "com.zelgius.hexapod.controller"
        minSdk = 26
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin.sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += listOf("platform")
    productFlavors {
        create("emulator") {
            dimension = "platform"
            buildConfigField("String","CAMERA_URL", "\"http://10.0.2.2\"")
        }

        create("device") {
            dimension = "platform"
            buildConfigField("String","CAMERA_URL", "\"http://192.168.1.145:8080/?action=stream\"")
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.material3:material3:1.0.0-alpha02")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.compose}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.lifeCycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifeCycle}")
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.0.0-rc02")

    implementation(project(":api"))
    implementation(project(":mjpg_streamer"))

    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hilt}")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.compose}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
}
