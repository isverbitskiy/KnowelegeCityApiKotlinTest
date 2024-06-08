plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("io.qameta.allure") version "2.11.0"
}

group = "com.isverbit.KnowledgeCityApiKotlin"
version = "1.0-SNAPSHOT"

val ktorVersion = "2.3.11"
val kotlinVersion = "2.0.0"
val junitVersion = "5.10.2"
val fakerVersion = "1.0.2"
val allureJunit5Version = "2.20.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    implementation("com.github.javafaker:javafaker:$fakerVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.qameta.allure:allure-junit5:$allureJunit5Version")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("allureReport")
}

allure {
    version = allureJunit5Version
}

kotlin {
    jvmToolchain(17)
}