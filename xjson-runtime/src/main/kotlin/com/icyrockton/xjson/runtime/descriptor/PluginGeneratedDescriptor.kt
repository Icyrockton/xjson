package com.icyrockton.xjson.runtime.descriptor

import com.icyrockton.xjson.runtime.PluginGeneratedXSerializer

class PluginGeneratedDescriptor(
    override val name: String,
    override val elementsCount: Int,
    private val generatedXSerializer: PluginGeneratedXSerializer<*>,
) : Descriptor {

    override val kind: DescriptorKind
        get() = StructureKind.OBJ

    private var current = 0
    private var elementOptional = Array(elementsCount) { false }
    private var elementNames = Array(elementsCount) { "[XJson]" }
    private val name2idx by lazy {
        elementNames.mapIndexed { index, s -> s to index }.toMap()
    }

    fun element(name: String, isOptional: Boolean = false) {
        elementNames[current] = name
        elementOptional[current] = isOptional
        current++
    }

    override fun getElementName(index: Int): String {
        return elementNames[index]
    }

    override fun getElementIndex(name: String): Int {
        return name2idx[name]!!
    }

    override fun getElementDescriptor(index: Int): Descriptor =
        generatedXSerializer.childSerializers()[index].descriptor


    override fun getElementIsOptional(index: Int): Boolean {
        return elementOptional[index]
    }

}