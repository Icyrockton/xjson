package com.icyrockton.xjson.plugin

import com.icyrockton.xjson.plugin.XJsonPackage.annotationPackage
import com.icyrockton.xjson.plugin.XJsonPackage.runtimePackage
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


object XJsonPackage {
    val annotationPackage = FqName("com.icyrockton.xjson.runtime.annotation")
    val runtimePackage = FqName("com.icyrockton.xjson.runtime")
}

object XJsonClassId {
    val serializableClassId = ClassId(annotationPackage, Name.identifier("XSerializable"))
    val serializableFqName = serializableClassId.asSingleFqName()

    val pluginGeneratedSerializerClassId = ClassId(runtimePackage,Name.identifier("PluginGeneratedXSerializer"))
}

object XJsonNames {
    val SERIALIZE = Name.identifier("serialize")
    val DESERIALIZE = Name.identifier("deserialize")
    val DESCRIPTOR = Name.identifier("descriptor")
    val TYPE_PARAMETER_SERIALIZERS = Name.identifier("typeParameterSerializers")
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