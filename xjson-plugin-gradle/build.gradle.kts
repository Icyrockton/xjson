plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
}

group = "com.icyrockton"
version = "1.0"

repositories {
    mavenCentral()
}

gradlePlugin{
    plugins {
        create("XJsonGradlePlugin") {
            id = "com.icyrockton.xjson.plugin"
            displayName = "XJsonGradlePlugin"
            description = "XJson: A serialization tool based on Kotlin compiler plugin"
            implementationClass = "com.icyrockton.xjson.gradle.XJsonGradlePlugin"
        }
    }
}

val compilerVersion : String by properties
dependencies {
    implementation(kotlin("gradle-plugin-api"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}