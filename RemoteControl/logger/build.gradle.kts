
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

val getProps = rootProject.extra["getProps"] as (String) -> String
val deployJarTask = rootProject.extra["deployJarTask"] as (Project, TaskProvider<Jar>) -> Unit
val copyJarTask = rootProject.extra["copyJarTask"] as (Project, TaskProvider<Jar>) -> Unit

plugins {
    application
    kotlin("jvm") version "1.4.20"
}

val mainPackage = "com.zelgius.remoteController.logger"
group = mainPackage
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
}
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.pi4j:pi4j-core:1.2")
    implementation("com.github.mhashim6:Pi4K:0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
    testImplementation(kotlin("test-junit5"))
}

tasks.withType(KotlinCompile::class.java) {
    kotlinOptions {
        val options = this as org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
        options.jvmTarget = "1.8"
    }
}
application {
    mainClassName = "MainKt"
}

lateinit var jar: Jar
val jarTask = tasks.register("fatJar", Jar::class.java) {
    jar = this
    jar.apply {
        logger.warn("Building jar")
        manifest {
            attributes(
                "Implementation-Title" to "Gradle Jar File Example",
                "Implementation-Version" to archiveVersion.get(),
                "Main-Class" to "$mainPackage.MainKt"
            )
        }
        archiveBaseName.set(project.name + "-all")
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        with(tasks.jar.get() as CopySpec)
    }
}

copyJarTask(project, jarTask)
deployJarTask(project, jarTask)
