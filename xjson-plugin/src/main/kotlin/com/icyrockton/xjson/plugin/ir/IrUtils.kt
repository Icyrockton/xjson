package com.icyrockton.xjson.plugin.ir

import com.icyrockton.xjson.plugin.XJsonClassId.serializableFqName
import com.icyrockton.xjson.plugin.XJsonClassId.serializerFqName
import com.icyrockton.xjson.plugin.XJsonPluginGenerated.GenerateSerializer
import com.icyrockton.xjson.plugin.XJsonPluginKey
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

val IrClass.hasSerializableAnnotationWithoutArgs: Boolean
    get() {
        val annotation = getSerializableAnnotation() ?: return false
        return annotation.getValueArgument(0) == null
    }

val IrClass.hasSerializableAnnotation
    get() = getSerializableAnnotation() != null

fun IrClass.getSerializableAnnotation(): IrConstructorCall? {
    return annotations.findAnnotation(serializableFqName)
}

fun IrClass.findPluginGenerateFunctionByName(name: Name): IrSimpleFunction? {
    return functions.find { it.name == name && it.origin.isFromPlugin }
}

fun IrClass.findPluginGeneratePropertyByName(name: Name): IrProperty? {
    return properties.find { it.name == name && it.origin.isFromPlugin }
}

val IrClass.serializableClass: IrClass
    get() {
        val parent = this.parent as? IrClass ?: error("The serialization class for serializer $this was not found")
        if (!parent.hasSerializableAnnotation) error("$parent don't has @Serializable annotation")
        return parent
    }

val IrDeclarationOrigin.isFromPlugin
    get() = this == IrDeclarationOrigin.GeneratedByPlugin(XJsonPluginKey)

fun IrClass.getGenerateSerializerClass(): IrClass? {
    return declarations.asSequence().filterIsInstance<IrClass>()
        .find { it.name == GenerateSerializer && it.origin.isFromPlugin }
}

val IrType.isXSerializer: Boolean
    get() = this.classFqName == serializerFqName
