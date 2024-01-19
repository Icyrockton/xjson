package com.icyrockton.xjson.plugin

import com.icyrockton.xjson.plugin.fir.XJsonFir
import com.icyrockton.xjson.plugin.ir.XJsonIr
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class XJsonPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        register(this, configuration)
    }

    companion object {

        fun register(storage: ExtensionStorage, configuration: CompilerConfiguration) = with(storage) {
            FirExtensionRegistrarAdapter.registerExtension(XJsonFir())
            IrGenerationExtension.registerExtension(XJsonIr())
        }

    }
}