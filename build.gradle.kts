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

    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("ch.qos.logback:logback-classic:1.4.4")

    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")

    //operator
    implementation("io.javaoperatorsdk:operator-framework:${javaOperatorVersion}")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.1.3")
    annotationProcessor("io.javaoperatorsdk:operator-framework:${javaOperatorVersion}")
    annotationProcessor("io.fabric8:crd-generator-apt:5.4.0")

    // metrics
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}