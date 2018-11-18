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
    compile("com.typesafe.akka:akka-actor_2.12:2.5.18")
    compile("com.enragedginger:akka-quartz-scheduler_2.12:1.7.0-akka-2.5.x")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}