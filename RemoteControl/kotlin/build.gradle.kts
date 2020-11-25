import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.Service
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

val getProps = rootProject.extra["getProps"] as (String) -> String

plugins {
    id("org.hidetake.ssh") version "2.10.1"
    application
    kotlin("jvm") version "1.4.20"
}



val mainPackage = "com.zelgius.remoteController"
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
    implementation(group = "net.sf.bluecove", name = "bluecove", version = "2.1.0")
    implementation(group = "net.sf.bluecove", name = "bluecove-gpl", version = "2.1.0")
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

var raspberry = remotes.create("raspberry") {
    host = "192.168.1.38"
    user = "pi"
    password = getProps("password")
}


val deploy = tasks.create("deploy") {
    doLast {
        ssh.runSessions {
            session(raspberry) {
                try {
                    execute("sudo rm ${jar.archiveFile.get().asFile.name}")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
                put(jar.archiveFile.get().asFile, File("/home/pi/"))
                logger.warn(execute("chmod +x ${jar.archiveFile.get().asFile.name}"))
                logger.warn(execute("sudo java -jar ${jar.archiveFile.get().asFile.name}"))
            }
        }
    }
}



deploy.dependsOn(jarTask)

fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: Any, into: Any) =
    put(hashMapOf("from" to from, "into" to into))
