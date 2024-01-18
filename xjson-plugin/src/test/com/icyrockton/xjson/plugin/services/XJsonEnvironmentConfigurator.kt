package com.icyrockton.xjson.plugin.services

import com.icyrockton.xjson.plugin.XJsonPluginRegistrar
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

/**
 * Inject XJson plugin into test environment
 */
class XJsonEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {

    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        // register XJson plugin
        XJsonPluginRegistrar.register(this, configuration)
    }

    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        // register XJson runtime
        getXJsonRuntimeJarFile()?.let { configuration.addJvmClasspathRoot(it) }
    }

    private fun getXJsonRuntimeJarFile(): File? {
        try {
            val clazz = Class.forName("com.icyrockton.xjson.runtime.XSerializer")
            return PathUtil.getResourcePathForClass(clazz)
        } catch (e: ClassNotFoundException) {
            assert(false) { "XJson runtime jar not found!" }
        }
        return null
    }

}