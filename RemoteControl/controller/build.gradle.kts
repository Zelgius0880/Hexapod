import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.protobuf.gradle.protoc

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.google.protobuf")
}

val mainPackage = "com.zelgius.remoteController"
group = mainPackage
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://repo1.maven.org/maven2/")
    maven(url = "https://jitpack.io")
    maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
}



dependencies {
    implementation(project(":drivers"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")
    implementation("com.google.protobuf:protobuf-java:3.6.1")

    testImplementation("org.slf4j:slf4j-simple:2.0.0-alpha0")
    testImplementation(kotlin("test-junit5"))

}

tasks.withType(KotlinCompile::class.java) {
    kotlinOptions {
        val options = this as org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
        options.jvmTarget = "1.8"
    }

    //project.projectDir
}



application {
    mainClass.set("${mainPackage}.MainKt")
}

lateinit var archiveJar: Jar
tasks {
    named<ShadowJar>("shadowJar") {
        archiveJar = this
        logger.warn("Building jar")
        manifest {
            attributes(
                "Implementation-Version" to archiveVersion.get(),
                "Main-Class" to "$mainPackage.MainKt"
            )
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName.set(project.name)
        mergeServiceFiles()
    }
}

setupJavaDeployTasks( tasks.shadowJar.get().archiveFile.get().asFile, tasks.shadowJar)

protobuf {
    protobuf.protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:3.6.1"
    }
}