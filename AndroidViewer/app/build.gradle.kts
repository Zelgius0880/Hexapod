plugins {
    id("com.android.application")
    id("kotlin-android")
}
val kotlinVersion = rootProject.extra["kotlinVersion"]
android {
    compileSdkVersion(30)
    buildToolsVersion( "30.0.2")

    defaultConfig {
        applicationId = "com.zelgius.androidviewer"
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        externalNativeBuild {
            cmake {
                cppFlags.add("-std=c++17")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
    }

    buildTypes {
        getByName("release") {
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    externalNativeBuild {
        cmake {
            path("CMakeLists.txt")
        }
    }

}

dependencies {
    implementation (fileTree("dir" to "libs", "include" to mutableListOf("*.jar")))

    project(":sdk")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation("com.google.android.material:material:1.2.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

}


// The dependencies for NDK builds live inside the .aar files so they need to
// be extracted before NDK targets can link against.
val extractNdk = tasks.register("extractNdk", Copy::class.java) {
    if (file("${project.rootDir}/sdk/build/outputs/aar/sdk-release.aar").exists()) {
        copy {
            from(zipTree("${project.rootDir}/sdk/build/outputs/aar/sdk-release.aar"))
            into("libraries/")
            include("jni/**/libcardboard_api.so")
        }
        copy {
            from("${project.rootDir}/sdk/include/cardboard.h")
            into("libraries/")
        }
    }
}

val deleteNdk = tasks.register("deleteNdk", Delete::class.java) {
    delete("libraries/jni")
    delete("libraries/cardboard.h")
}

tasks.getByName("build").dependsOn(extractNdk)
tasks.getByName("clean").dependsOn(deleteNdk)
