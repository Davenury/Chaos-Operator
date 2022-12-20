plugins {
    kotlin("jvm") version "1.7.10"
    java
    application
}

val javaOperatorVersion = "4.1.1"
val ktor_version: String = "2.1.3"

group = "com.github.davenury.operator"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.github.davenury.operator.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")

    implementation("io.javaoperatorsdk:operator-framework:${javaOperatorVersion}")
    annotationProcessor("io.javaoperatorsdk:operator-framework:${javaOperatorVersion}")
    annotationProcessor("io.fabric8:crd-generator-apt:5.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}