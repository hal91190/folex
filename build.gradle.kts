plugins {
    kotlin("jvm") version "1.3.61"
    application
}

group = "folex"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "fr.uvsq.folex.FolexKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}