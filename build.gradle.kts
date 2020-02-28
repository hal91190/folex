plugins {
    kotlin("jvm") version "1.3.61"
    application
}

group = "folex"
version = "1.0-SNAPSHOT"

val javaVersion = JavaVersion.VERSION_1_8
val klaxonVersion = "5.2"
val jUnitVersion = "5.5.1"

application {
    mainClassName = "fr.uvsq.folex.FolexKt"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.beust:klaxon:$klaxonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = javaVersion.toString()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
