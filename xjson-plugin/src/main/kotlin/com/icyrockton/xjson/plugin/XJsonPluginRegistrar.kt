package com.icyrockton.xjson.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class XJsonPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        Companion.register(this, configuration)
    }

    companion object {

        fun register(storage: ExtensionStorage, configuration: CompilerConfiguration) = with(storage) {
//            FirExtensionRegistrarAdapter.registerExtension()
//            IrGenerationExtension.registerExtension()
        }

    }
}