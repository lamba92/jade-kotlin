import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.50"
}

group = "it.lamba"
version = "1.0"

repositories {
    mavenCentral()
    maven(url="https://jitpack.io")
    maven(url="http://jade.tilab.com/maven/")
}

dependencies {
    implementation("com.jfoenix","jfoenix","9.0.4")
    implementation("net.bramp.ffmpeg", "ffmpeg", "0.6.2")
    implementation(kotlin("reflect"))
    implementation("org.greenrobot", "eventbus", "3.1.1")
    implementation("com.google.code.gson", "gson", "2.8.4")
    implementation("com.github.Lamba92", "jade-modern-agent", "1.2")
    implementation("com.tilab.jade", "jade", "4.5.0")
    implementation("commons-codec", "commons-codec", "1.9")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}