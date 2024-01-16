pluginManagement {
    val compilerVersion: String by settings
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
        kotlin("jvm") version compilerVersion
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "xjson"
include("xjson-plugin")
include("xjson-plugin-gradle")
include("xjson-runtime")
