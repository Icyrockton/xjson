import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm")
}

group = "com.icyrockton"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("compiler"))
    compileOnly(kotlin("compiler"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("compiler-internal-test-framework"))  // compiler plugin test generator / test utils

    testRuntimeOnly(kotlin("test"))
    testRuntimeOnly(kotlin("script-runtime"))
    testRuntimeOnly(kotlin("annotations-jvm"))
    testImplementation(kotlin("reflect"))
    testImplementation(project(":xjson-runtime"))   // include `XJson runtime` in test
}

sourceSets {
    main{
        java.setSrcDirs(listOf("src/main/java"))
        kotlin.setSrcDirs(listOf("src/main/kotlin"))
        resources.setSrcDirs(listOf("src/main/resources"))
    }

    test{
        kotlin.setSrcDirs(listOf("src/test","src/test-gen"))
        java.setSrcDirs(listOf("src/test","src/test-gen"))
        resources.setSrcDirs(listOf("src/test/resources"))
    }
}

task<JavaExec>("generateTest") {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "com.icyrockton.xjson.plugin.GenerateTestsKt"
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi"))
    }
}

// add following properties for test
tasks.test {
    useJUnitPlatform()
    doFirst {
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
        setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
    }
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = project.configurations
        .testRuntimeClasspath.get()
        .files
        .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}