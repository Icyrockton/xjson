package com.icyrockton.xjson.plugin.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class XJsonFir() : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::FirSerializerGenerator
    }
}