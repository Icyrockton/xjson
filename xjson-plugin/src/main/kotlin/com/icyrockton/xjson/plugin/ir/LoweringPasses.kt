package com.icyrockton.xjson.plugin.ir

import com.icyrockton.xjson.plugin.XJsonPluginGenerated
import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.hasAnnotation


class SerializerLowering(private val pluginContext: IrPluginContext) : ClassLoweringPass {
    override fun lower(irClass: IrClass) {
        val parent = irClass.parent
        val clazzName = irClass.name
        if(parent is IrClass  &&  parent.hasSerializableAnnotationWithoutArgs
            && clazzName == XJsonPluginGenerated.GenerateSerializer
            ) {
            // $serializer$ class
            IrSerializerGenerator(irClass,pluginContext).generate()
        }
    }
}