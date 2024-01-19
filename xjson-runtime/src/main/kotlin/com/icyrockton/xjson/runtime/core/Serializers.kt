package com.icyrockton.xjson.runtime.core

import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.XSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.typeOf

inline fun <reified T> T.serializer(): XSerializer<T> {
    return findSerializerByType(typeOf<T>())
}

inline fun <reified T> serializer() : XSerializer<T> {
    return findSerializerByType(typeOf<T>())
}


fun <T> findSerializerByType(type: KType): XSerializer<T> {
    val kclass = type.kclass()
    val isNullable = type.isMarkedNullable // todo nullable
    val typeArguments = type.arguments.map {
        requireNotNull(it.type) { "startProjection is not supported from type $type" }
    }

    return kclass.findSerializer(typeArguments)?.cast() ?: error("No serializer found for type $type")
}

@Suppress("UNCHECKED_CAST")
fun <T> XSerializer<*>.cast() : XSerializer<T> {
    return this as XSerializer<T>
}

private fun <T : Any> KClass<T>.findSerializer(typeArguments: List<KType>): XSerializer<T>? {
    val javaClass = java
    val typeArgumentSerializers = typeParameterSerializers(typeArguments)

    if (typeArguments.isEmpty())
        findPrimitiveSerializer()?.let { return it }


    // step.1 find from declared class $serializer
    findNestedSerializerClass(javaClass, typeArgumentSerializers)?.let { return it }

    // step.2 .... TODO

    return null
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> KClass<T>.findPrimitiveSerializer(): XSerializer<T>? {
    return PRIMITIVE_SERIALIZER[this] as? XSerializer<T>
}


@Suppress("UNCHECKED_CAST")
private fun <T> findNestedSerializerClass(jClass: Class<T>, typeSerializers: List<XSerializer<Any>>): XSerializer<T>? {
    val serializerClass = jClass.declaredClasses.singleOrNull { it.simpleName == "\$serializer\$" } ?: return null
    // object INSTANCE
    serializerClass.fields.singleOrNull { it.name == "INSTANCE" }?.let {
        return it.get(null) as XSerializer<T>
    }

    // call constructor
    val typeParameters = Array(typeSerializers.size) { XSerializer::class.java }
    val ctr = serializerClass.getConstructor(*typeParameters)


    return ctr.newInstance(*typeSerializers.toTypedArray()) as? XSerializer<T>
}

private fun typeParameterSerializers(types: List<KType>): List<XSerializer<Any>> {
    val xSerializers = types.map { findSerializerByType<Any>(it) }
    return xSerializers
}

internal fun KType.kclass(): KClass<*> = when (val t = classifier) {
    is KClass<*> -> t
    is KTypeParameter -> error("typeParameter is not supported from type $this")
    else -> error("this branch should not be reached: $this")
}