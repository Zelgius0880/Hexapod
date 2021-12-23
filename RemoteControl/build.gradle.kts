buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath ("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
    }
}

allprojects {

    repositories {
        mavenCentral()
    }

}

plugins {
}

subprojects {
    version = "1.0"
}