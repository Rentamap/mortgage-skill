import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

val versionProps = Properties().apply {
    file("version.properties").inputStream().use { load(it) }
}

group = "com.rentamap"
version = versionProps.getProperty("version")

dependencies {
    implementation(libs.clikt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.property)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.rentamap.cli.MainKt")
}

tasks.shadowJar {
    archiveBaseName.set("french-property-investment")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "com.rentamap.cli.MainKt"
    }
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
