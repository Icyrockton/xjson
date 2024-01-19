package com.icyrockton.xjson.plugin.ir

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class XJsonIr : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val lowerings : List<FileLoweringPass> = listOf(SerializerLowering(pluginContext))
        for(lowerPass in lowerings){
            moduleFragment.files.forEach {
                lowerPass.lower(it)
            }
        }
    }
}