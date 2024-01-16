package com.icyrockton.xjson.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class XJsonGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        println("you call this plugin ${project.name} ${project}")
        return project.provider {
            val options = mutableListOf<SubpluginOption>()
            options
        }
    }

    override fun getCompilerPluginId(): String = "com.icyrockton.xjson.plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact("com.icyrockton", "xjson-plugin")

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}