plugins {
    kotlin("jvm") version "1.3.61"
    application
}

group = "folex"
version = "1.0-SNAPSHOT"

val javaVersion = JavaVersion.VERSION_1_8
val klaxonVersion = "5.2"
val csvVersion = "1.8"
val jGitVersion = "5.7.0.202003110725-r"
val jUnitVersion = "5.5.1"
val slf4JVersion = "1.7.30"
val logbackVersion = "1.2.3"

application {
    mainClassName = "fr.uvsq.folex.FolexKt"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.beust:klaxon:$klaxonVersion")
    implementation("org.apache.commons:commons-csv:$csvVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jGitVersion")

    implementation("org.slf4j:slf4j-api:$slf4JVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = javaVersion.toString()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
