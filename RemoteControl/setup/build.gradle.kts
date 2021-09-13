

plugins {
    kotlin("jvm") version "1.5.21"
}

version = "unspecified"

repositories {
    mavenCentral()
}
val SourceSetContainer.main: SourceSet get() = getByName("main")

val setup = tasks.create("setup") {
    doLast {

    }
}

val copy = tasks.create("copy") {
    doLast {

    }
}

setup.dependsOn(copy)

