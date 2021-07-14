plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.21"
}

group = "xyz.missingnoshiny.ftg.core"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.20")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
    implementation("io.ktor:ktor-server-core:1.6.1")
    implementation("io.ktor:ktor-websockets:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
}