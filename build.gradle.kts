import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.0"
}

group = "com.github.fjlopez"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.typesafe.akka:akka-actor_2.11:2.5.11")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}