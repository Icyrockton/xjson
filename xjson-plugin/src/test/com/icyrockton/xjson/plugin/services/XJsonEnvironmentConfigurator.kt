package com.icyrockton.xjson.plugin.services

import com.icyrockton.xjson.plugin.XJsonPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

/**
 * Inject XJson plugin into test environment
 */
class XJsonEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {

    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        XJsonPluginRegistrar.register(this, configuration)
    }
}