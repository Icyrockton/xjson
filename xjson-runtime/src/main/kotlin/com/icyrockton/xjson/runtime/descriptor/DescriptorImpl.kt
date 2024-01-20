package com.icyrockton.xjson.runtime.descriptor

import com.icyrockton.xjson.runtime.core.serializer
import com.icyrockton.xjson.runtime.descriptor.Descriptor.Companion.UNKNOWN_ELEMENT

class DescriptorBuilder {
    private val uniqueNames = hashSetOf<String>()
    internal val elementNames = mutableListOf<String>()
    internal val elementIsOptional = mutableListOf<Boolean>()
    internal val elementDescriptors = mutableListOf<Descriptor>()

    fun element(name: String, descriptor: Descriptor, isOptional: Boolean) {
        assert(uniqueNames.add(name)) { "element $name already registered in this descriptor[$name]" }
        elementNames += name
        elementDescriptors += descriptor
        elementIsOptional += isOptional
    }

    inline fun <reified T> element(name: String, isOptional: Boolean = false) {
        val descriptor = serializer<T>().descriptor
        element(name, descriptor, isOptional)
    }
}


fun buildSerialDescriptor(name: String, kind: DescriptorKind, block: DescriptorBuilder.() -> Unit): Descriptor {
    val builder = DescriptorBuilder()
    builder.block()
    return DescriptorImpl(name, kind, builder.elementNames.size, builder)
}

fun buildObjSerialDescriptor(name: String, block: DescriptorBuilder.() -> Unit): Descriptor {
    val builder = DescriptorBuilder()
    builder.block()
    return DescriptorImpl(name, StructureKind.OBJ, builder.elementNames.size, builder)
}

class DescriptorImpl(
    override val name: String,
    override val kind: DescriptorKind,
    override val elementsCount: Int,
    builder: DescriptorBuilder
) : Descriptor {
    private val elementNames = builder.elementNames.toTypedArray()
    private val name2Index = builder.elementNames.mapIndexed { index, name -> name to index }.toMap()
    private val elementDescriptors = builder.elementDescriptors
    private val elementIsOptional = builder.elementIsOptional
    override fun getElementName(index: Int): String = elementNames[index]
    override fun getElementIndex(name: String): Int = name2Index[name] ?: UNKNOWN_ELEMENT
    override fun getElementDescriptor(index: Int): Descriptor = elementDescriptors[index]
    override fun getElementIsOptional(index: Int): Boolean = elementIsOptional[index]

    override fun toString(): String = "$name(${elementNames.joinToString(separator = ", ")})"
}