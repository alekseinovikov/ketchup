plugins {
    kotlin("jvm") version "1.4.31"
    id("java")
}

group = "com.ketchup.server"
version = "1.0-SNAPSHOT"

val nettyVersion: String by project


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.netty:netty-all:$nettyVersion")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.ketchup.server.ServerKt"
    }
    from(configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) })
}