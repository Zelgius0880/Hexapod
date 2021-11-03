plugins {
    kotlin("jvm")
    id("java-library")
}

repositories {
    mavenCentral()
    maven(url = "https://repo1.maven.org/maven2/")
}

dependencies {
    val pi4jVersion = "2.1.0"

    api("com.pi4j:pi4j-core:$pi4jVersion")
    api(group = "com.pi4j", name = "pi4j-plugin-linuxfs", version = pi4jVersion)
    api(group = "com.pi4j", name = "pi4j-plugin-pigpio", version = pi4jVersion)
    api(group = "com.pi4j", name = "pi4j-plugin-raspberrypi", version = pi4jVersion)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")

    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}