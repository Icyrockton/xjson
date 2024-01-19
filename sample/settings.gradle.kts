plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "sample"
includeBuild("..") // reference root project e.g. `com.icyrockton:xjson-runtime`
