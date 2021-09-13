import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
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
    val pi4jVersion = "2.0"
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("com.pi4j:pi4j-core:$pi4jVersion")
    implementation(group = "com.pi4j", name = "pi4j-plugin-linuxfs", version = pi4jVersion)
    implementation(group = "com.pi4j", name = "pi4j-plugin-pigpio", version = pi4jVersion)
    implementation(group = "com.pi4j", name = "pi4j-plugin-raspberrypi", version = pi4jVersion)

    testImplementation("org.junit.platform:junit-platform-commons:1.7.2")
    testImplementation(kotlin("test-junit5"))

}

tasks.withType(KotlinCompile::class.java) {
    kotlinOptions {
        val options = this as org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
        options.jvmTarget = "1.8"
    }
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

