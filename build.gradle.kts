buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.10.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
        classpath("com.google.gms:google-services:4.4.1")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}