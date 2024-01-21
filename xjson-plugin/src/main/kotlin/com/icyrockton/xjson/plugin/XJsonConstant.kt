package com.icyrockton.xjson.plugin

import com.icyrockton.xjson.plugin.XJsonPackage.annotationPackage
import com.icyrockton.xjson.plugin.XJsonPackage.descriptorPackage
import com.icyrockton.xjson.plugin.XJsonPackage.encodingPackage
import com.icyrockton.xjson.plugin.XJsonPackage.exceptionPackage
import com.icyrockton.xjson.plugin.XJsonPackage.runtimePackage
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


object XJsonPackage {
    val annotationPackage = FqName("com.icyrockton.xjson.runtime.annotation")
    val runtimePackage = FqName("com.icyrockton.xjson.runtime")
    val encodingPackage = FqName("com.icyrockton.xjson.runtime.encoding")
    val corePackage = FqName("com.icyrockton.xjson.runtime.core")
    val descriptorPackage = FqName("com.icyrockton.xjson.runtime.descriptor")
    val exceptionPackage = FqName("com.icyrockton.xjson.runtime.exception")
}

object XJsonClassId {
    val serializableClassId = ClassId(annotationPackage, Name.identifier("XSerializable"))
    val serializableFqName = serializableClassId.asSingleFqName()

    val pluginGeneratedSerializerClassId = ClassId(runtimePackage,Name.identifier("PluginGeneratedXSerializer"))
    val serializerClassId = ClassId(runtimePackage,Name.identifier("XSerializer"))
    val serializerFqName = serializerClassId.asSingleFqName()

    val compositeEncoderClassId = ClassId(encodingPackage,Name.identifier("CompositeEncoder"))
    val encoderClassId = ClassId(encodingPackage,Name.identifier("Encoder"))
    val compositeDecoderClassId = ClassId(encodingPackage,Name.identifier("CompositeDecoder"))
    val decoderClassId = ClassId(encodingPackage,Name.identifier("Decoder"))
    val missingPropertyExceptionClassId = ClassId(exceptionPackage,Name.identifier("MissingPropertyException"))

    val pluginGeneratedDescriptor = ClassId(descriptorPackage,Name.identifier("PluginGeneratedDescriptor"))
}

object XJsonNames {
    val SERIALIZE = Name.identifier("serialize")
    val DESERIALIZE = Name.identifier("deserialize")
    val DESCRIPTOR = Name.identifier("descriptor")
    val TYPE_PARAMETER_SERIALIZERS = Name.identifier("typeParameterSerializers")
    val CHILD_SERIALIZERS = Name.identifier("childSerializers")
    val BEGIN_STRUCTURE = Name.identifier("beginStructure")
    val END_STRUCTURE = Name.identifier("endStructure")
    val DECODE_ELEMENT_INDEX = Name.identifier("decodeElementIndex")
    fun TYPE_PARAMETER_SERIALIZER_VALUE_PARAMETER_NAME(index:Int) = Name.identifier("tpSerializer_${index}")

}

object XJsonAnnotationParam {
    val WITH = Name.identifier("with")
}

object XJsonPluginGenerated {
    val GenerateSerializer = Name.identifier("\$serializer\$")
}

object XJsonPluginKey : GeneratedDeclarationKey() {
    override fun toString(): String {
        return "XJson GeneratedDeclarationKey"
    }
}

val XJsonPluginKey.irOrigin : IrDeclarationOrigin
    get() = IrDeclarationOrigin.GeneratedByPlugin(this)