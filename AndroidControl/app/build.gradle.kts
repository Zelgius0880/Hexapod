import org.apache.tools.ant.taskdefs.condition.Os

plugins{
    id("com.android.application")
    kotlin("android")
    id("de.mannodermaus.android-junit5")
}

android {
    compileSdkVersion (30)
    buildToolsVersion ("30.0.3")
    lintOptions {
        isAbortOnError  = false
    }
    defaultConfig {
        applicationId ("com.zelgius.hexapod.viewer")
        minSdkVersion (30)
        targetSdkVersion (30)
        versionCode (1)
        versionName = "1.0.0"
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 2) Connect JUnit 5 to the runner
        testInstrumentationRunnerArgument("runnerBuilder", "de.mannodermaus.junit5.AndroidJUnit5Builder")
        ndk {

            this@ndk.abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        externalNativeBuild {
            cmake {
                cppFlags.add("-std=c++17")
                arguments( "-DANDROID_STL=c++_shared")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled =  false
            proguardFiles( getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    externalNativeBuild {
        cmake {
            path ("CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}

val kotlin_version by extra(rootProject)
dependencies {
    implementation (fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")

    // Android Mobile Vision
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.google.android.material:material:1.3.0")
    implementation (project(":sdk"))
    implementation ("androidx.core:core-ktx:+")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2-native-mt")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    androidTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.2.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.2.0")
    androidTestImplementation ("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    androidTestImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation ("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation ("androidx.test:rules:1.3.0")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    androidTestImplementation( "androidx.test:rules:1.3.0")
}

// The dependencies for NDK builds live inside the .aar files so they need to
// be extracted before NDK targets can link against.
tasks.create("extractNdk", Copy::class.java)  {
    if (file("${project.rootDir}/sdk/build/outputs/aar/sdk-release.aar").exists()) {
        copy {
            from (zipTree("${project.rootDir}/sdk/build/outputs/aar/sdk-release.aar"))
            into ("libraries/")
            include ("jni/**/libcardboard_api.so")
        }
        copy {
            from ("${project.rootDir}/sdk/include/cardboard.h")
            into ("libraries/")
        }
    }

}
tasks.create("deleteNdk", Delete::class.java) {
    delete ("libraries/jni")
    delete ("libraries/cardboard.h")
}

tasks.getByName("build").dependsOn("extractNdk")
tasks.getByName("build").dependsOn("sdk:assemble")
tasks.getByName("clean").dependsOn("deleteNdk")
repositories {
    maven (url  = "https://dl.bintray.com/kotlin/kotlin-eap" )
    mavenCentral()
}

val androidPlugin = project.plugins.findPlugin("android")
//val adb = androidPlugin.sdkHandler.sdkInfo?.adb
val mypackageDir = "/storage/emulated/0/DCIM/result.png"

//Grant necessary permissions and create storage dir

/*
val imagingBeforeTest = tasks.create("imagingBeforeTest", Exec::class.java) {
    commandLine ("adb", "shell", "pm", "grant", "${android.defaultConfig.applicationId}", "android.permission.READ_EXTERNAL_STORAGE")
    commandLine ("adb", "shell", "pm", "grant", "${android.defaultConfig.applicationId}", "android.permission.WRITE_EXTERNAL_STORAGE")
    //commandLine ("adb", "shell", "rm", "-r", mypackageDir)
    //commandLine ("adb", "shell", "mkdir", mypackageDir)
}

afterEvaluate {
    tasks.withType(Test::class.java).forEach {
        it.dependsOn(imagingBeforeTest)
    }
}
*/

//Pull content of storage dir to the local build directory
val pullImage = tasks.register("imagingAfterTest",  Exec::class.java) {
        // Alias is /storage/self/primary/Download/mypackage
        val testDir = File(rootProject.buildDir.absoluteFile, "/pulledTest/")
        //testDir.mkdir()
        println("copy")
        println(commandLine("adb", "pull", mypackageDir, testDir.absolutePath))
}
/*


tasks.register("testAndPull", Exec::class.java) {
   */
/* afterEvaluate {
        tasks.getByName("connectedAndroidTest").finalizedBy("imagingAfterTest")
    }*//*


    val gradle = if(Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew"
    commandLine("${rootDir.absolutePath}${File.separator}$gradle app:testDebugUnitTest --tests com.zelgius.hexapod.viewer.overlay.BatteryLevelTest")
    this.finalizedBy(pullImage)
}*/
