plugins {
    kotlin("jvm")
}

version = "unspecified"

repositories {
    mavenCentral()
}
val SourceSetContainer.main: SourceSet get() = getByName("main")

val shadowJarTask = project(":controller").tasks.getByName("shadowJar")

val setup = tasks.create("setup") {
    doLast {
        ssh("chmod +x /home/pi/setup/setup.sh")
        ssh("chmod +x /home/pi/setup/copy.sh")

        ssh("cd /home/pi/setup/")
        ssh("sudo copy.sh")
        ssh("sudo setup.sh")
    }
}

val copy = tasks.create("copy") {
    doLast {
        ssh("mkdir -p /home/pi/setup")
        sourceSets.main.resources.forEach {
            scp(it, "/home/pi/setup")
        }
        scp(shadowJarTask.outputs.files.files.first(), "/home/pi/setup")
    }
}

copy.dependsOn(shadowJarTask)
setup.dependsOn(copy)

